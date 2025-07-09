package com.example.springbatch.command;

import com.example.springbatch.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * コマンドラインJob実行器
 * 使用方法：java -jar spring-batch-demo.jar --job.run=true
 */
@Component
public class JobCommandLineRunner implements CommandLineRunner {

    @Autowired
    private JobService jobService;

    @Override
    public void run(String... args) throws Exception {
        // job.runパラメータが含まれているかチェック
        boolean shouldRunJob = false;
        
        for (String arg : args) {
            if ("--job.run=true".equals(arg) || "--job.run".equals(arg)) {
                shouldRunJob = true;
                break;
            }
        }
        
        if (shouldRunJob) {
            System.out.println("=== コマンドラインからSpring Batch Jobを開始 ===");
            
            try {
                // 実行中のJobがあるかチェック
                if (jobService.hasRunningJobs()) {
                    System.out.println("警告：既にJobが実行中です、実行をスキップします");
                    return;
                }
                
                JobExecution jobExecution = jobService.startJobSync();
                
                System.out.println("Job実行完了！");
                System.out.println("実行ID: " + jobExecution.getId());
                System.out.println("ステータス: " + jobExecution.getStatus());
                System.out.println("開始時間: " + jobExecution.getStartTime());
                System.out.println("終了時間: " + jobExecution.getEndTime());
                System.out.println("終了コード: " + jobExecution.getExitStatus().getExitCode());
                
                if (jobExecution.getExitStatus().getExitDescription() != null) {
                    System.out.println("終了説明: " + jobExecution.getExitStatus().getExitDescription());
                }
                
            } catch (Exception e) {
                System.err.println("Job実行失敗: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}