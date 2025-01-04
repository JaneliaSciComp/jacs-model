package org.janelia.model.access.domain;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.janelia.model.access.cdi.WithCache;
import org.janelia.model.access.domain.dao.ReferenceDomainObjectReadDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainObjectGetter;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WithCache
@Dependent
public class CachedDomainObjectGetter implements DomainObjectGetter {

    private static final Logger LOG = LoggerFactory.getLogger(CachedDomainObjectGetter.class);

    private final Cache<Reference, DomainObject> DOMAIN_OBJECT_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .build();

    private final Cache<ReverseReference, List<? extends DomainObject>> REVERSE_REFERENCED_DOMAIN_OBJECTS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .build();

    private final ReferenceDomainObjectReadDao referenceDomainObjectReadDao;

    @Inject
    public CachedDomainObjectGetter(ReferenceDomainObjectReadDao referenceDomainObjectReadDao) {
        this.referenceDomainObjectReadDao = referenceDomainObjectReadDao;
    }

    @Override
    public DomainObject getDomainObjectByReference(Reference entityReference) {
        try {
            return DOMAIN_OBJECT_CACHE.get(entityReference, () -> referenceDomainObjectReadDao.findByReference(entityReference));
        } catch (ExecutionException e) {
            LOG.warn("No domain object found for {}", entityReference, e);
            return null;
        }
    }

    @Override
    public List<? extends DomainObject> getDomainObjectsByReferences(List<Reference> entityReferences) {
        return referenceDomainObjectReadDao.findByReferences(entityReferences);
    }

    @Override
    public List<? extends DomainObject> getDomainObjectsReferencedBy(ReverseReference reverseEntityReference) {
        try {
            return REVERSE_REFERENCED_DOMAIN_OBJECTS_CACHE.get(reverseEntityReference, () -> referenceDomainObjectReadDao.findByReverseReference(reverseEntityReference));
        } catch (ExecutionException e) {
            LOG.warn("No domain objects found for reverse reference {}", reverseEntityReference, e);
            return Collections.emptyList();
        }
    }
}
