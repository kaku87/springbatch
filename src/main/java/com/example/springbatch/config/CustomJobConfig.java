package com.example.springbatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.step.tasklet.Tasklet;

import com.example.springbatch.tasklet.CustomReportTasklet;

/**
 * カスタムJob定義クラスのサンプル。
 * <p>
 * {@link CustomReportTasklet} を利用して非同期レポート生成ジョブを構成する。
 * 独自のジョブを追加したい場合は、このクラスを参考に新しい設定クラスを作成する。
 */
@Configuration
public class CustomJobConfig extends AbstractSingleTaskletJobConfiguration {

    @Override
    protected String jobName() {
        return "customReportJob";
    }

    @Override
    protected Class<? extends Tasklet> taskletClass() {
        return CustomReportTasklet.class;
    }
}
