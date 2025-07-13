package com.example.springbatch.service;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Job管理サービスクラス
 */
@Service
public class JobService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job fourStepJob;
    
    @Autowired
    private Job singleAsyncJob;
    
    @Autowired
    private JobStopManager jobStopManager;

    /**
     * Jobを開始
     */
    public Long startJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncher.run(fourStepJob, jobParameters);
        return jobExecution.getId();
    }

    /**
     * 非同期でJobを開始
     */
    @Async
    public CompletableFuture<Long> startJobAsync() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(fourStepJob, jobParameters);
            return CompletableFuture.completedFuture(jobExecution.getId());
        } catch (Exception e) {
            throw new RuntimeException("Job実行エラー", e);
        }
    }

    /**
     * 启动单个异步Step的Job
     */
    public Long startSingleAsyncJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        
        JobExecution jobExecution = jobLauncher.run(singleAsyncJob, jobParameters);
        return jobExecution.getId();
    }

    /**
     * 非同期で单个异步Step的Jobを開始
     */
    @Async
    public CompletableFuture<Long> startSingleAsyncJobAsync() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(singleAsyncJob, jobParameters);
            return CompletableFuture.completedFuture(jobExecution.getId());
        } catch (Exception e) {
            throw new RuntimeException("单个异步Job実行エラー", e);
        }
    }

    /**
     * 同期でJobを開始（コマンドライン実行用）
     */
    public JobExecution startJobSync() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();
            
            return jobLauncher.run(fourStepJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | 
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            throw new RuntimeException("Job開始失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 実行中のJobを停止
     */
    public boolean stopJob(Long executionId) {
        try {
            // 设置停止标志，用于停止异步任务
            jobStopManager.setStopFlag(executionId);
            
            // 调用Spring Batch的停止方法
            boolean stopped = jobOperator.stop(executionId);
            
            System.out.println("Job停止请求处理完成 - 执行ID: " + executionId + ", 结果: " + stopped);
            return stopped;
        } catch (Exception e) {
            System.err.println("Job停止失敗: " + e.getMessage());
            // 即使Spring Batch停止失败，也要设置停止标志
            jobStopManager.setStopFlag(executionId);
            return false;
        }
    }

    /**
     * 全てのJob実行記録を取得
     */
    public List<JobExecution> getAllJobExecutions() {
        List<JobExecution> allExecutions = new ArrayList<>();
        
        // fourStepJobのJobインスタンスを取得
        List<JobInstance> fourStepJobInstances = jobExplorer.getJobInstances("fourStepJob", 0, 100);
        for (JobInstance jobInstance : fourStepJobInstances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
            allExecutions.addAll(executions);
        }
        
        // singleAsyncJobのJobインスタンスを取得
        List<JobInstance> singleAsyncJobInstances = jobExplorer.getJobInstances("singleAsyncJob", 0, 100);
        for (JobInstance jobInstance : singleAsyncJobInstances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
            allExecutions.addAll(executions);
        }
        
        // 開始時間の降順でソート
        allExecutions.sort((e1, e2) -> {
            if (e1.getStartTime() == null && e2.getStartTime() == null) return 0;
            if (e1.getStartTime() == null) return 1;
            if (e2.getStartTime() == null) return -1;
            return e2.getStartTime().compareTo(e1.getStartTime());
        });
        
        return allExecutions;
    }

    /**
     * 実行中のJob実行記録を取得
     */
    public List<JobExecution> getRunningJobExecutions() {
        List<JobExecution> allRunningExecutions = new ArrayList<>();
        
        // fourStepJobの実行中Job
        Set<JobExecution> fourStepRunningExecutions = jobExplorer.findRunningJobExecutions("fourStepJob");
        allRunningExecutions.addAll(fourStepRunningExecutions);
        
        // singleAsyncJobの実行中Job
        Set<JobExecution> singleAsyncRunningExecutions = jobExplorer.findRunningJobExecutions("singleAsyncJob");
        allRunningExecutions.addAll(singleAsyncRunningExecutions);
        
        return allRunningExecutions;
    }

    /**
     * IDによりJob実行記録を取得
     */
    public JobExecution getJobExecution(Long executionId) {
        return jobExplorer.getJobExecution(executionId);
    }

    /**
     * 実行中のJobがあるかチェック
     */
    public boolean hasRunningJobs() {
        return !getRunningJobExecutions().isEmpty();
    }
}