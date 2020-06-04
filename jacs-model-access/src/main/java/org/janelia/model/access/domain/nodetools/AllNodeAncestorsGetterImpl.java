package org.janelia.model.access.domain.nodetools;

import java.util.HashSet;
import java.util.Set;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllNodeAncestorsGetterImpl<T extends Node> implements NodeAncestorsGetter<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AllNodeAncestorsGetterImpl.class);

    private final DirectNodeAncestorsGetter<T> directNodeAncestorsGetter;

    public AllNodeAncestorsGetterImpl(DirectNodeAncestorsGetter<T> directNodeAncestorsGetter) {
        this.directNodeAncestorsGetter = directNodeAncestorsGetter;
    }

    public Set<Reference> getNodeAncestors(Reference nodeReference) {
        LOG.debug("Start loading all node ancestors for {}", nodeReference);
        try {
            Set<Reference> nodeAncestors = new HashSet<>();
            NodeUtils.traverseAllAncestors(nodeReference, directNodeAncestorsGetter, ref -> nodeAncestors.add(ref), -1);
            return nodeAncestors;
        } finally {
            LOG.debug("Finished loading all node ancestors for {}", nodeReference);
        }
    }
}
