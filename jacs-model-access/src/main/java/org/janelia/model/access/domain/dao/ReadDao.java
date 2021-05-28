package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

/**
 * Read data access spec.
 *
 * @param <T> entity type
 * @param <I> entity type
 */
public interface ReadDao<T, I> extends Dao<T, I> {
    T findById(I id);
    List<T> findByIds(Collection<I> ids);
    List<T> findAll(long offset, int length);
    default List<T> findAll() {
        return findAll(0, 0);
    }
}
