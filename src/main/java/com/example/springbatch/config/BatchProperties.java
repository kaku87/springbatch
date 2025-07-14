package com.example.springbatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * バッチ処理関連の設定プロパティ
 */
@Component
@ConfigurationProperties(prefix = "app.batch")
public class BatchProperties {
    
    /**
     * 業務処理シミュレーション時間（秒）
     */
    private int simulationDurationSeconds = 600;
    
    /**
     * バッチ処理ステータス定義
     */
    private Status status = new Status();
    
    public int getSimulationDurationSeconds() {
        return simulationDurationSeconds;
    }
    
    public void setSimulationDurationSeconds(int simulationDurationSeconds) {
        this.simulationDurationSeconds = simulationDurationSeconds;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * ステータス定義クラス
     */
    public static class Status {
        private String processing = "PROCESSING";
        private String completed = "COMPLETED";
        private String failed = "FAILED";
        
        public String getProcessing() {
            return processing;
        }
        
        public void setProcessing(String processing) {
            this.processing = processing;
        }
        
        public String getCompleted() {
            return completed;
        }
        
        public void setCompleted(String completed) {
            this.completed = completed;
        }
        
        public String getFailed() {
            return failed;
        }
        
        public void setFailed(String failed) {
            this.failed = failed;
        }
    }
}