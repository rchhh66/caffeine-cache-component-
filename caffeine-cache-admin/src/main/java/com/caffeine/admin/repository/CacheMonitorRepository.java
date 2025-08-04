package com.caffeine.admin.repository;

import com.caffeine.admin.model.CacheMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CacheMonitorRepository extends JpaRepository<CacheMonitor, Long> {
    Optional<CacheMonitor> findByCacheName(String cacheName);
    boolean existsByCacheName(String cacheName);
    void deleteByCacheName(String cacheName);
}