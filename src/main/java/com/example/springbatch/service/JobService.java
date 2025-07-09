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
    private Job processPersonJob;

    /**
     * 非同期でJobを開始
     */
    @Async
    public CompletableFuture<JobExecution> startJobAsync() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(processPersonJob, jobParameters);
            return CompletableFuture.completedFuture(jobExecution);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | 
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            throw new RuntimeException("Job開始失敗: " + e.getMessage(), e);
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
            
            return jobLauncher.run(processPersonJob, jobParameters);
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
            return jobOperator.stop(executionId);
        } catch (Exception e) {
            System.err.println("Job停止失敗: " + e.getMessage());
            return false;
        }
    }

    /**
     * 全てのJob実行記録を取得
     */
    public List<JobExecution> getAllJobExecutions() {
        List<JobExecution> allExecutions = new ArrayList<>();
        
        // 全てのJobインスタンスを取得
        List<JobInstance> jobInstances = jobExplorer.getJobInstances("processPersonJob", 0, 100);
        
        for (JobInstance jobInstance : jobInstances) {
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
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("processPersonJob");
        return new ArrayList<>(runningExecutions);
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