package com.example.springbatch.config;

import com.example.springbatch.tasklet.GenerateBatchIdTasklet;
import com.example.springbatch.tasklet.LogStartTimeTasklet;
import com.example.springbatch.tasklet.AsyncBusinessJobTasklet;
import com.example.springbatch.tasklet.ReturnJobIdTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch設定クラス - 4ステップJob
 */
@Configuration
public class BatchConfig {

    @Autowired
    private GenerateBatchIdTasklet generateBatchIdTasklet;
    
    @Autowired
    private LogStartTimeTasklet logStartTimeTasklet;
    
    @Autowired
    @Qualifier("asyncBusinessJobTasklet")
    private Tasklet asyncBusinessJobTasklet;
    
    @Autowired
    private ReturnJobIdTasklet returnJobIdTasklet;

    /**
     * ステップ1: バッチIDを生成するStep
     */
    @Bean
    public Step generateBatchIdStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("generateBatchIdStep", jobRepository)
                .tasklet(generateBatchIdTasklet, transactionManager)
                .build();
    }

    /**
     * ステップ2: 開始時間を記録するStep
     */
    @Bean
    public Step logStartTimeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("logStartTimeStep", jobRepository)
                .tasklet(logStartTimeTasklet, transactionManager)
                .build();
    }

    /**
     * ステップ3: 非同期で業務Jobを実行するStep
     */
    @Bean
    public Step asyncBusinessJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("asyncBusinessJobStep", jobRepository)
                .tasklet(asyncBusinessJobTasklet, transactionManager)
                .build();
    }

    /**
     * ステップ4: JobIDを返すStep
     */
    @Bean
    public Step returnJobIdStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("returnJobIdStep", jobRepository)
                .tasklet(returnJobIdTasklet, transactionManager)
                .build();
    }

    /**
     * 4ステップからなるメインJob
     */
    @Bean
    public Job fourStepJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("fourStepJob", jobRepository)
                .start(generateBatchIdStep(jobRepository, transactionManager))
                .next(logStartTimeStep(jobRepository, transactionManager))
                .next(asyncBusinessJobStep(jobRepository, transactionManager))
                .next(returnJobIdStep(jobRepository, transactionManager))
                .build();
    }
}