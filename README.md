# Caffeine Cache Component

一个基于Caffeine的高性能缓存组件，提供本地缓存、堆外缓存、持久化和异步预热等功能。

## 功能特性

1. **多级缓存支持**
   - 堆内缓存 (基于Caffeine)
   - 堆外缓存 (基于Ehcache)
   - 磁盘持久化 (基于MapDB)

2. **高级特性**
   - 缓存自动过期
   - 异步缓存预热
   - 自动持久化
   - 缓存配置动态更新
   - 缓存统计

3. **管理后台**
   - 基于Spring Boot的Web管理界面
   - 缓存配置可视化编辑
   - 缓存状态监控

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
// 创建缓存配置
CacheConfig config = new CacheConfig();
config.setMaximumSize(10000);
config.setExpireAfterWrite(3600);
config.setPersistenceEnabled(true);
config.setAsyncWarmupEnabled(true);

// 创建缓存管理器
CaffeineCacheManager cacheManager = new CaffeineCacheManager(config);

// 获取缓存实例
Cache<Object, Object> cache = cacheManager.getCache("myCache");

// 使用缓存
cache.put("key", "value");
Object value = cache.getIfPresent("key");
```

### 高级配置

```java
// 自定义缓存配置
CacheConfig customConfig = new CacheConfig(
    5000,                // 最大容量
    1800,                // 过期时间(秒)
    true,                // 启用堆外缓存
    true,                // 启用持久化
    true,                // 启用自动持久化
    true,                // 启用异步预热
    "./custom_cache_data", // 持久化路径
    3600                 // 持久化间隔(秒)
);

// 更新现有缓存管理器的配置
cacheManager.setConfig(customConfig);
```

### 异步缓存预热

```java
// 创建异步预热管理器
AsyncCacheWarmupManager warmupManager = new AsyncCacheWarmupManager(config);

// 设置数据加载器
warmupManager.setDataLoader(new CacheDataLoader() {
    @Override
    public void loadData(String cacheName, Cache<Object, Object> cache) {
        // 从数据源加载数据到缓存
        cache.put("preloaded-key", "preloaded-value");
    }
});

// 异步预热缓存
warmupManager.warmupCacheAsync("myCache", cache);
```

### 持久化操作

```java
// 手动持久化缓存
cacheManager.persistCache("myCache");

// 持久化所有缓存
cacheManager.persistAllCaches();
```

## 管理后台使用

### 启动管理后台

```bash
cd caffeine-cache-admin
mvn spring-boot:run
```

### 访问管理界面

打开浏览器访问: http://localhost:8080/caffeine-admin

### 功能说明

1. **缓存管理**：查看和管理所有缓存实例
2. **配置编辑**：修改缓存配置参数
3. **缓存统计**：查看缓存命中率、访问次数等统计信息

## 项目结构

```
caffeine-cache-component/  # 核心缓存组件
caffeine-cache-admin/      # 管理后台
caffeine-cache-test/       # 测试用例
```

## 技术栈

- Java 8+
- Caffeine (缓存核心)
- Ehcache (堆外缓存)
- MapDB (持久化)
- Spring Boot (管理后台)
- Thymeleaf (前端模板)
- Bootstrap (UI框架)

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/fooBar`)
3. 提交更改 (`git commit -am 'Add some fooBar'`)
4. 推送到分支 (`git push origin feature/fooBar`)
5. 创建新的Pull Request