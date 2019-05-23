package org.janelia.model.domain;

import java.util.List;

public interface DomainObjectGetter {
    DomainObject getDomainObjectByReference(Reference entityReference);
    List<DomainObject> getDomainObjectsByReferences(List<Reference> entityReferences);
    List<DomainObject> getDomainObjectsReferencedBy(ReverseReference reverseEntityReference);
}
