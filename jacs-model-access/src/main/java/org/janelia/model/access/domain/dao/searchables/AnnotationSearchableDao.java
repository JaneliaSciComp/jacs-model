package org.janelia.model.access.domain.dao.searchables;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.access.domain.dao.ReferenceDomainObjectReadDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.ontology.OntologyTermReference;

/**
 * {@link Annotation} DAO.
 */
@AsyncIndex
public class AnnotationSearchableDao extends AbstractDomainSearchableDao<Annotation> implements AnnotationDao {

    private final AnnotationDao annotationDao;
    private final ReferenceDomainObjectReadDao referenceDomainObjectReadDao;

    @Inject
    AnnotationSearchableDao(AnnotationDao annotationDao,
                            ReferenceDomainObjectReadDao referenceDomainObjectReadDao,
                            @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(annotationDao, objectIndexer);
        this.annotationDao = annotationDao;
        this.referenceDomainObjectReadDao = referenceDomainObjectReadDao;
    }

    @Override
    public Annotation createAnnotation(String subjectKey, Reference target, OntologyTermReference ontologyTermReference, String value) {
        Annotation persistedAnnotation = annotationDao.createAnnotation(subjectKey, target, ontologyTermReference, value);
        indexAnnotationTarget(persistedAnnotation);
        return persistedAnnotation;
    }

    @Override
    public Annotation updateAnnotationValue(String subjectKey, Long annotationId, String value) {
        Annotation persistedAnnotation = annotationDao.updateAnnotationValue(subjectKey, annotationId, value);
        indexAnnotationTarget(persistedAnnotation);
        return persistedAnnotation;
    }

    @Override
    public List<Annotation> findAnnotationsByTargets(Collection<Reference> references) {
        return annotationDao.findAnnotationsByTargets(references);
    }

    @Override
    public List<Annotation> findAnnotationsByTargetsAccessibleBySubjectKey(Collection<Reference> references, String subjectKey) {
        return annotationDao.findAnnotationsByTargetsAccessibleBySubjectKey(references, subjectKey);
    }

    @Override
    public Annotation saveBySubjectKey(Annotation entity, String subjectKey) {
        Annotation persistedAnnotation = super.saveBySubjectKey(entity, subjectKey);
        indexAnnotationTarget(persistedAnnotation);
        return persistedAnnotation;
    }

    private void indexAnnotationTarget(Annotation a) {
        if (a != null) {
            indexAnnotationTarget(a.getTarget());
        }
    }

    private void indexAnnotationTarget(Reference reference) {
        if (reference != null) {
            DomainObject targetObject = referenceDomainObjectReadDao.findByReference(reference);
            if (targetObject != null) {
                domainObjectIndexer.indexDocument(targetObject);
            }
        }
    }

    @Override
    public void save(Annotation annotation) {
        super.save(annotation);
        indexAnnotationTarget(annotation);
    }

    @Override
    public void saveAll(Collection<Annotation> annotations) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            super.saveAll(annotations);
            List<? extends DomainObject> annotationTargets = referenceDomainObjectReadDao.findByReferences(annotations.stream()
                    .map(Annotation::getTarget)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );
            domainObjectIndexer.indexDocumentStream(annotationTargets.stream());
        }
    }

    @Override
    public long deleteByIdAndSubjectKey(Long id, String subjectKey) {
        Annotation existingAnnotation = domainObjectDao.findById(id);
        long nDeletedItems = super.deleteByIdAndSubjectKey(id, subjectKey);
        if (nDeletedItems > 0 && existingAnnotation != null) {
            indexAnnotationTarget(existingAnnotation);
        }
        return nDeletedItems;
    }

    @Override
    public void delete(Annotation annotation) {
        Reference annotationTarget;
        if (annotation.getTarget() != null) {
            annotationTarget = annotation.getTarget();
        } else {
            Annotation existingAnnotation = domainObjectDao.findById(annotation.getId());
            if (existingAnnotation != null) {
                annotationTarget = existingAnnotation.getTarget();
            } else {
                annotationTarget = null;
            }
        }
        super.delete(annotation);
        indexAnnotationTarget(annotationTarget);
    }

}
