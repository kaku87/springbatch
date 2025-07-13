package com.example.springbatch.config;

import com.example.springbatch.tasklet.AsyncBusinessJobTasklet;
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
 * Spring Batch設定クラス - 单个异步Step Job
 */
@Configuration
public class BatchConfig {

    @Autowired
    @Qualifier("asyncBusinessJobTasklet")
    private Tasklet asyncBusinessJobTasklet;

    /**
     * 单个异步业务处理Step
     */
    @Bean
    public Step singleAsyncStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("singleAsyncStep", jobRepository)
                .tasklet(asyncBusinessJobTasklet, transactionManager)
                .build();
    }

    /**
     * 只包含一个异步Step的Job
     */
    @Bean
    public Job singleAsyncJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("singleAsyncJob", jobRepository)
                .start(singleAsyncStep(jobRepository, transactionManager))
                .build();
    }


}