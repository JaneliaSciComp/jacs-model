package org.janelia.model.access.domain;

import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.domain.dao.ReferenceDomainObjectReadDao;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainObjectGetter;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;

public class SimpleDomainObjectGetter implements DomainObjectGetter {

    private final ReferenceDomainObjectReadDao referenceDomainObjectReadDao;

    @Inject
    public SimpleDomainObjectGetter(ReferenceDomainObjectReadDao referenceDomainObjectReadDao) {
        this.referenceDomainObjectReadDao = referenceDomainObjectReadDao;
    }

    @Override
    public DomainObject getDomainObjectByReference(Reference entityReference) {
        return referenceDomainObjectReadDao.findByReference(entityReference);
    }

    @Override
    public List<? extends DomainObject> getDomainObjectsByReferences(List<Reference> entityReferences) {
        return referenceDomainObjectReadDao.findByReferences(entityReferences);
    }

    @Override
    public List<? extends DomainObject> getDomainObjectsReferencedBy(ReverseReference reverseEntityReference) {
        return referenceDomainObjectReadDao.findByReverseReference(reverseEntityReference);
    }
}
