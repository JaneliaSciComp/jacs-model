package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base interface for reading domain objects.
 *
 * @param <T> entity type
 */
public interface DomainObjectReadDao<T extends DomainObject> extends ReadDao<T, Long> {
    /**
     * Find domain entity by ID accessible by the provided subject key.
     * @param id
     * @param subjectKey
     * @return
     */
    T findEntityByIdAccessibleBySubjectKey(Long id, String subjectKey);

    /**
     * Finds entities by ids accessible by subjectKey.
     * @param ids
     * @param subjectKey
     * @return
     */
    List<T> findEntitiesByIdsAccessibleBySubjectKey(List<Long> ids, String subjectKey);

    /**
     * Find domain entities owned by the provided subjectKey.
     *
     * @param subjectKey
     * @return
     */
    List<T> findOwnedEntitiesBySubjectKey(String subjectKey, long offset, int length);

    /**
     * Find domain entities readable by the provided subjectKey. If no subjectKey is provided it returns all entities.
     *
     * @param subjectKey
     * @return
     */
    List<T> findEntitiesReadableBySubjectKey(@Nullable String subjectKey, long offset, int length);

}