package com.example.springbatch.tasklet;

import com.example.springbatch.model.BatchExecution;
import com.example.springbatch.repository.BatchExecutionRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * ステップ4: JobIDを返すTasklet
 */
@Component
public class ReturnJobIdTasklet implements Tasklet {
    
    @Autowired
    private BatchExecutionRepository batchExecutionRepository;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // JobExecutionContextからバッチIDとJobIDを取得
        String batchId = (String) chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext().getString("batchId");
        
        Long batchExecutionId = (Long) chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext().getLong("batchExecutionId");
        
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getId();
        
        System.out.println("ステップ4: JobID返却 - バッチID: " + batchId + ", JobID: " + jobExecutionId);
        
        // バッチ実行記録を最終更新
        Optional<BatchExecution> optionalBatchExecution = batchExecutionRepository.findById(batchExecutionId);
        if (optionalBatchExecution.isPresent()) {
            BatchExecution batchExecution = optionalBatchExecution.get();
            
            // JobIDが未設定の場合は設定
            if (batchExecution.getJobId() == null) {
                batchExecution.setJobId(jobExecutionId);
            }
            
            // ステータスが「PROCESSING」の場合は「JOB_COMPLETED」に更新
            if ("PROCESSING".equals(batchExecution.getStatus())) {
                batchExecution.setStatus("JOB_COMPLETED");
            }
            
            batchExecutionRepository.save(batchExecution);
            
            System.out.println("JobID返却完了 - バッチID: " + batchId + ", JobID: " + jobExecutionId + ", ステータス: " + batchExecution.getStatus());
        } else {
            throw new RuntimeException("バッチ実行記録が見つかりません: ID=" + batchExecutionId);
        }
        
        // JobExecutionContextにJobIDを保存（他のコンポーネントから参照可能）
        chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext()
                .putLong("returnedJobId", jobExecutionId);
        
        System.out.println("=== 4ステップJob実行完了 ===");
        System.out.println("バッチID: " + batchId);
        System.out.println("JobID: " + jobExecutionId);
        System.out.println("非同期業務処理は継続中です");
        
        return RepeatStatus.FINISHED;
    }
}