package org.janelia.model.domain.workspace;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.domain.Reference;

/**
 * Utilities for implementing a node, since Java does not support mix-ins.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class NodeUtils {

    public static void traverseAllAncestors(Reference nodeReference, DirectNodeAncestorsGetter directNodeAncestorsMap, Consumer<Reference> ancestorAction, int howManyLevels) {
        Set<Reference> directNodeAncestors = directNodeAncestorsMap.getDirectAncestors(nodeReference);
        if (CollectionUtils.isNotEmpty(directNodeAncestors)) {
            Set<Reference> allAncestors = new LinkedHashSet<>();
            allAncestors.add(nodeReference);
            traverseAllAncestors(directNodeAncestorsMap, allAncestors, directNodeAncestors.stream().map(nodeAncestor -> ImmutablePair.of(nodeAncestor, howManyLevels > 0 ? howManyLevels : -1)).collect(Collectors.toSet()), ancestorAction);
        }
    }

    private static void traverseAllAncestors(DirectNodeAncestorsGetter directNodeAncestorsMap, Set<Reference> visitedNodes, Set<Pair<Reference, Integer>> remainingNodes, Consumer<Reference> ancestorAction) {
        for (Set<Pair<Reference, Integer>> uninspectedAncestorNodes = remainingNodes; !uninspectedAncestorNodes.isEmpty();) {
            uninspectedAncestorNodes = uninspectedAncestorNodes.stream()
                    .filter(nodeRefWithLevel -> nodeRefWithLevel.getRight() != 0)
                    .flatMap(nodeRefWithLevel -> {
                        ancestorAction
                                .andThen(nodeRef -> visitedNodes.add(nodeRef))
                                .accept(nodeRefWithLevel.getLeft());
                        Set<Reference> directNodeAncestors = directNodeAncestorsMap.getDirectAncestors(nodeRefWithLevel.getLeft());
                        if (CollectionUtils.isNotEmpty(directNodeAncestors)) {
                            return directNodeAncestors.stream().map(nr -> ImmutablePair.of(nr, nodeRefWithLevel.getRight() > 0 ? nodeRefWithLevel.getRight() - 1 : -1));
                        } else {
                            return Stream.of();
                        }
                    })
                    .filter(nodeReRefWithLevel -> !visitedNodes.contains(nodeReRefWithLevel.getLeft()))
                    .collect(Collectors.toSet());
        }
    }

}
