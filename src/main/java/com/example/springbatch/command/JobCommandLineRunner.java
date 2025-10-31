package com.example.springbatch.command;

import com.example.springbatch.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * コマンドライン実行用のRunner
 */
@Component
public class JobCommandLineRunner implements CommandLineRunner {

    @Autowired
    private JobService jobService;

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "startJob".equals(args[0])) {
            System.out.println("コマンドラインからJobを開始します...");
            Long executionId = jobService.startJob();
            System.out.println("Job実行開始済み - JobExecutionId: " + executionId);
        }
    }
}
