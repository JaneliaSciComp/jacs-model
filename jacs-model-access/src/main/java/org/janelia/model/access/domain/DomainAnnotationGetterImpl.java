package org.janelia.model.access.domain;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
        LOG.info("Load annotations for {}", ref);
        try {
            return annotationDao.findAnnotationsByTargetsAccessibleBySubjectKey(ImmutableSet.of(ref), null).stream()
                    .map(a -> new SimpleDomainAnnotation(a.getName(), a.getReaders()))
                    .collect(Collectors.toSet());
        } finally {
            LOG.info("Finished loading annotations for {}", ref);
        }
    }
}
