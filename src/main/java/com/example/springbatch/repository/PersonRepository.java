package com.example.springbatch.repository;

import com.example.springbatch.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 人員データアクセスインターフェース
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    /**
     * 未処理の人員を検索
     */
    List<Person> findByProcessedFalse();
    
    /**
     * 処理済みの人員を検索
     */
    List<Person> findByProcessedTrue();
    
    /**
     * 未処理の人員数を集計
     */
    @Query("SELECT COUNT(p) FROM Person p WHERE p.processed = false")
    long countUnprocessed();
    
    /**
     * 処理済みの人員数を集計
     */
    @Query("SELECT COUNT(p) FROM Person p WHERE p.processed = true")
    long countProcessed();
}