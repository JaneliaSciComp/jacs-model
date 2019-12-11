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
        Class<?> hierarchyClass = this.getClass();
        while (!(hierarchyClass.getGenericSuperclass() instanceof ParameterizedType)) {
            hierarchyClass = hierarchyClass.getSuperclass();
        }
        return (Class<T>)((ParameterizedType) hierarchyClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    default Class<I> getEntityIdType() {
        Class<?> hierarchyClass = this.getClass();
        while (!(hierarchyClass.getGenericSuperclass() instanceof ParameterizedType)) {
            hierarchyClass = hierarchyClass.getSuperclass();
        }
        return (Class<I>)((ParameterizedType) hierarchyClass.getGenericSuperclass()).getActualTypeArguments()[1];
    }
}
