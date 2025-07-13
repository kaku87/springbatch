package com.example.springbatch.command;

import com.example.springbatch.service.JobService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * コマンドライン実行用のRunner
 */
@Component
public class JobCommandLineRunner implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job fourStepJob;
    
    @Autowired
    private JobService jobService;

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "startJob".equals(args[0])) {
            System.out.println("コマンドラインからJobを開始します...");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(fourStepJob, jobParameters);
            
            System.out.println("Job実行完了: " + jobExecution.getStatus());
        }
    }
}