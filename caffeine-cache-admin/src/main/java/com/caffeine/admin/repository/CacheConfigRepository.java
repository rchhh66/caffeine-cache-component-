package com.caffeine.admin.repository;

import com.caffeine.admin.model.CacheConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheConfigRepository extends JpaRepository<CacheConfig, Long> {
    CacheConfig findByCacheName(String cacheName);
    boolean existsByCacheName(String cacheName);
    void deleteByCacheName(String cacheName);
}