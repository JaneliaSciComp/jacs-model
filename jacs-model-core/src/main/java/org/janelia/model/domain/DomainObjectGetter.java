package org.janelia.model.domain;

import java.util.List;

public interface DomainObjectGetter {
    DomainObject getDomainObjectByReference(Reference entityReference);
    List<? extends DomainObject> getDomainObjectsByReferences(List<Reference> entityReferences);
    List<? extends DomainObject> getDomainObjectsReferencedBy(ReverseReference reverseEntityReference);
}
