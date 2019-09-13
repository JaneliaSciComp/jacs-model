package org.janelia.model.access.domain;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.DirectNodeAncestorsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectNodeAncestorsGetterImpl implements DirectNodeAncestorsGetter {

    private static final Logger LOG = LoggerFactory.getLogger(DirectNodeAncestorsGetterImpl.class);

    private final TreeNodeDao treeNodeDao;

    @Inject
    public DirectNodeAncestorsGetterImpl(TreeNodeDao treeNodeDao) {
        this.treeNodeDao = treeNodeDao;
    }

    @Override
    public Set<Reference> getDirectAncestors(Reference nodeReference) {
        return getDirectAncestorsForNodeRef(nodeReference);
    }

    private Set<Reference> getDirectAncestorsForNodeRef(Reference reference) {
        LOG.debug("Start loading direct node ancestors for {}", reference);
        try {
            return treeNodeDao.getNodeDirectAncestors(reference).stream().map(tn -> Reference.createFor(tn)).collect(Collectors.toSet());
        } finally {
            LOG.debug("Finished loading direct node ancestors for {}", reference);
        }
    }

}
