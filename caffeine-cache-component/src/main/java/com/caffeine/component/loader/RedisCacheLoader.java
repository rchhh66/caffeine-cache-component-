package com.caffeine.component.loader;

import com.github.benmanes.caffeine.cache.Cache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Set;

/**
 * Redis缓存加载器
 * 从Redis加载数据到本地缓存
 */
public class RedisCacheLoader extends AbstractExternalCacheLoader {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheLoader.class);
    private final JedisPool jedisPool;
    private final String redisPrefix;

    /**
     * 构造函数
     * @param host Redis主机地址
     * @param port Redis端口
     * @param redisPrefix Redis键前缀
     */
    public RedisCacheLoader(String host, int port, String redisPrefix) {
        this(host, port, redisPrefix, new JedisPoolConfig());
    }

    /**
     * 构造函数
     * @param host Redis主机地址
     * @param port Redis端口
     * @param redisPrefix Redis键前缀
     * @param poolConfig Jedis连接池配置
     */
    public RedisCacheLoader(String host, int port, String redisPrefix, JedisPoolConfig poolConfig) {
        super();
        this.redisPrefix = redisPrefix != null ? redisPrefix : "";
        this.jedisPool = new JedisPool(poolConfig, host, port);
        logger.info("初始化Redis缓存加载器，连接到: {}:{}", host, port);
    }

    /**
     * 从Redis加载数据到本地缓存
     * @param cacheName 缓存名称
     * @param cache 本地缓存实例
     * @throws Exception 加载过程中可能出现的异常
     */
    @Override
    protected void doLoadData(String cacheName, Cache<Object, Object> cache) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            // 构造Redis键模式
            String keyPattern = redisPrefix + cacheName + ":*";
            logger.info("从Redis加载数据，键模式: {}", keyPattern);

            // 获取匹配的键
            Set<String> keys = jedis.keys(keyPattern);
            if (keys.isEmpty()) {
                logger.info("没有找到匹配的Redis键: {}", keyPattern);
                return;
            }

            logger.info("找到 {} 个匹配的Redis键", keys.size());

            // 批量获取键值对
            for (String key : keys) {
                // 去掉前缀，只保留实际的键
                String actualKey = key.substring((redisPrefix + cacheName + ":").length());
                String value = jedis.get(key);
                if (value != null) {
                    cache.put(actualKey, value);
                }
            }

            logger.info("成功加载 {} 个键值对到本地缓存: {}", keys.size(), cacheName);
        } catch (Exception e) {
            logger.error("从Redis加载数据失败", e);
            throw e;
        }
    }

    /**
     * 设置Redis键值对
     * @param cacheName 缓存名称
     * @param key 键
     * @param value 值
     */
    public void set(String cacheName, String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String redisKey = redisPrefix + cacheName + ":" + key;
            jedis.set(redisKey, value);
            logger.info("设置Redis键值对成功: {} -> {}", redisKey, value);
        } catch (Exception e) {
            logger.error("设置Redis键值对失败: {}:{}", cacheName, key, e);
            throw new RuntimeException("设置Redis键值对失败", e);
        }
    }

    /**
     * 批量设置Redis键值对
     * @param cacheName 缓存名称
     * @param data 键值对映射
     */
    public void setBatch(String cacheName, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            logger.info("批量设置Redis键值对，数据为空");
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String redisKey = redisPrefix + cacheName + ":" + key;
                jedis.set(redisKey, value);
            }
            logger.info("成功批量设置 {} 个Redis键值对到缓存: {}", data.size(), cacheName);
        } catch (Exception e) {
            logger.error("批量设置Redis键值对失败", e);
            throw new RuntimeException("批量设置Redis键值对失败", e);
        }
    }

    /**
     * 关闭Redis连接池和线程池
     */
    @Override
    public void shutdown() {
        super.shutdown();
        logger.info("关闭Redis连接池");
        jedisPool.close();
    }
}