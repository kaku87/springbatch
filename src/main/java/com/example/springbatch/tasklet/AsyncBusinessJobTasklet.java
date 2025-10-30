package com.example.springbatch.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

/**
 * 非同期バッチ処理Taskletの標準テンプレート。
 * <p>
 * サブクラスは {@link #doExecute(Long)} を実装し、ビジネスロジックのみを記述する。
 */
public abstract class AsyncBusinessJobTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(AsyncBusinessJobTasklet.class);
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Job実行IDを取得
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        
        logger.info("非同期Job実行開始 - Job実行ID: {}", jobExecutionId);

        // 非同期で業務処理を実行
        executeBusinessLogicAsync(jobExecutionId);

        // 非同期処理の開始を確認（実際の完了は待たない）
        logger.info("非同期業務Job実行開始完了 - Job実行ID: {}", jobExecutionId);

        return RepeatStatus.FINISHED;
    }

    /**
     * 非同期で業務ロジックを実行
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeBusinessLogicAsync(Long jobExecutionId) {
        try {
            logger.info("非同期業務処理開始 - Job実行ID: {}, スレッド: {}", jobExecutionId, Thread.currentThread().getName());

            doExecute(jobExecutionId);

            logger.info("非同期業務処理完了 - Job実行ID: {}", jobExecutionId);
            onExecutionSuccess(jobExecutionId);
        } catch (Exception e) {
            logger.error("非同期業務処理でエラーが発生しました - Job実行ID: {}", jobExecutionId, e);
            onExecutionError(jobExecutionId, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * サブクラスで実装する業務ロジック。
     *
     * @param jobExecutionId ジョブ実行ID
     */
    protected abstract void doExecute(Long jobExecutionId) throws Exception;

    /**
     * 正常終了時のフック。必要な場合にサブクラスでオーバーライドする。
     */
    protected void onExecutionSuccess(Long jobExecutionId) {
        // default no-op
    }

    /**
     * 異常終了時のフック。必要な場合にサブクラスでオーバーライドする。
     */
    protected void onExecutionError(Long jobExecutionId, Exception exception) {
        // default no-op
    }
}
