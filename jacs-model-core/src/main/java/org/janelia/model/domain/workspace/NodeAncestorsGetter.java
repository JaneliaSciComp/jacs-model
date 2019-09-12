package org.janelia.model.domain.workspace;

import java.util.Set;

import org.janelia.model.domain.DataSupplier;
import org.janelia.model.domain.Reference;

public interface NodeAncestorsGetter extends DataSupplier<Reference, Set<Reference>> {

    Set<Reference> getNodeAncestors(Reference nodeReference);

    @Override
    default Set<Reference> getData(Reference key) {
        return getNodeAncestors(key);
    }
}
