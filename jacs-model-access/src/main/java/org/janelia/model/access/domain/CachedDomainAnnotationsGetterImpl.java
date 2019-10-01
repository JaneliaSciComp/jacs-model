package org.janelia.model.access.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.janelia.model.access.cdi.WithCache;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.DomainAnnotationGetter;
import org.janelia.model.domain.ontology.SimpleDomainAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WithCache
public class CachedDomainAnnotationsGetterImpl implements DomainAnnotationGetter {
    private static final Logger LOG = LoggerFactory.getLogger(CachedDomainAnnotationsGetterImpl.class);

    private final AnnotationDao annotationDao;
    private final Map<Reference, Set<SimpleDomainAnnotation>> annotationsCache;

    @Inject
    public CachedDomainAnnotationsGetterImpl(AnnotationDao annotationDao) {
        this.annotationDao = annotationDao;
        this.annotationsCache = loadAllAnnotations();
    }

    @Override
    public Set<SimpleDomainAnnotation> getAnnotations(Reference domainReference) {
        return annotationsCache.getOrDefault(domainReference, Collections.emptySet());
    }

    private Map<Reference, Set<SimpleDomainAnnotation>> loadAllAnnotations() {
        LOG.info("Start loading domain annotations cache");
        try {
            return annotationDao.streamAll()
                    .collect(Collectors.groupingBy(a -> a.getTarget(), Collectors.mapping(
                            a -> new SimpleDomainAnnotation(a.getName(),
                                    a.getReaders()), Collectors.toSet())));
        } finally {
            LOG.info("Finished loading domain annotations cache");
        }
    }

}
