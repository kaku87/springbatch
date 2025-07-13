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
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ステップ1: バッチIDを生成するTasklet
 */
@Component
public class GenerateBatchIdTasklet implements Tasklet {
    
    @Autowired
    private BatchExecutionRepository batchExecutionRepository;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // バッチIDを生成（タイムスタンプ + UUID）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String batchId = "BATCH_" + timestamp + "_" + uuid;
        
        System.out.println("ステップ1: バッチID生成 - " + batchId);
        
        // バッチ実行記録を作成
        BatchExecution batchExecution = new BatchExecution(batchId);
        batchExecution = batchExecutionRepository.save(batchExecution);
        
        // JobExecutionContextにバッチIDを保存
        chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext()
                .putString("batchId", batchId);
        
        chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext()
                .putLong("batchExecutionId", batchExecution.getId());
        
        System.out.println("バッチID生成完了: " + batchId);
        
        return RepeatStatus.FINISHED;
    }
}