package org.janelia.model.domain;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedDataSupplier<K, V> implements DataSupplier<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(CachedDataSupplier.class);

    private final LoadingCache<K, V> dataCache;

    public CachedDataSupplier(DataSupplier<K, V> dataSupplierDelegate, long cacheDurationInSeconds) {
        this.dataCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheDurationInSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        return dataSupplierDelegate.getData(key);
                    }
                })
                ;
    }

    @Override
    public V getData(K key) {
        try {
            return dataCache.get(key);
        } catch (ExecutionException e) {
            LOG.error("Error retrieving data for {}", key, e);
            throw new IllegalStateException(e);
        }
    }

}
