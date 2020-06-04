package org.janelia.model.access.domain.nodetools;

import java.util.Set;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;

public interface DirectNodeAncestorsGetter<T extends Node> {
    Set<Reference> getDirectAncestors(Reference nodeReference);
}
