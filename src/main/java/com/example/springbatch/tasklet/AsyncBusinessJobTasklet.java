package com.example.springbatch.tasklet;

import com.example.springbatch.config.BatchProperties;
import com.example.springbatch.model.BatchExecution;
import com.example.springbatch.model.Task;
import com.example.springbatch.repository.BatchExecutionRepository;
import com.example.springbatch.repository.TaskRepository;
import com.example.springbatch.service.JobStopManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * ステップ3: 非同期で業務Jobを実行するTasklet
 */
@Component
public class AsyncBusinessJobTasklet implements Tasklet {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncBusinessJobTasklet.class);
    
    @Autowired
    private BatchExecutionRepository batchExecutionRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private JobStopManager jobStopManager;
    
    @Autowired
    private BatchProperties batchProperties;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Job実行IDを取得
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        
        // バッチIDを生成（Job実行IDをバッチIDとして使用）
        String batchId = "BATCH_" + jobExecutionId + "_" + System.currentTimeMillis();
        
        logger.info("単一非同期Job実行開始 - バッチID: {}, Job実行ID: {}", batchId, jobExecutionId);
        
        // バッチ実行記録を作成
        BatchExecution batchExecution = new BatchExecution();
        batchExecution.setBatchId(batchId);
        batchExecution.setJobId(jobExecutionId);
        batchExecution.setStatus(batchProperties.getStatus().getProcessing());
        batchExecution.setStartTime(java.time.LocalDateTime.now());
        batchExecution = batchExecutionRepository.save(batchExecution);
        
        Long batchExecutionId = batchExecution.getId();
        
        // 非同期で業務処理を実行
        CompletableFuture<Void> future = executeBusinessLogicAsync(batchId, batchExecutionId, jobExecutionId);
        
        // 非同期処理の開始を確認（実際の完了は待たない）
        logger.info("非同期業務Job実行開始完了 - バッチID: {}", batchId);
        
        // 注意: CompletableFutureは序列化できないため、JobExecutionContextには保存しない
        
        return RepeatStatus.FINISHED;
    }
    
    /**
     * バッチ実行記録のステータスを更新する
     * @param batchExecutionId バッチ実行ID
     * @param status ステータス
     */
    private void updateBatchExecutionStatus(Long batchExecutionId, String status) {
        Optional<BatchExecution> optionalBatchExecution = batchExecutionRepository.findById(batchExecutionId);
        if (optionalBatchExecution.isPresent()) {
            BatchExecution batchExecution = optionalBatchExecution.get();
            batchExecution.setStatus(status);
            batchExecution.setEndTime(java.time.LocalDateTime.now());
            batchExecutionRepository.save(batchExecution);
        }
    }
    
    /**
     * 非同期で業務ロジックを実行
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeBusinessLogicAsync(String batchId, Long batchExecutionId, Long jobExecutionId) {
        try {
            // 現在のスレッドをJobStopManagerに登録し、強制中断をサポート
            jobStopManager.registerJobThread(jobExecutionId, Thread.currentThread());
            
            logger.info("非同期業務処理開始 - バッチID: {}, スレッド: {}", batchId, Thread.currentThread().getName());
            
            // 未処理のTaskデータを取得
            List<Task> unprocessedTasks = taskRepository.findByProcessedFalse();
            
            logger.info("処理対象データ数: {}", unprocessedTasks.size());
            
            // 長時間実行をシミュレート - 設定可能な時間での処理
            int totalIterations = batchProperties.getSimulationDurationSeconds();
            for (int i = 0; i < totalIterations; i++) {
                logger.debug("長時間処理実行中... {}/{} - バッチID: {}", (i + 1), totalIterations, batchId);
                
                // 1秒間の処理をシミュレート（中断可能）
                // Thread.sleep()はThread.interrupt()に自動応答するため、追加チェック不要
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Thread.sleep中に中断されました - バッチID: {}", batchId);
                    Thread.currentThread().interrupt(); // 中断状態を再設定
                    throw e;
                }
            }
            
            // 全てのTaskを処理済みに更新
            for (Task task : unprocessedTasks) {
                task.setProcessed(true);
                task.setProcessedAt(java.time.LocalDateTime.now());
            }
            taskRepository.saveAll(unprocessedTasks);
            
            logger.info("非同期業務処理完了 - バッチID: {}", batchId);
            
            // バッチ実行記録のステータスを更新
            updateBatchExecutionStatus(batchExecutionId, batchProperties.getStatus().getCompleted());
            
        } catch (Exception e) {
            logger.error("非同期業務処理でエラーが発生しました - バッチID: {}, エラー: {}", batchId, e.getMessage(), e);
            
            // バッチ実行記録を失敗状態に更新
            updateBatchExecutionStatus(batchExecutionId, batchProperties.getStatus().getFailed());
        } finally {
            // 停止フラグをクリア（必ず実行される）
            jobStopManager.clearStopFlag(jobExecutionId);
        }
        
        return CompletableFuture.completedFuture(null);
    }
}