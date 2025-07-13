package com.example.springbatch.tasklet;

import com.example.springbatch.model.BatchExecution;
import com.example.springbatch.repository.BatchExecutionRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ステップ2: 開始時間を記録するTasklet
 */
@Component
public class LogStartTimeTasklet implements Tasklet {
    
    @Autowired
    private BatchExecutionRepository batchExecutionRepository;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // JobExecutionContextからバッチIDを取得
        String batchId = (String) chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext().getString("batchId");
        
        Long batchExecutionId = (Long) chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext().getLong("batchExecutionId");
        
        System.out.println("ステップ2: 開始時間記録 - バッチID: " + batchId);
        
        // バッチ実行記録を更新
        Optional<BatchExecution> optionalBatchExecution = batchExecutionRepository.findById(batchExecutionId);
        if (optionalBatchExecution.isPresent()) {
            BatchExecution batchExecution = optionalBatchExecution.get();
            batchExecution.setStartTime(LocalDateTime.now());
            batchExecution.setStatus("STARTED");
            
            // JobIDを設定
            Long jobExecutionId = chunkContext.getStepContext().getStepExecution()
                    .getJobExecution().getId();
            batchExecution.setJobId(jobExecutionId);
            
            batchExecutionRepository.save(batchExecution);
            
            System.out.println("開始時間記録完了: " + batchExecution.getStartTime() + ", JobID: " + jobExecutionId);
        } else {
            throw new RuntimeException("バッチ実行記録が見つかりません: ID=" + batchExecutionId);
        }
        
        return RepeatStatus.FINISHED;
    }
}