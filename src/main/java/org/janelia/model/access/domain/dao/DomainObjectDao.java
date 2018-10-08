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
     * Delete the entity by id if the specified subjectKey has write permissions
     * @param id
     * @param subjectKey
     * @return
     */
    long deleteByIdAndSubjectKey(Long id, String subjectKey);
}
