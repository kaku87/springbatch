package com.example.springbatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.step.tasklet.Tasklet;

import com.example.springbatch.tasklet.DefaultAsyncBusinessJobTasklet;

/**
 * Spring Batch設定クラス - 单个异步Step Job
 */
@Configuration
public class BatchConfig extends AbstractSingleTaskletJobConfiguration {

    @Override
    protected String jobName() {
        return "singleAsyncJob";
    }

    @Override
    protected String stepName() {
        return "singleAsyncStep";
    }

    @Override
    protected Class<? extends Tasklet> taskletClass() {
        return DefaultAsyncBusinessJobTasklet.class;
    }
}
