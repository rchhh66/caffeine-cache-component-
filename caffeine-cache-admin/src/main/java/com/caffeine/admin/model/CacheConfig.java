package com.caffeine.admin.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cache_config")
public class CacheConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cache_name", unique = true, nullable = false, length = 100)
    private String cacheName;

    @Column(name = "initial_capacity", nullable = false)
    private int initialCapacity;

    @Column(name = "maximum_size", nullable = false)
    private int maximumSize;

    @Column(name = "expire_time", nullable = false)
    private long expireTime;

    @Column(name = "expire_policy", nullable = false, length = 50)
    private String expirePolicy;

    @Column(name = "key_strategy", nullable = false, length = 50)
    private String keyStrategy;

    @Column(name = "off_heap", nullable = false)
    private boolean offHeap;

    @Column(name = "persistent", nullable = false)
    private boolean persistent;

    @Column(name = "async_warmup", nullable = false)
    private boolean asyncWarmup;

    @Column(name = "persistent_path", length = 255)
    private String persistentPath;

    @Column(name = "persistent_interval", nullable = false)
    private long persistentInterval;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    // 构造函数
    public CacheConfig() {
    }

    // 在实体持久化前设置创建时间和更新时间
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    // 在实体更新前设置更新时间
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}