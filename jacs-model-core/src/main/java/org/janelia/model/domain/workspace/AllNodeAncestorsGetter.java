package org.janelia.model.domain.workspace;

import java.util.HashSet;
import java.util.Set;

import org.janelia.model.domain.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllNodeAncestorsGetter<T extends Node> implements NodeAncestorsGetter<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AllNodeAncestorsGetter.class);

    private final DirectNodeAncestorsGetter<T> directNodeAncestorsGetter;

    public AllNodeAncestorsGetter(DirectNodeAncestorsGetter<T> directNodeAncestorsGetter) {
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
