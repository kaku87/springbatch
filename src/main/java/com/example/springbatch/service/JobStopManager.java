package com.example.springbatch.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Job停止マネージャー
 * 非同期タスクの停止状態と強制スレッド中断を管理
 */
@Component
public class JobStopManager {
    
    // 各Job実行IDの停止フラグを格納
    private final ConcurrentHashMap<Long, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();
    
    // 各Job実行IDに対応するスレッド参照を格納、強制中断用
    private final ConcurrentHashMap<Long, Thread> jobThreads = new ConcurrentHashMap<>();
    
    /**
     * Jobスレッドを登録
     * @param executionId Job実行ID
     * @param thread Jobを実行するスレッド
     */
    public void registerJobThread(Long executionId, Thread thread) {
        jobThreads.put(executionId, thread);
        System.out.println("Jobスレッドを登録 - Job実行ID: " + executionId + ", スレッド: " + thread.getName());
    }
    
    /**
     * Job停止フラグを設定し、スレッドを強制中断
     * @param executionId Job実行ID
     */
    public void setStopFlag(Long executionId) {
        stopFlags.put(executionId, new AtomicBoolean(true));
        
        // 対応するスレッドを強制中断
        Thread jobThread = jobThreads.get(executionId);
        if (jobThread != null && jobThread.isAlive()) {
            System.out.println("Jobスレッドを強制中断 - Job実行ID: " + executionId + ", スレッド: " + jobThread.getName());
            jobThread.interrupt();
        }
        
        System.out.println("停止フラグを設定 - Job実行ID: " + executionId);
    }
    
    /**
     * Jobが停止すべきかチェック
     * @param executionId Job実行ID
     * @return trueは停止すべき、falseは実行継続
     */
    public boolean shouldStop(Long executionId) {
        AtomicBoolean stopFlag = stopFlags.get(executionId);
        return stopFlag != null && stopFlag.get();
    }
    
    /**
     * Job停止フラグとスレッド参照をクリア
     * @param executionId Job実行ID
     */
    public void clearStopFlag(Long executionId) {
        stopFlags.remove(executionId);
        jobThreads.remove(executionId);
        System.out.println("停止フラグとスレッド参照をクリア - Job実行ID: " + executionId);
    }
    
    /**
     * 停止フラグが設定された全Job実行IDを取得
     * @return 停止フラグのJob実行ID集合
     */
    public java.util.Set<Long> getStoppedJobIds() {
        return stopFlags.keySet();
    }
}