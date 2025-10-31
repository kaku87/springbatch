package com.example.springbatch.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 簡単なデモ用Tasklet。非同期で短い処理を行いログを出力する。
 */
@Component
public class TestJobTasklet extends AsyncBusinessJobTasklet {

    private static final Logger logger = LoggerFactory.getLogger(TestJobTasklet.class);

    @Override
    protected void doExecute(Long jobExecutionId) throws Exception {
        logger.info("testJob 処理開始 - JobExecutionId: {}", jobExecutionId);

        for (int i = 1; i <= 5; i++) {
            logger.info("testJob 処理ステップ {}/5", i);
            Thread.sleep(500);
        }

        logger.info("testJob 処理完了 - JobExecutionId: {}", jobExecutionId);
    }
}
