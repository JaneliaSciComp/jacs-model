package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.Annotation;

/**
 * {@link Annotation} DAO.
 */
public interface AnnotationDao extends DomainObjectDao<Annotation> {
    List<Annotation> findAnnotationsByTargetsAccessibleBySubjectKey(Collection<Reference> references, String subjectKey);
}
