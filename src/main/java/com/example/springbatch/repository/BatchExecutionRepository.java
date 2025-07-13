package com.example.springbatch.repository;

import com.example.springbatch.model.BatchExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * バッチ実行記録データアクセスインターフェース
 */
@Repository
public interface BatchExecutionRepository extends JpaRepository<BatchExecution, Long> {
    
    /**
     * バッチIDでバッチ実行記録を検索
     */
    Optional<BatchExecution> findByBatchId(String batchId);
    
    /**
     * JobIDでバッチ実行記録を検索
     */
    Optional<BatchExecution> findByJobId(Long jobId);
    
    /**
     * ステータスでバッチ実行記録を検索
     */
    java.util.List<BatchExecution> findByStatus(String status);
}