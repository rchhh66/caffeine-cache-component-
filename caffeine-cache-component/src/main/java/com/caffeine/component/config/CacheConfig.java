package com.caffeine.component.config;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 缓存配置类
 */
public class CacheConfig {
    // 默认配置
    private static final long DEFAULT_MAXIMUM_SIZE = 10000;
    private static final long DEFAULT_EXPIRE_AFTER_WRITE = 3600;
    private static final boolean DEFAULT_OFF_HEAP_CACHE_ENABLED = false;
    private static final boolean DEFAULT_PERSISTENCE_ENABLED = false;
    private static final boolean DEFAULT_AUTO_PERSISTENCE_ENABLED = false;
    private static final boolean DEFAULT_ASYNC_WARMUP_ENABLED = false;
    private static final String DEFAULT_PERSISTENCE_PATH = "./cache_data";
    private static final long DEFAULT_PERSISTENCE_INTERVAL = 3600;

    // 配置项
    private long maximumSize;
    private long expireAfterWrite;
    private boolean offHeapCacheEnabled;
    private boolean persistenceEnabled;
    private boolean autoPersistenceEnabled;
    private boolean asyncWarmupEnabled;
    private String persistencePath;
    private long persistenceInterval;

    // 锁，用于线程安全的配置更新
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 构造函数，使用默认配置
     */
    public CacheConfig() {
        this.maximumSize = DEFAULT_MAXIMUM_SIZE;
        this.expireAfterWrite = DEFAULT_EXPIRE_AFTER_WRITE;
        this.offHeapCacheEnabled = DEFAULT_OFF_HEAP_CACHE_ENABLED;
        this.persistenceEnabled = DEFAULT_PERSISTENCE_ENABLED;
        this.autoPersistenceEnabled = DEFAULT_AUTO_PERSISTENCE_ENABLED;
        this.asyncWarmupEnabled = DEFAULT_ASYNC_WARMUP_ENABLED;
        this.persistencePath = DEFAULT_PERSISTENCE_PATH;
        this.persistenceInterval = DEFAULT_PERSISTENCE_INTERVAL;
    }

    /**
     * 构造函数，自定义配置
     */
    public CacheConfig(long maximumSize, long expireAfterWrite, boolean offHeapCacheEnabled,
                       boolean persistenceEnabled, boolean autoPersistenceEnabled,
                       boolean asyncWarmupEnabled, String persistencePath, long persistenceInterval) {
        this.maximumSize = maximumSize;
        this.expireAfterWrite = expireAfterWrite;
        this.offHeapCacheEnabled = offHeapCacheEnabled;
        this.persistenceEnabled = persistenceEnabled;
        this.autoPersistenceEnabled = autoPersistenceEnabled;
        this.asyncWarmupEnabled = asyncWarmupEnabled;
        this.persistencePath = persistencePath;
        this.persistenceInterval = persistenceInterval;
    }

    /**
     * 更新配置
     * @param newConfig 新配置
     */
    public void update(CacheConfig newConfig) {
        lock.writeLock().lock();
        try {
            this.maximumSize = newConfig.maximumSize;
            this.expireAfterWrite = newConfig.expireAfterWrite;
            this.offHeapCacheEnabled = newConfig.offHeapCacheEnabled;
            this.persistenceEnabled = newConfig.persistenceEnabled;
            this.autoPersistenceEnabled = newConfig.autoPersistenceEnabled;
            this.asyncWarmupEnabled = newConfig.asyncWarmupEnabled;
            this.persistencePath = newConfig.persistencePath;
            this.persistenceInterval = newConfig.persistenceInterval;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Getters and setters with proper locking

    public long getMaximumSize() {
        lock.readLock().lock();
        try {
            return maximumSize;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setMaximumSize(long maximumSize) {
        lock.writeLock().lock();
        try {
            this.maximumSize = maximumSize;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long getExpireAfterWrite() {
        lock.readLock().lock();
        try {
            return expireAfterWrite;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        lock.writeLock().lock();
        try {
            this.expireAfterWrite = expireAfterWrite;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isOffHeapCacheEnabled() {
        lock.readLock().lock();
        try {
            return offHeapCacheEnabled;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setOffHeapCacheEnabled(boolean offHeapCacheEnabled) {
        lock.writeLock().lock();
        try {
            this.offHeapCacheEnabled = offHeapCacheEnabled;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isPersistenceEnabled() {
        lock.readLock().lock();
        try {
            return persistenceEnabled;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPersistenceEnabled(boolean persistenceEnabled) {
        lock.writeLock().lock();
        try {
            this.persistenceEnabled = persistenceEnabled;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isAutoPersistenceEnabled() {
        lock.readLock().lock();
        try {
            return autoPersistenceEnabled;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setAutoPersistenceEnabled(boolean autoPersistenceEnabled) {
        lock.writeLock().lock();
        try {
            this.autoPersistenceEnabled = autoPersistenceEnabled;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isAsyncWarmupEnabled() {
        lock.readLock().lock();
        try {
            return asyncWarmupEnabled;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setAsyncWarmupEnabled(boolean asyncWarmupEnabled) {
        lock.writeLock().lock();
        try {
            this.asyncWarmupEnabled = asyncWarmupEnabled;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getPersistencePath() {
        lock.readLock().lock();
        try {
            return persistencePath;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPersistencePath(String persistencePath) {
        lock.writeLock().lock();
        try {
            this.persistencePath = persistencePath;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long getPersistenceInterval() {
        lock.readLock().lock();
        try {
            return persistenceInterval;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPersistenceInterval(long persistenceInterval) {
        lock.writeLock().lock();
        try {
            this.persistenceInterval = persistenceInterval;
        } finally {
            lock.writeLock().unlock();
        }
    }
}