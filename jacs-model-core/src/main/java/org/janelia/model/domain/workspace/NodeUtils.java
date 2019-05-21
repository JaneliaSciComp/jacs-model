package org.janelia.model.domain.workspace;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.domain.Reference;

/**
 * Utilities for implementing a node, since Java does not support mix-ins.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class NodeUtils {

    public static void traverseAllAncestors(Reference nodeReference, NodeAncestorsGetter directNodeAncestorsMap, Consumer<Reference> ancestorAction) {
        Set<Reference> directNodeAncestors = directNodeAncestorsMap.getNodeAncestors(nodeReference);
        if (CollectionUtils.isNotEmpty(directNodeAncestors)) {
            Set<Reference> allAncestors = new LinkedHashSet<>();
            allAncestors.add(nodeReference);
            traverseAllAncestors(directNodeAncestorsMap, allAncestors, directNodeAncestors, ancestorAction);
        }
    }

    private static void traverseAllAncestors(NodeAncestorsGetter directNodeAncestorsMap, Set<Reference> visitedNodes, Set<Reference> remainingNodes, Consumer<Reference> ancestorAction) {
        for (Set<Reference> uninspectedAncestorNodes = remainingNodes; !uninspectedAncestorNodes.isEmpty();) {
            uninspectedAncestorNodes = uninspectedAncestorNodes.stream()
                    .flatMap(nodeReference -> {
                        ancestorAction
                                .andThen(nr -> visitedNodes.add(nr))
                                .accept(nodeReference);
                        Set<Reference> directNodeAncestors = directNodeAncestorsMap.getNodeAncestors(nodeReference);
                        if (CollectionUtils.isNotEmpty(directNodeAncestors)) {
                            return directNodeAncestors.stream();
                        } else {
                            return Stream.of();
                        }
                    })
                    .filter(node -> !visitedNodes.contains(node))
                    .collect(Collectors.toSet());
        }
    }

}
