package org.janelia.model.access.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.janelia.model.access.cdi.WithCache;
import org.janelia.model.access.domain.dao.TreeNodeDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.SimpleDomainAnnotation;
import org.janelia.model.domain.workspace.DirectNodeAncestorsGetter;
import org.janelia.model.domain.workspace.NodeAncestorsGetter;
import org.janelia.model.domain.workspace.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WithCache
public class CachedAllNodeAncestorsGetterImpl implements NodeAncestorsGetter {

    private static final Logger LOG = LoggerFactory.getLogger(CachedAllNodeAncestorsGetterImpl.class);

    private final TreeNodeDao treeNodeDao;
    private final Map<Reference, Set<Reference>> nodeAncestorsCache;

    @Inject
    public CachedAllNodeAncestorsGetterImpl(TreeNodeDao treeNodeDao) {
        this.treeNodeDao = treeNodeDao;
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
                    treeNodeDao.streamAll()
                            .flatMap(tn -> {
                                if (tn.hasChildren()) {
                                    return tn.getChildren().stream().map(childRef -> ImmutablePair.of(childRef, Reference.createFor(tn)));
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
