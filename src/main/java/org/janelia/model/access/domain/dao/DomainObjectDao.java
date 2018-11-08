package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;

import java.util.List;

/**
 * Base interface for domain object access.
 *
 * @param <T> entity type
 */
public interface DomainObjectDao<T extends DomainObject> extends ReadWriteDao<T, Long> {
    List<T> findByOwnerKey(String ownerKey);

    /**
     * Finds an readable entity by id and subjectKey.
     * @param id
     * @param subjectKey
     * @return
     */
    T findByIdAndSubjectKey(Long id, String subjectKey);

    /**
     * Finds entities by ids and subjectKey.
     * @param ids
     * @param subjectKey
     * @return
     */
    List<T> findByIdsAndSubjectKey(List<Long> ids, String subjectKey);

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