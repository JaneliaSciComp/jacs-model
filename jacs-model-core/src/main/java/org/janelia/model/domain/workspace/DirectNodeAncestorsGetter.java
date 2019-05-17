package org.janelia.model.domain.workspace;

import java.util.Set;

import org.janelia.model.domain.Reference;

public interface DirectNodeAncestorsGetter {
    Set<Reference> getNodeDirectAncestors(Reference nodeReference);
}
