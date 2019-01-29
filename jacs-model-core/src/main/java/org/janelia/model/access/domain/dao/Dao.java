package org.janelia.model.access.domain.dao;

import java.lang.reflect.ParameterizedType;

/**
 * Base interface for data access.
 *
 * @param <T> Entity Type
 * @param <I> Type of the Entity ID
 */
public interface Dao<T, I> {
    @SuppressWarnings("unchecked")
    default Class<T> getEntityType() {
        return (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    default Class<I> getEntityIdType() {
        return (Class<I>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }
}
