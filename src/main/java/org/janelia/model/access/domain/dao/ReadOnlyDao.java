package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

/**
 * Read only data access spec.
 *
 * @param <T> entity type
 * @param <I> entity type
 */
public interface ReadOnlyDao<T, I> extends Dao<T, I> {
    T findById(I id);
    List<T> findByIds(Collection<I> ids);
}
