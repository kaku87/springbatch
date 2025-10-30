package com.example.springbatch.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * サンプルのカスタムJob実装。
 * <p>
 * {@link AsyncBusinessJobTasklet} を継承し、非同期で動作する業務ロジックを記述する。
 * この例ではレポート生成処理を模した単純なループを実行する。
 */
@Component("customReportTasklet")
public class CustomReportTasklet extends AsyncBusinessJobTasklet {

    private static final Logger logger = LoggerFactory.getLogger(CustomReportTasklet.class);

    @Override
    protected void doExecute(Long jobExecutionId) throws Exception {
        logger.info("カスタムレポート生成開始 - Job実行ID: {}", jobExecutionId);

        int totalSections = 3;
        for (int section = 1; section <= totalSections; section++) {
            logger.info("レポートセクション処理中 {}/{}", section, totalSections);
            Thread.sleep(1000); // 実際の処理に置き換える
        }

        logger.info("カスタムレポート生成完了 - Job実行ID: {}", jobExecutionId);
    }
}
