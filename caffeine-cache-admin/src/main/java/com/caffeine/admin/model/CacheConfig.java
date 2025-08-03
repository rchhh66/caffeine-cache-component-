package com.caffeine.admin.model;

public class CacheConfig {
    private String cacheName;
    private int initialCapacity;
    private int maximumSize;
    private long expireTime;
    private String expirePolicy;
    private String keyStrategy;
    private boolean offHeap;
    private boolean persistent;
    private boolean asyncWarmup;
    private String persistentPath;
    private long persistentInterval;

    // 构造函数
    public CacheConfig() {
    }

    // Getters and Setters
    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getExpirePolicy() {
        return expirePolicy;
    }

    public void setExpirePolicy(String expirePolicy) {
        this.expirePolicy = expirePolicy;
    }

    public String getKeyStrategy() {
        return keyStrategy;
    }

    public void setKeyStrategy(String keyStrategy) {
        this.keyStrategy = keyStrategy;
    }

    public boolean isOffHeap() {
        return offHeap;
    }

    public void setOffHeap(boolean offHeap) {
        this.offHeap = offHeap;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public boolean isAsyncWarmup() {
        return asyncWarmup;
    }

    public void setAsyncWarmup(boolean asyncWarmup) {
        this.asyncWarmup = asyncWarmup;
    }

    public String getPersistentPath() {
        return persistentPath;
    }

    public void setPersistentPath(String persistentPath) {
        this.persistentPath = persistentPath;
    }

    public long getPersistentInterval() {
        return persistentInterval;
    }

    public void setPersistentInterval(long persistentInterval) {
        this.persistentInterval = persistentInterval;
    }
}