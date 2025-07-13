package com.example.springbatch.tasklet;

import com.example.springbatch.model.BatchExecution;
import com.example.springbatch.model.Task;
import com.example.springbatch.repository.BatchExecutionRepository;
import com.example.springbatch.repository.TaskRepository;
import com.example.springbatch.service.JobStopManager;
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
    
    @Autowired
    private BatchExecutionRepository batchExecutionRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private JobStopManager jobStopManager;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Job実行IDを取得
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        
        // バッチIDを生成（Job実行IDをバッチIDとして使用）
        String batchId = "BATCH_" + jobExecutionId + "_" + System.currentTimeMillis();
        
        System.out.println("単一非同期Job実行開始 - バッチID: " + batchId + ", Job実行ID: " + jobExecutionId);
        
        // バッチ実行記録を作成
        BatchExecution batchExecution = new BatchExecution();
        batchExecution.setBatchId(batchId);
        batchExecution.setJobId(jobExecutionId);
        batchExecution.setStatus("PROCESSING");
        batchExecution.setStartTime(java.time.LocalDateTime.now());
        batchExecution = batchExecutionRepository.save(batchExecution);
        
        Long batchExecutionId = batchExecution.getId();
        
        // 非同期で業務処理を実行
        CompletableFuture<Void> future = executeBusinessLogicAsync(batchId, batchExecutionId, jobExecutionId);
        
        // 非同期処理の開始を確認（実際の完了は待たない）
        System.out.println("非同期業務Job実行開始完了 - バッチID: " + batchId);
        
        // 注意: CompletableFutureは序列化できないため、JobExecutionContextには保存しない
        
        return RepeatStatus.FINISHED;
    }
    
    /**
     * 非同期で業務ロジックを実行
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeBusinessLogicAsync(String batchId, Long batchExecutionId, Long jobExecutionId) {
        try {
            // 現在のスレッドをJobStopManagerに登録し、強制中断をサポート
            jobStopManager.registerJobThread(jobExecutionId, Thread.currentThread());
            
            System.out.println("非同期業務処理開始 - バッチID: " + batchId + ", スレッド: " + Thread.currentThread().getName());
            
            // 未処理のTaskデータを取得
            List<Task> unprocessedTasks = taskRepository.findByProcessedFalse();
            
            System.out.println("処理対象データ数: " + unprocessedTasks.size());
            
            // 長時間実行をシミュレート - 10分間の処理
            int totalIterations = 600; // 10分 = 600秒
            for (int i = 0; i < totalIterations; i++) {
                System.out.println("長時間処理実行中... " + (i + 1) + "/" + totalIterations + " - バッチID: " + batchId);
                
                // 1秒間の処理をシミュレート（中断可能）
                // Thread.sleep()はThread.interrupt()に自動応答するため、追加チェック不要
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Thread.sleep中に中断されました - バッチID: " + batchId);
                    Thread.currentThread().interrupt(); // 中断状態を再設定
                    throw e;
                }
            }
            
            // 実際のデータ処理（短縮版）
            for (Task task : unprocessedTasks) {
                // データ処理開始時に一度だけ中断状態をチェック
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("データ処理が中断されました - バッチID: " + batchId);
                    throw new InterruptedException("データ処理が中断されました");
                }
                
                // タスクステータスを更新（業務処理をシミュレート）
                task.setStatus("PROCESSING");
                task.setTaskName(task.getTaskName().toUpperCase());
                
                // 処理済みとしてマーク
                task.setProcessed(true);
                task.setStatus("COMPLETED");
                
                // データベースに保存
                taskRepository.save(task);
                
                System.out.println("データ処理完了: " + task);
            }
            
            // バッチ実行記録を完了状態に更新
            Optional<BatchExecution> optionalBatchExecution = batchExecutionRepository.findById(batchExecutionId);
            if (optionalBatchExecution.isPresent()) {
                BatchExecution batchExecution = optionalBatchExecution.get();
                batchExecution.setStatus("COMPLETED");
                batchExecution.setEndTime(java.time.LocalDateTime.now());
                batchExecutionRepository.save(batchExecution);
            }
            
            System.out.println("非同期業務処理完了 - バッチID: " + batchId);
            
            // 停止フラグをクリア
            jobStopManager.clearStopFlag(jobExecutionId);
            
        } catch (Exception e) {
            System.err.println("非同期業務処理エラー - バッチID: " + batchId + ", エラー: " + e.getMessage());
            
            // エラー時にバッチ実行記録を更新
            Optional<BatchExecution> optionalBatchExecution = batchExecutionRepository.findById(batchExecutionId);
            if (optionalBatchExecution.isPresent()) {
                BatchExecution batchExecution = optionalBatchExecution.get();
                batchExecution.setStatus("FAILED");
                batchExecution.setEndTime(java.time.LocalDateTime.now());
                batchExecutionRepository.save(batchExecution);
            }
            
            // 停止フラグをクリア
            jobStopManager.clearStopFlag(jobExecutionId);
            
            throw new RuntimeException("非同期業務処理失敗", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
}