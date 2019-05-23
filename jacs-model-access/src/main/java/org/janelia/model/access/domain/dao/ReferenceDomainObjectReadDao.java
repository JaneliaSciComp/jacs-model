package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;

import java.util.List;

/**
 * Interface for reading domain objects by their reference.
 */
public interface ReferenceDomainObjectReadDao {
    /**
     * Find a domain object by its reference accessible by the provided subjectKey
     * @param entityReference
     * @param subjectKey
     * @param <T> entity type
     * @return
     */
    <T extends DomainObject> T findByReferenceAndSubjectKey(Reference entityReference, String subjectKey);

    List<? extends DomainObject> findByReferencesAndSubjectKey(List<Reference> entityReferences, String subjectKey);

    List<? extends DomainObject> findByReverseReferenceAndSubjectKey(ReverseReference reverseEntityReference, String subjectKey);
}
