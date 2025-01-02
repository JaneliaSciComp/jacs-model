package org.janelia.model.access.domain;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import com.google.common.collect.ImmutableSet;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.DomainAnnotationGetter;
import org.janelia.model.domain.ontology.SimpleDomainAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainAnnotationGetterImpl implements DomainAnnotationGetter {

    private static final Logger LOG = LoggerFactory.getLogger(DomainAnnotationGetterImpl.class);

    private final AnnotationDao annotationDao;

    @Inject
    public DomainAnnotationGetterImpl(AnnotationDao annotationDao) {
        this.annotationDao = annotationDao;
    }

    @Override
    public Set<SimpleDomainAnnotation> getAnnotations(Reference ref) {
        return loadAnnotationsByReference(ref);
    }

    private Set<SimpleDomainAnnotation> loadAnnotationsByReference(Reference ref) {
        LOG.debug("Load annotations for {}", ref);
        try {
            Set<SimpleDomainAnnotation> annotations = annotationDao.findAnnotationsByTargets(ImmutableSet.of(ref)).stream()
                    .map(a -> new SimpleDomainAnnotation(a.getName(), a.getReaders()))
                    .collect(Collectors.toSet());
            LOG.debug("Found annotations {} for {}", annotations, ref);
            return annotations;
        } finally {
            LOG.debug("Finished loading annotations for {}", ref);
        }
    }
}
