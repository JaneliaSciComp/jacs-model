package org.janelia.model.access.domain.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.janelia.model.domain.support.MongoMapped;

import java.util.concurrent.ExecutionException;

public class EntityUtils {

    private static LoadingCache<Class<?>, MongoMapped> MONGO_MAPPING_CACHE_BUILDER = CacheBuilder.newBuilder()
            .maximumSize(20)
            .build(new CacheLoader<Class<?>, MongoMapped>() {
                @Override
                public MongoMapped load(Class<?> entityClass) {
                    return loadPersistenceInfo(entityClass);
                }
            });

    private static MongoMapped loadPersistenceInfo(Class<?> objectClass) {
        MongoMapped persistenceInfo = null;
        for(Class<?> clazz = objectClass; clazz != null; clazz = clazz.getSuperclass()) {
            if (clazz.isAnnotationPresent(MongoMapped.class)) {
                persistenceInfo = clazz.getAnnotation(MongoMapped.class);
                break;
            }
        }
        return persistenceInfo;
    }

    public static MongoMapped getPersistenceInfo(Class<?> objectClass) {
        try {
            return MONGO_MAPPING_CACHE_BUILDER.get(objectClass);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
