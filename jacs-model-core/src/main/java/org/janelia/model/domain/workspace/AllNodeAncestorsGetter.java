package org.janelia.model.domain.workspace;

import java.util.HashSet;
import java.util.Set;

import org.janelia.model.domain.DataSupplier;
import org.janelia.model.domain.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllNodeAncestorsGetter implements NodeAncestorsGetter, DataSupplier<Reference, Set<Reference>> {
    private static final Logger LOG = LoggerFactory.getLogger(AllNodeAncestorsGetter.class);

    private final DirectNodeAncestorsGetter directNodeAncestorsGetter;

    public AllNodeAncestorsGetter(DirectNodeAncestorsGetter directNodeAncestorsGetter) {
        this.directNodeAncestorsGetter = directNodeAncestorsGetter;
    }

    public Set<Reference> getNodeAncestors(Reference nodeReference) {
        LOG.info("Start loading all node ancestors for {}", nodeReference);
        try {
            Set<Reference> nodeAncestors = new HashSet<>();
            NodeUtils.traverseAllAncestors(nodeReference, directNodeAncestorsGetter, ref -> nodeAncestors.add(ref), -1);
            return nodeAncestors;
        } finally {
            LOG.info("Finished loading all node ancestors for {}", nodeReference);
        }
    }


}
