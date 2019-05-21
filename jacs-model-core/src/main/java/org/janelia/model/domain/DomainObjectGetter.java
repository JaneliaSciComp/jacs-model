package org.janelia.model.domain;

import java.util.List;

public interface DomainObjectGetter {
    DomainObject getDomainObjectByReference(Reference objRef);
    List<DomainObject> getDomainObjectsReferencedBy(ReverseReference reverseObjRef);
}
