package com.example.springbatch.service;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
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
    private JobRegistry jobRegistry;
    
    @Autowired
    private JobStopManager jobStopManager;



    /**
     * Jobを開始
     */
    public Long startJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        return startJob("singleAsyncJob");
    }

    public Long startJob(String jobName) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        Job job = getJob(jobName);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        return jobExecution.getId();
    }

    /**
     * 非同期でJobを開始
     */
    @Async
    public CompletableFuture<Long> startJobAsync() {
        return startJobAsync("singleAsyncJob");
    }

    @Async
    public CompletableFuture<Long> startJobAsync(String jobName) {
        try {
            Job job = getJob(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            return CompletableFuture.completedFuture(jobExecution.getId());
        } catch (Exception e) {
            throw new RuntimeException("Job実行エラー", e);
        }
    }

    /**
     * 同期でJobを開始（コマンドライン実行用）
     */
    public JobExecution startJobSync() {
        return startJobSync("singleAsyncJob");
    }

    public JobExecution startJobSync(String jobName) {
        try {
            Job job = getJob(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();
            
            return jobLauncher.run(job, jobParameters);
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
            // まずJobが存在するかチェック
            JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
            if (jobExecution == null) {
                System.err.println("Job停止失敗: Job执行记录不存在 - 执行ID: " + executionId);
                return false;
            }
            
            // Jobが実行中かチェック
            if (!jobExecution.getStatus().isRunning()) {
                System.out.println("Job已经停止或完成 - 执行ID: " + executionId + ", 状态: " + jobExecution.getStatus());
                return true; // 既に停止したJobは停止成功とみなす
            }
            
            // 停止フラグを設定し、非同期タスクを停止
            jobStopManager.setStopFlag(executionId);
            
            // Spring Batchの停止メソッドを呼び出し
            boolean stopped = jobOperator.stop(executionId);
            
            System.out.println("Job停止请求处理完成 - 执行ID: " + executionId + ", Spring Batch停止结果: " + stopped);
            
            // Spring Batchがfalseを返しても、停止フラグが設定されれば停止リクエスト成功とみなす
            return true;
        } catch (Exception e) {
            System.err.println("Job停止失敗: " + e.getMessage());
            e.printStackTrace();
            // Spring Batchの停止が失敗しても、停止フラグを設定
            jobStopManager.setStopFlag(executionId);
            // 停止フラグ設定後は停止リクエスト成功とみなす
            return true;
        }
    }

    /**
     * 全てのJob実行記録を取得
     */
    public List<JobExecution> getAllJobExecutions() {
        List<JobExecution> allExecutions = new ArrayList<>();

        for (String jobName : jobExplorer.getJobNames()) {
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 100);
            for (JobInstance jobInstance : jobInstances) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
                allExecutions.addAll(executions);
            }
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

        for (String jobName : jobExplorer.getJobNames()) {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions(jobName);
            allRunningExecutions.addAll(runningExecutions);
        }

        return allRunningExecutions;
    }

    public List<JobExecution> getRunningJobExecutions(String jobName) {
        return new ArrayList<>(jobExplorer.findRunningJobExecutions(jobName));
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

    public boolean hasRunningJobs(String jobName) {
        return !jobExplorer.findRunningJobExecutions(jobName).isEmpty();
    }

    private Job getJob(String jobName) {
        try {
            return jobRegistry.getJob(jobName);
        } catch (Exception e) {
            throw new IllegalArgumentException("指定されたJobが存在しません: " + jobName, e);
        }
    }
}
