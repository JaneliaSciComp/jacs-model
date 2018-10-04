package org.janelia.model.access.domain.dao;

import java.util.Collection;

/**
 * Read/Write data access spec.
 *
 * @param <T> entity type
 * @param <I> entity identifier type
 */
public interface ReadWriteDao<T, I> extends ReadOnlyDao<T, I> {
    void save(T entity);
    void saveAll(Collection<T> entities);
    void delete(T entity);
}
