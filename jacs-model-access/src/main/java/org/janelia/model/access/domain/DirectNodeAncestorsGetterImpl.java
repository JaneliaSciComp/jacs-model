package org.janelia.model.access.domain;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.janelia.model.access.domain.dao.NodeDao;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.DirectNodeAncestorsGetter;
import org.janelia.model.domain.workspace.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectNodeAncestorsGetterImpl<T extends Node> implements DirectNodeAncestorsGetter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DirectNodeAncestorsGetterImpl.class);

    private final NodeDao<T> nodeDao;

    @Inject
    public DirectNodeAncestorsGetterImpl(NodeDao<T> nodeDao) {
        this.nodeDao = nodeDao;
    }

    @Override
    public Set<Reference> getDirectAncestors(Reference nodeReference) {
        return getDirectAncestorsForNodeRef(nodeReference);
    }

    private Set<Reference> getDirectAncestorsForNodeRef(Reference reference) {
        LOG.debug("Start loading direct node ancestors for {}", reference);
        try {
            return nodeDao.getNodeDirectAncestors(reference).stream().map(Reference::createFor).collect(Collectors.toSet());
        } finally {
            LOG.debug("Finished loading direct node ancestors for {}", reference);
        }
    }

}
