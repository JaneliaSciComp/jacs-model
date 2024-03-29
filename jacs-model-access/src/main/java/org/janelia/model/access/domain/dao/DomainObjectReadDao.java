package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

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
    T findEntityByIdReadableBySubjectKey(Long id, String subjectKey);

    /**
     * Finds entities by ids accessible by subjectKey.
     * @param ids
     * @param subjectKey
     * @return
     */
    List<T> findEntitiesByIdsReadableBySubjectKey(List<Long> ids, String subjectKey);

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

    /**
     * Find domain entities readable by the provided subjectKey, with a given foreign key. If no subjectKey is provided
     * it returns all entities with the foreign key.
     *
     * @param subjectKey key of authorized subject
     * @param foreignKey name of the attribute holding a foreign key
     * @param foreignRef value of the foreign identifier
     * @return
     */
    default List<T> findEntitiesByForeignKeyReadableBySubjectKey(String subjectKey, String foreignKey, Reference foreignRef) {
        return findEntitiesByForeignKeyReadableBySubjectKey(subjectKey, foreignKey, foreignRef);
    }

    /**
     * Find domain entities readable by the provided subjectKey, with a given foreign key, in a paginated way.
     * If no subjectKey is provided it returns all entities with the foreign key.
     *
     * @param subjectKey key of authorized subject
     * @param foreignKey name of the attribute holding a foreign key
     * @param foreignRef value of the foreign identifier
     * @param offset index of first result to return
     * @param length max number of results to return
     * @return
     */
    List<T> findEntitiesByForeignKeyReadableBySubjectKey(@Nullable String subjectKey, String foreignKey, Reference foreignRef, long offset, int length);

    /**
     * Find domain entities by exact name.
     */
    List<T> findEntitiesByExactName(String name);

    /**
     * Find domain entities that match any of the given names. If the given names is empty then it returns an empty list
     */
    List<T> findEntitiesMatchingAnyGivenName(List<String> names);

    /**
     * Stream all records.
     *
     * @return
     */
    Stream<T> streamAll();
}
