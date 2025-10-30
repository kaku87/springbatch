package com.example.springbatch.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * サンプル実装。ビジネスロジックが必要な場合は、このクラスを継承もしくは
 * {@link AsyncBusinessJobTasklet} を継承した独自のTaskletを作成し、
 * {@link #doExecute(Long)} を実装する。
 */
@Component("asyncBusinessJobTasklet")
public class DefaultAsyncBusinessJobTasklet extends AsyncBusinessJobTasklet {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncBusinessJobTasklet.class);

    @Override
    protected void doExecute(Long jobExecutionId) {
        // 実際のビジネスロジックをここに実装する
        logger.info("デフォルト非同期タスク実行 - Job実行ID: {}", jobExecutionId);
    }
}
