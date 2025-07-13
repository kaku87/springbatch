package com.example.springbatch.controller;

import com.example.springbatch.model.Person;
import com.example.springbatch.repository.PersonRepository;
import com.example.springbatch.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Job管理コントローラー
 */
@Controller
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private PersonRepository personRepository;

    /**
     * Job管理ページを表示
     */
    @GetMapping("/")
    public String index(Model model) {
        // 全てのJob実行記録を取得
        List<JobExecution> jobExecutions = jobService.getAllJobExecutions();
        model.addAttribute("jobExecutions", jobExecutions);
        
        // 実行中のJobを取得
        List<JobExecution> runningJobs = jobService.getRunningJobExecutions();
        model.addAttribute("runningJobs", runningJobs);
        
        // データ統計を取得
        long totalCount = personRepository.count();
        long processedCount = personRepository.countProcessed();
        long unprocessedCount = personRepository.countUnprocessed();
        
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("processedCount", processedCount);
        model.addAttribute("unprocessedCount", unprocessedCount);
        
        return "index";
    }

    /**
     * 非同期でJobを開始
     */
    @PostMapping("/api/jobs/start")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startJob() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 実行中のJobがあるかチェック
            if (jobService.hasRunningJobs()) {
                response.put("success", false);
                response.put("message", "既にJobが実行中です。完了後に新しいJobを開始してください");
                return ResponseEntity.badRequest().body(response);
            }
            
            CompletableFuture<Long> future = jobService.startJobAsync();
            Long executionId = future.get();
            
            response.put("success", true);
            response.put("message", "Job開始成功");
            response.put("executionId", executionId);
            response.put("status", "STARTED");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Job開始失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Jobを停止
     */
    @PostMapping("/api/jobs/{executionId}/stop")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> stopJob(@PathVariable Long executionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean stopped = jobService.stopJob(executionId);
            
            if (stopped) {
                response.put("success", true);
                response.put("message", "Job停止リクエストが送信されました");
            } else {
                response.put("success", false);
                response.put("message", "Job停止失敗");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Job停止失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Jobステータスを取得
     */
    @GetMapping("/api/jobs/{executionId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long executionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            JobExecution jobExecution = jobService.getJobExecution(executionId);
            
            if (jobExecution != null) {
                response.put("success", true);
                response.put("executionId", jobExecution.getId());
                response.put("status", jobExecution.getStatus().toString());
                response.put("startTime", jobExecution.getStartTime());
                response.put("endTime", jobExecution.getEndTime());
                response.put("exitCode", jobExecution.getExitStatus().getExitCode());
                response.put("exitDescription", jobExecution.getExitStatus().getExitDescription());
            } else {
                response.put("success", false);
                response.put("message", "指定されたJob実行記録が見つかりません");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Jobステータス取得失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * テストデータを初期化
     */
    @PostMapping("/api/data/init")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> initTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 既存データをクリア
            personRepository.deleteAll();
            
            // テストデータを作成
            for (int i = 1; i <= 20; i++) {
                Person person = new Person(
                    "FirstName" + i,
                    "LastName" + i,
                    "user" + i + "@example.com"
                );
                personRepository.save(person);
            }
            
            response.put("success", true);
            response.put("message", "テストデータ初期化成功、20件のレコードを作成しました");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "テストデータ初期化失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}