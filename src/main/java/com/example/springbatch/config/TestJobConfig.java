package com.example.springbatch.config;

import com.example.springbatch.tasklet.TestJobTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Configuration;

/**
 * `testJob` の定義。`TestJobTasklet` を利用して単一ステップのジョブを構成する。
 */
@Configuration
public class TestJobConfig extends AbstractSingleTaskletJobConfiguration {

    @Override
    protected String jobName() {
        return "testJob";
    }

    @Override
    protected Class<? extends Tasklet> taskletClass() {
        return TestJobTasklet.class;
    }
}
