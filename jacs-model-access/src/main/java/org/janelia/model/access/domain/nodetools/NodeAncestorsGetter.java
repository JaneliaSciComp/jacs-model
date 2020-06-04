package org.janelia.model.access.domain.nodetools;

import java.util.Set;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;

public interface NodeAncestorsGetter<T extends Node> {
    Set<Reference> getNodeAncestors(Reference nodeReference);
}
