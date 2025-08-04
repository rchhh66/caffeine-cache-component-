package com.caffeine.admin.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cache_monitor")
public class CacheMonitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cache_name", nullable = false, length = 100)
    private String cacheName;

    @Column(name = "access_count", nullable = false)
    private long accessCount;

    @Column(name = "hit_count", nullable = false)
    private long hitCount;

    @Column(name = "miss_count", nullable = false)
    private long missCount;

    @Column(name = "expire_count", nullable = false)
    private long expireCount;

    @Column(name = "eviction_count", nullable = false)
    private long evictionCount;

    @Column(name = "cache_size", nullable = false)
    private int cacheSize;

    @Column(name = "average_access_time", nullable = false)
    private double averageAccessTime;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    // 构造函数
    public CacheMonitor() {
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

    public long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(long accessCount) {
        this.accessCount = accessCount;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public long getExpireCount() {
        return expireCount;
    }

    public void setExpireCount(long expireCount) {
        this.expireCount = expireCount;
    }

    public long getEvictionCount() {
        return evictionCount;
    }

    public void setEvictionCount(long evictionCount) {
        this.evictionCount = evictionCount;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public double getAverageAccessTime() {
        return averageAccessTime;
    }

    public void setAverageAccessTime(double averageAccessTime) {
        this.averageAccessTime = averageAccessTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
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