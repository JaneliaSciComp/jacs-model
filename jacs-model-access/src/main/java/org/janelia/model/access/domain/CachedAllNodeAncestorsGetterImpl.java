package org.janelia.model.access.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.janelia.model.access.domain.dao.NodeDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.domain.workspace.NodeAncestorsGetter;
import org.janelia.model.domain.workspace.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedAllNodeAncestorsGetterImpl<T extends Node> implements NodeAncestorsGetter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(CachedAllNodeAncestorsGetterImpl.class);

    private final NodeDao<T> nodeDao;
    private final Map<Reference, Set<Reference>> nodeAncestorsCache;

    public CachedAllNodeAncestorsGetterImpl(NodeDao<T> nodeDao) {
        this.nodeDao = nodeDao;
        this.nodeAncestorsCache = loadAllNodeAncestors();
    }

    @Override
    public Set<Reference> getNodeAncestors(Reference nodeReference) {
        return nodeAncestorsCache.getOrDefault(nodeReference, Collections.emptySet());
    }

    private Map<Reference, Set<Reference>> loadAllNodeAncestors() {
        LOG.info("Start loading all node ancestors cache");
        try {
            Map<Reference, Set<Reference>> directAncestors =
                    nodeDao.streamAll()
                            .flatMap(tn -> {
                                if (tn.hasChildren()) {
                                    return tn.getChildren()
                                            .stream()
                                            .filter(Objects::nonNull)
                                            .map(childRef -> ImmutablePair.of(childRef, Reference.createFor(tn)));
                                } else {
                                    return Stream.of();
                                }
                            })
                            .collect(Collectors.groupingBy(ancestorNodePair -> ancestorNodePair.getLeft(),
                                    Collectors.mapping(ancestorNodePair -> ancestorNodePair.getRight(), Collectors.toSet())));
            Map<Reference, Set<Reference>> allAncestorsMap = new HashMap<>();
            directAncestors.forEach((nodeReference, nodeDirectAncestors) -> {
                Set<Reference> nodeAncestors = new HashSet<>();
                NodeUtils.traverseAllAncestors(nodeReference, directAncestors::get, ref -> nodeAncestors.add(ref), -1);
                allAncestorsMap.put(nodeReference, nodeAncestors);
            });
            return allAncestorsMap;
        } finally {
            LOG.info("Finished loading all node ancestors cache");
        }
    }


}
