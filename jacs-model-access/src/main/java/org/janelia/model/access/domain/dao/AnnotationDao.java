package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.ontology.OntologyTermReference;

/**
 * {@link Annotation} DAO.
 */
public interface AnnotationDao extends DomainObjectDao<Annotation> {
    Annotation createAnnotation(String subjectKey, Reference target, OntologyTermReference ontologyTermReference, String value);
    Annotation updateAnnotationValue(String subjectKey, Long annotationId, String value);
    List<Annotation> findAnnotationsByTargets(Collection<Reference> references);
    List<Annotation> findAnnotationsByTargetsAccessibleBySubjectKey(Collection<Reference> references, String subjectKey);
}
