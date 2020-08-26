package org.janelia.model.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.janelia.model.domain.tiledMicroscope.TmGeoAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmNeuronUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronUtils.class);

    /**
     * Given a collection of annotations, under a common neuron, make
     * annotations for each in the database, preserving the linkages implied in
     * the "value" target of the map provided.
     *
     * @param annotations map of node offset id vs "unserialized" annotation.
     * @param nodeParentLinkage map of node offset id vs parent node offset id.
     */
    public static void addLinkedGeometricAnnotationsInMemory(Map<Integer, Integer> nodeParentLinkage,
                                                             Map<Integer, TmGeoAnnotation> annotations,
                                                             TmNeuronMetadata tmNeuronMetadata,
                                                             Supplier<Long> neuronIdGenerator) {
        Long neuronId = tmNeuronMetadata.getId();
        int putativeRootCount = 0;
        // Cache to avoid re-fetch.
        Map<Integer, Long> nodeIdToAnnotationId = new HashMap<>();
        // Ensure the order of progression through nodes matches node IDs.
        Set<Integer> sortedKeys = new TreeSet<>(annotations.keySet());
        for (Integer nodeId : sortedKeys) {
            boolean isRoot = false;
            TmGeoAnnotation unlinkedAnnotation = annotations.get(nodeId);

            // Establish node linkage.
            Integer parentIndex = nodeParentLinkage.get(nodeId);
            Long parentAnnotationId = null;
            if (parentIndex != null && parentIndex != -1) {
                // NOTE: unless the annotation has been processed as
                // below, prior to now, the parent ID will be null.
                parentAnnotationId = nodeIdToAnnotationId.get(parentIndex);
                if (parentAnnotationId == null) {
                    parentAnnotationId = neuronId;
                }
            } else {
                putativeRootCount++;
                parentAnnotationId = neuronId;
                isRoot = true;
            }

            // Make the actual annotation, and save its linkage
            // through its original node id.
            TmGeoAnnotation linkedAnnotation = createGeometricAnnotationInMemory(tmNeuronMetadata,
                    isRoot,
                    parentAnnotationId,
                    unlinkedAnnotation,
                    neuronIdGenerator);
            TmGeoAnnotation parentAnnotation = tmNeuronMetadata.getParentOf(linkedAnnotation);
            if (parentAnnotation != null) {
                parentAnnotation.addChild(linkedAnnotation);
            }
            nodeIdToAnnotationId.put(nodeId, linkedAnnotation.getId());

            LOG.trace("Node " + nodeId + " at " + linkedAnnotation.toString() + ", has id " + linkedAnnotation.getId()
                    + ", has parent " + linkedAnnotation.getParentId() + ", under neuron " + linkedAnnotation.getNeuronId());
        }

        if (putativeRootCount > 1) {
            LOG.warn("Number of nodes with neuron as parent is " + putativeRootCount);
        }
    }

    private static TmGeoAnnotation createGeometricAnnotationInMemory(TmNeuronMetadata neuron,
                                                                     boolean isRoot,
                                                                     Long parentAnnotationId,
                                                                     TmGeoAnnotation unserializedAnno,
                                                                     Supplier<Long> neuronIdGenerator) {
        return createGeometricAnnotationInMemory(neuron,
                isRoot,
                parentAnnotationId,
                unserializedAnno.getX(), unserializedAnno.getY(), unserializedAnno.getZ(), unserializedAnno.getRadius(),
                neuronIdGenerator);
    }

    private static TmGeoAnnotation createGeometricAnnotationInMemory(TmNeuronMetadata tmNeuronMetadata,
                                                                     boolean isRoot,
                                                                     Long parentAnnotationId,
                                                                     double x, double y, double z, double radius,
                                                                     Supplier<Long> neuronIdGenerator) {

        Long generatedId = neuronIdGenerator.get();
        Date now = new Date();
        TmGeoAnnotation geoAnnotation = new TmGeoAnnotation(generatedId, parentAnnotationId, tmNeuronMetadata.getId(), x, y, z, radius, now, now);
        tmNeuronMetadata.getGeoAnnotationMap().put(geoAnnotation.getId(), geoAnnotation);
        if (isRoot) {
            tmNeuronMetadata.addRootAnnotation(geoAnnotation);
        } else {
            if (parentAnnotationId==null) {
                LOG.error("Non-root geometric annotation has null parent id for neuron "+generatedId);
            }
        }
        return geoAnnotation;
    }

}
