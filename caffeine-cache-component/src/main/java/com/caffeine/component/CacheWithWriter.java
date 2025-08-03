package com.caffeine.component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 包装Caffeine缓存，添加写入逻辑
 */
public class CacheWithWriter<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final RemovalListener<K, V> writer;

    public CacheWithWriter(Cache<K, V> delegate, RemovalListener<K, V> writer) {
        this.delegate = delegate;
        this.writer = writer;
    }


    



    @Override
    @SuppressWarnings("unchecked")
    public V getIfPresent(Object key) {
        return delegate.getIfPresent((K) key);
    }

    @Override
    public V get(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        V value = delegate.get(key, mappingFunction);
        if (value != null) {
            if (writer instanceof CacheWriterAdapter) {
                ((CacheWriterAdapter<K, V>) writer).write(key, value);
            }
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public java.util.Map<K, V> getAllPresent(java.lang.Iterable<? extends K> keys) {
        return delegate.getAllPresent(keys);
    }

    @Override
    public java.util.Map<K, V> getAll(java.lang.Iterable<? extends K> keys, java.util.function.Function<? super java.util.Set<? extends K>, ? extends java.util.Map<? extends K, ? extends V>> mappingFunction) {
        java.util.Map<K, V> result = delegate.getAll(keys, mappingFunction);
        if (writer instanceof CacheWriterAdapter) {
            CacheWriterAdapter<K, V> adapter = (CacheWriterAdapter<K, V>) writer;
            result.forEach(adapter::write);
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        if (writer instanceof CacheWriterAdapter) {
            ((CacheWriterAdapter<K, V>) writer).write(key, value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
        if (writer instanceof CacheWriterAdapter) {
            CacheWriterAdapter<K, V> adapter = (CacheWriterAdapter<K, V>) writer;
            map.forEach(adapter::write);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invalidate(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        V value = delegate.getIfPresent(k);
        delegate.invalidate(k);
        if (value != null) {
            writer.onRemoval(k, value, RemovalCause.EXPLICIT);
        }
    }

    @Override
    public void invalidateAll() {
        Map<K, V> snapshot = new ConcurrentHashMap<>(delegate.asMap());
        delegate.invalidateAll();
        snapshot.forEach((key, value) -> writer.onRemoval(key, value, RemovalCause.EXPLICIT));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invalidateAll(Iterable<? extends K> keys) {
        Map<K, V> snapshot = new ConcurrentHashMap<>();
        keys.forEach(key -> {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            V value = delegate.getIfPresent(k);
            if (value != null) {
                snapshot.put(k, value);
            }
        });
        delegate.invalidateAll(keys);
        snapshot.forEach((key, value) -> writer.onRemoval(key, value, RemovalCause.EXPLICIT));
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return new MapWithWriter<>(delegate.asMap(), writer);
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    @Override
    public Policy<K, V> policy() {
        return delegate.policy();
    }



    @Override
    public CacheStats stats() {
        return delegate.stats();
    }

    @Override
    public long estimatedSize() {
        return delegate.estimatedSize();
    }

    /**
     * 包装Map，添加写入逻辑
     */
    private static class MapWithWriter<K, V> implements ConcurrentMap<K, V> {
        private final Map<K, V> delegate;
        private final RemovalListener<K, V> writer;

        public MapWithWriter(Map<K, V> delegate, RemovalListener<K, V> writer) {
            this.delegate = delegate;
            this.writer = writer;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return delegate.get(key);
        }

        @Override
        public V put(K key, V value) {
            V oldValue = delegate.put(key, value);
            if (oldValue != null) {
                writer.onRemoval(key, oldValue, RemovalCause.REPLACED);
            }
            if (writer instanceof CacheWriterAdapter) {
                ((CacheWriterAdapter<K, V>) writer).write(key, value);
            }
            return oldValue;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V remove(Object key) {
            V oldValue = delegate.remove(key);
            if (oldValue != null) {
                writer.onRemoval((K) key, oldValue, RemovalCause.EXPLICIT);
            }
            return oldValue;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            m.forEach((key, value) -> {
                V oldValue = delegate.put(key, value);
                if (oldValue != null) {
                    writer.onRemoval(key, oldValue, RemovalCause.REPLACED);
                }
                if (writer instanceof CacheWriterAdapter) {
                    ((CacheWriterAdapter<K, V>) writer).write(key, value);
                }
            });
        }

        @Override
        public void clear() {
            Map<K, V> snapshot = new ConcurrentHashMap<>(delegate);
            delegate.clear();
            snapshot.forEach((key, value) -> writer.onRemoval(key, value, RemovalCause.EXPLICIT));
        }

        @Override
        public V putIfAbsent(K key, V value) {
            V oldValue = delegate.putIfAbsent(key, value);
            if (oldValue == null) {
                if (writer instanceof CacheWriterAdapter) {
                    ((CacheWriterAdapter<K, V>) writer).write(key, value);
                }
            }
            return oldValue;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = delegate.remove(key, value);
            if (removed) {
                @SuppressWarnings("unchecked")
                K k = (K) key;
                @SuppressWarnings("unchecked")
                V v = (V) value;
                writer.onRemoval(k, v, RemovalCause.EXPLICIT);
            }
            return removed;
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            boolean replaced = delegate.replace(key, oldValue, newValue);
            if (replaced) {
                writer.onRemoval(key, oldValue, RemovalCause.REPLACED);
                if (writer instanceof CacheWriterAdapter) {
                    ((CacheWriterAdapter<K, V>) writer).write(key, newValue);
                }
            }
            return replaced;
        }

        @Override
        public V replace(K key, V value) {
            V oldValue = delegate.replace(key, value);
            if (oldValue != null) {
                writer.onRemoval(key, oldValue, RemovalCause.REPLACED);
                if (writer instanceof CacheWriterAdapter) {
                    ((CacheWriterAdapter<K, V>) writer).write(key, value);
                }
            }
            return oldValue;
        }

        // 其余ConcurrentMap方法实现
        @Override
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<V> values() {
            return delegate.values();
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return delegate.entrySet();
        }
    }

}