package com.example.springbatch.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Job停止管理器
 * 用于管理异步任务的停止状态和强制线程中断
 */
@Component
public class JobStopManager {
    
    // 存储每个Job执行ID的停止标志
    private final ConcurrentHashMap<Long, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();
    
    // 存储每个Job执行ID对应的线程引用，用于强制中断
    private final ConcurrentHashMap<Long, Thread> jobThreads = new ConcurrentHashMap<>();
    
    /**
     * 注册Job线程
     * @param executionId Job执行ID
     * @param thread 执行Job的线程
     */
    public void registerJobThread(Long executionId, Thread thread) {
        jobThreads.put(executionId, thread);
        System.out.println("注册Job线程 - Job执行ID: " + executionId + ", 线程: " + thread.getName());
    }
    
    /**
     * 设置Job停止标志并强制中断线程
     * @param executionId Job执行ID
     */
    public void setStopFlag(Long executionId) {
        stopFlags.put(executionId, new AtomicBoolean(true));
        
        // 强制中断对应的线程
        Thread jobThread = jobThreads.get(executionId);
        if (jobThread != null && jobThread.isAlive()) {
            System.out.println("强制中断Job线程 - Job执行ID: " + executionId + ", 线程: " + jobThread.getName());
            jobThread.interrupt();
        }
        
        System.out.println("设置停止标志 - Job执行ID: " + executionId);
    }
    
    /**
     * 检查Job是否应该停止
     * @param executionId Job执行ID
     * @return true表示应该停止，false表示继续执行
     */
    public boolean shouldStop(Long executionId) {
        AtomicBoolean stopFlag = stopFlags.get(executionId);
        return stopFlag != null && stopFlag.get();
    }
    
    /**
     * 清除Job停止标志和线程引用
     * @param executionId Job执行ID
     */
    public void clearStopFlag(Long executionId) {
        stopFlags.remove(executionId);
        jobThreads.remove(executionId);
        System.out.println("清除停止标志和线程引用 - Job执行ID: " + executionId);
    }
    
    /**
     * 获取所有停止标志的Job执行ID
     * @return 停止标志的Job执行ID集合
     */
    public java.util.Set<Long> getStoppedJobIds() {
        return stopFlags.keySet();
    }
}