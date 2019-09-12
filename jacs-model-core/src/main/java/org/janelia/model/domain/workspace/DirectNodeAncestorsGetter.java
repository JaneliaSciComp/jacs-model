package org.janelia.model.domain.workspace;

import java.util.Set;

import org.janelia.model.domain.DataSupplier;
import org.janelia.model.domain.Reference;

public interface DirectNodeAncestorsGetter extends DataSupplier<Reference, Set<Reference>> {

    Set<Reference> getDirectAncestors(Reference nodeReference);

    @Override
    default Set<Reference> getData(Reference key) {
        return getDirectAncestors(key);
    }
}
