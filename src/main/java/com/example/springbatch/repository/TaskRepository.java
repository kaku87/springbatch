package com.example.springbatch.repository;

import com.example.springbatch.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * タスクデータアクセスインターフェース
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * 未処理のタスクを検索
     */
    List<Task> findByProcessedFalse();
    
    /**
     * 処理済みのタスクを検索
     */
    List<Task> findByProcessedTrue();
    
    /**
     * 未処理のタスク数を集計
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.processed = false")
    long countUnprocessed();
    
    /**
     * 処理済みのタスク数を集計
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.processed = true")
    long countProcessed();
    
    /**
     * 優先度別にタスクを検索
     */
    List<Task> findByPriorityOrderByIdAsc(Integer priority);
    
    /**
     * ステータス別にタスクを検索
     */
    List<Task> findByStatus(String status);
}