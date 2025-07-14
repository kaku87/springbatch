package com.example.springbatch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * タスクエンティティクラス
 */
@Entity
@Table(name = "task")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "processed")
    private Boolean processed = false;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // デフォルトコンストラクタ
    public Task() {
    }
    
    public Task(String taskName, String description, Integer priority) {
        this.taskName = taskName;
        this.description = description;
        this.priority = priority;
        this.status = "PENDING";
    }
    
    // Getter and Setter methods
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getProcessed() {
        return processed;
    }
    
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status='" + status + '\'' +
                ", processed=" + processed +
                ", processedAt=" + processedAt +
                '}';
    }
}