package com.example.springbatch.tasklet;

import com.example.springbatch.model.BatchExecution;
import com.example.springbatch.model.Person;
import com.example.springbatch.repository.BatchExecutionRepository;
import com.example.springbatch.repository.PersonRepository;
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
    private PersonRepository personRepository;
    
    @Autowired
    private JobStopManager jobStopManager;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // JobExecutionContextからバッチIDを取得
        String batchId = (String) chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext().getString("batchId");
        
        Long batchExecutionId = (Long) chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext().getLong("batchExecutionId");
        
        System.out.println("ステップ3: 非同期業務Job実行開始 - バッチID: " + batchId);
        
        // バッチ実行記録を更新
        Optional<BatchExecution> optionalBatchExecution = batchExecutionRepository.findById(batchExecutionId);
        if (optionalBatchExecution.isPresent()) {
            BatchExecution batchExecution = optionalBatchExecution.get();
            batchExecution.setStatus("PROCESSING");
            batchExecutionRepository.save(batchExecution);
        }
        
        // 获取Job执行ID
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        
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
            // 注册当前线程到JobStopManager，以便支持强制中断
            jobStopManager.registerJobThread(jobExecutionId, Thread.currentThread());
            
            System.out.println("非同期業務処理開始 - バッチID: " + batchId + ", 线程: " + Thread.currentThread().getName());
            
            // 未処理のPersonデータを取得
            List<Person> unprocessedPersons = personRepository.findByProcessedFalse();
            
            System.out.println("処理対象データ数: " + unprocessedPersons.size());
            
            // 長時間実行をシミュレート - 10分間の処理
            int totalIterations = 600; // 10分 = 600秒
            for (int i = 0; i < totalIterations; i++) {
                System.out.println("長時間処理実行中... " + (i + 1) + "/" + totalIterations + " - バッチID: " + batchId);
                
                // 1秒間の処理をシミュレート（中断可能）
                // Thread.sleep()会自动响应Thread.interrupt()，无需额外检查
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Thread.sleep中に中断されました - バッチID: " + batchId);
                    Thread.currentThread().interrupt(); // 中断状态重新设置
                    throw e;
                }
            }
            
            // 実際のデータ処理（短縮版）
            for (Person person : unprocessedPersons) {
                // 只在数据处理开始时检查一次中断状态
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("データ処理が中断されました - バッチID: " + batchId);
                    throw new InterruptedException("データ処理が中断されました");
                }
                
                // 処理ロジックをシミュレート：名前を大文字に変換
                String firstName = person.getFirstName().toUpperCase();
                String lastName = person.getLastName().toUpperCase();
                
                person.setFirstName(firstName);
                person.setLastName(lastName);
                person.setProcessed(true);
                
                personRepository.save(person);
                
                System.out.println("データ処理完了: " + person);
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
            
            // 清除停止标志
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
            
            // 清除停止标志
            jobStopManager.clearStopFlag(jobExecutionId);
            
            throw new RuntimeException("非同期業務処理失敗", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
}