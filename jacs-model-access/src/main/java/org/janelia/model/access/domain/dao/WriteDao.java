package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.Map;

/**
 * Read/Write data access spec.
 *
 * @param <T> entity type
 * @param <I> entity identifier type
 */
public interface WriteDao<T, I> extends Dao<T, I> {
    void delete(T entity);
    void save(T entity);
    void saveAll(Collection<T> entities);
    void replace(T entity);
    DaoUpdateResult update(I entityId, Map<String, EntityFieldValueHandler<?>> fieldsToUpdate);
}
