package com.caffeine.component.persistence;

import com.caffeine.component.config.CacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存持久化管理器
 */
public class CachePersistenceManager {
    // 缓存配置
    private final CacheConfig config;
    // 数据库映射
    private final Map<String, DB> databases;
    // 存储缓存数据的映射
    private final Map<String, Map<Object, Object>> cacheDataMaps;

    /**
     * 构造函数
     * @param config 缓存配置
     */
    public CachePersistenceManager(CacheConfig config) {
        this.config = config;
        this.databases = new HashMap<>();
        this.cacheDataMaps = new ConcurrentHashMap<>();

        // 确保持久化目录存在
        File dir = new File(config.getPersistencePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 持久化缓存到本地磁盘
     * @param cacheName 缓存名称
     * @param cache 缓存实例
     */
    public void persistCache(String cacheName, Cache<Object, Object> cache) {
        if (!config.isPersistenceEnabled()) {
            return;
        }

        // 获取或创建数据库
        DB db = getOrCreateDatabase(cacheName);
        // 获取或创建数据映射
        Map<Object, Object> dataMap = getOrCreateDataMap(cacheName, db);

        // 清空现有数据
        dataMap.clear();
        // 保存新数据
        dataMap.putAll(cache.asMap());
        // 提交更改
        db.commit();
    }

    /**
     * 从持久化存储加载缓存
     * @param cacheName 缓存名称
     * @param cache 缓存实例
     */
    public void loadCache(String cacheName, Cache<Object, Object> cache) {
        if (!config.isPersistenceEnabled()) {
            return;
        }

        // 尝试获取数据库
        DB db = getDatabase(cacheName);
        if (db == null) {
            return;
        }

        // 尝试获取数据映射
        Map<Object, Object> dataMap = getOrCreateDataMap(cacheName, db);
        if (dataMap.isEmpty()) {
            return;
        }

        // 加载数据到缓存
        dataMap.forEach((key, value) -> {
            cache.put(key, value);
        });
    }

    /**
     * 持久化单个缓存条目
     * @param cacheName 缓存名称
     * @param key 键
     * @param value 值
     */
    public void persistEntry(String cacheName, Object key, Object value) {
        if (!config.isPersistenceEnabled()) {
            return;
        }

        // 获取或创建数据库
        DB db = getOrCreateDatabase(cacheName);
        // 获取或创建数据映射
        Map<Object, Object> dataMap = getOrCreateDataMap(cacheName, db);

        // 保存条目
        dataMap.put(key, value);
        // 提交更改
        db.commit();
    }

    /**
     * 删除单个缓存条目
     * @param cacheName 缓存名称
     * @param key 键
     */
    public void deleteEntry(String cacheName, Object key) {
        if (!config.isPersistenceEnabled()) {
            return;
        }

        // 尝试获取数据库
        DB db = getDatabase(cacheName);
        if (db == null) {
            return;
        }

        // 尝试获取数据映射
        Map<Object, Object> dataMap = getOrCreateDataMap(cacheName, db);

        // 删除条目
        if (dataMap.containsKey(key)) {
            dataMap.remove(key);
            // 提交更改
            db.commit();
        }
    }

    /**
     * 加载单个缓存条目
     * @param cacheName 缓存名称
     * @param key 键
     * @return 缓存值，如果不存在则返回null
     */
    public Object loadEntry(String cacheName, Object key) {
        if (!config.isPersistenceEnabled()) {
            return null;
        }

        // 尝试获取数据库
        DB db = getDatabase(cacheName);
        if (db == null) {
            return null;
        }

        // 尝试获取数据映射
        Map<Object, Object> dataMap = getOrCreateDataMap(cacheName, db);

        // 返回条目
        return dataMap.get(key);
    }

    /**
     * 获取或创建数据库
     * @param cacheName 缓存名称
     * @return 数据库实例
     */
    private DB getOrCreateDatabase(String cacheName) {
        return databases.computeIfAbsent(cacheName, name -> {
            String dbPath = config.getPersistencePath() + File.separator + name + ".db";
            return DBMaker.fileDB(dbPath)
                    .checksumHeaderBypass()
                    .fileMmapEnableIfSupported()
                    .transactionEnable()
                    .make();
        });
    }

    /**
     * 获取数据库
     * @param cacheName 缓存名称
     * @return 数据库实例，如果不存在则返回null
     */
    private DB getDatabase(String cacheName) {
        return databases.get(cacheName);
    }

    /**
     * 获取或创建数据映射
     * @param cacheName 缓存名称
     * @param db 数据库实例
     * @return 数据映射
     */
    private Map<Object, Object> getOrCreateDataMap(String cacheName, DB db) {
        return cacheDataMaps.computeIfAbsent(cacheName, name -> {
            return db.hashMap(name, Serializer.JAVA, Serializer.JAVA)
                    .createOrOpen();
        });
    }

    /**
     * 关闭所有数据库
     */
    public void shutdown() {
        databases.values().forEach(DB::close);
        databases.clear();
        cacheDataMaps.clear();
    }
}