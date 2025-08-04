# Caffeine Cache 高性能缓存组件

一个基于Caffeine的高性能多级缓存组件，提供堆内缓存、堆外缓存、持久化存储和异步预热等企业级功能，同时附带可视化管理后台。

## 项目亮点

- **多级缓存架构**：结合堆内缓存、堆外缓存和磁盘持久化，平衡性能与内存占用
- **高性能**：基于Caffeine实现的堆内缓存，提供纳秒级响应速度
- **可靠性**：支持数据持久化和异步恢复，确保缓存数据不丢失
- **灵活配置**：支持动态更新缓存配置，无需重启应用
- **可视化管理**：提供Web管理界面，轻松监控和配置缓存
- **异步处理**：支持异步缓存预热和持久化操作，不阻塞主线程

## 功能特性

### 缓存层级
- **堆内缓存**：基于Caffeine实现，速度最快
- **堆外缓存**：基于Ehcache实现，突破JVM内存限制
- **磁盘持久化**：基于MapDB实现，支持数据持久化到磁盘

### 核心功能
- 缓存自动过期（TTL）
- 异步缓存预热
- 自动/手动持久化
- 缓存配置动态更新
- 缓存统计信息收集
- Redis数据加载器

### 管理后台
- 缓存实例管理
- 缓存配置可视化编辑
- 缓存状态实时监控
- 缓存统计数据展示

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.caffeine.component</groupId>
    <artifactId>caffeine-cache-component</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 基本使用

```java
// 1. 创建缓存配置
CacheConfig config = new CacheConfig();
config.setMaximumSize(10000);         // 最大容量
config.setExpireAfterWrite(3600);     // 过期时间(秒)
config.setPersistenceEnabled(true);   // 启用持久化
config.setOffHeapCacheEnabled(false); // 禁用堆外缓存

// 2. 创建缓存管理器
CaffeineCacheManager cacheManager = new CaffeineCacheManager(config);

// 3. 获取缓存实例
Cache<Object, Object> cache = cacheManager.getCache("myCache");

// 4. 使用缓存
cache.put("key", "value");
Object value = cache.getIfPresent("key");
```

## 高级使用

### 自定义缓存配置

```java
// 创建自定义缓存配置
CacheConfig customConfig = new CacheConfig(
    5000,                 // 最大容量
    1800,                 // 过期时间(秒)
    true,                 // 启用堆外缓存
    true,                 // 启用持久化
    true,                 // 启用自动持久化
    true,                 // 启用异步预热
    "./custom_cache_data", // 持久化路径
    3600                  // 持久化间隔(秒)
);

// 更新现有缓存管理器的配置
cacheManager.setConfig(customConfig);
```

### 异步缓存预热

使用`AsyncCacheWarmupManager`实现缓存的异步预热：

```java
// 创建异步预热管理器
AsyncCacheWarmupManager warmupManager = new AsyncCacheWarmupManager(config);

// 设置数据加载器
warmupManager.setDataLoader(new CacheDataLoader() {
    @Override
    public void loadData(String cacheName, Cache<Object, Object> cache) {
        // 从数据源加载数据到缓存
        // 示例：加载用户数据
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            cache.put("user:" + user.getId(), user);
        }
    }
});

// 异步预热缓存
warmupManager.warmupCacheAsync("userCache", cache);
```

### Redis缓存加载器

从Redis加载数据到本地缓存：

```java
// 初始化Redis缓存加载器
String redisHost = "localhost";
int redisPort = 6379;
String redisPrefix = "app:";

// 配置Jedis连接池
JedisPoolConfig poolConfig = new JedisPoolConfig();
poolConfig.setMaxTotal(10);
poolConfig.setMaxIdle(5);
poolConfig.setMinIdle(1);

RedisCacheLoader redisCacheLoader = new RedisCacheLoader(redisHost, redisPort, redisPrefix, poolConfig);

// 从Redis异步加载数据到本地缓存
redisCacheLoader.loadData(cacheName, cache);
```

### 持久化操作

```java
// 手动持久化指定缓存
cacheManager.persistCache("myCache");

// 持久化所有缓存
cacheManager.persistAllCaches();
```

### 堆外缓存操作

```java
// 保存指定缓存到堆外
cacheManager.saveToOffHeapCache("myCache");

// 保存所有缓存到堆外
cacheManager.saveAllToOffHeapCache();
```

## 管理后台

### 启动管理后台

```bash
cd caffeine-cache-admin
mvn spring-boot:run
```

### 访问管理界面

打开浏览器访问: http://localhost:8080/caffeine-admin

### 功能说明

1. **缓存管理**：查看和管理所有缓存实例，包括添加、删除和清空缓存
2. **配置中心**：可视化编辑缓存配置参数，并实时生效
3. **统计分析**：查看缓存命中率、访问次数、过期数量等统计信息
4. **数据查看**：查看缓存中的键值对数据

## 项目结构

```
caffeine-cache-component/  # 核心缓存组件
  ├── src/main/java/com/caffeine/component/core/       # 核心缓存实现
  ├── src/main/java/com/caffeine/component/offheap/   # 堆外缓存实现
  ├── src/main/java/com/caffeine/component/persistence/ # 持久化实现
  └── src/main/java/com/caffeine/component/warmup/    # 预热机制实现

caffeine-cache-admin/      # 管理后台
  ├── src/main/java/com/caffeine/admin/               # 后台代码
  └── src/main/resources/                             # 静态资源和配置

caffeine-cache-test/       # 测试用例
  └── src/test/java/com/caffeine/component/example/   # 示例代码
```

## 技术栈

- **核心框架**：Java 8+, Spring Boot 2.x
- **缓存技术**：Caffeine, Ehcache
- **持久化**：MapDB
- **Web框架**：Spring MVC
- **前端技术**：Thymeleaf, Bootstrap, jQuery
- **构建工具**：Maven

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建新的Pull Request

## 联系我们

如果您有任何问题或建议，请通过以下方式联系我们：
- 邮箱：team@caffeine-cache.com
- GitHub Issues：https://github.com/your-org/caffeine-cache/issues
