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

public class CachedDomainAnnotationGetter implements DomainAnnotationGetter {

    private static final Logger LOG = LoggerFactory.getLogger(CachedDomainAnnotationGetter.class);

    private final AnnotationDao annotationDao;
    private final LoadingCache<Reference, Set<SimpleDomainAnnotation>> referenceAnnotationsCache;

    @Inject
    public CachedDomainAnnotationGetter(AnnotationDao annotationDao) {
        this.annotationDao = annotationDao;
        referenceAnnotationsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .build(new CacheLoader<Reference, Set<SimpleDomainAnnotation>>() {
                    @Override
                    public Set<SimpleDomainAnnotation> load(Reference reference) {
                        return loadAnnotationsByReference(reference);
                    }
                });
    }

    @Override
    public Set<SimpleDomainAnnotation> getAnnotations(Reference ref) {
        try {
            return referenceAnnotationsCache.get(ref);
        } catch (ExecutionException e) {
            LOG.error("Error retrieving annotations for {}", ref, e);
            throw new IllegalStateException(e);
        }
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
