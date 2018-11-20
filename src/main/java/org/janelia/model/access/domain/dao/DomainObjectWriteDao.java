package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;

import java.util.List;

/**
 * Base interface for writing domain object.
 *
 * @param <T> entity type
 */
public interface DomainObjectWriteDao<T extends DomainObject> extends WriteDao<T, Long> {
    /**
     * Delete the entity by id if the specified subjectKey has write permissions
     * @param id
     * @param subjectKey
     * @return
     */
    long deleteByIdAndSubjectKey(Long id, String subjectKey);

    /**
     * Save object by subjectKey
     * @param entity
     * @param subjectKey
     * @return the persisted instance
     */
    T saveWithSubjectKey(T entity, String subjectKey);
}
