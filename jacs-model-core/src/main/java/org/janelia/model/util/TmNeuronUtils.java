package org.janelia.model.util;

import org.janelia.model.domain.tiledMicroscope.TmNeuronAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmNeuron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

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
                                                             Map<Integer, TmNeuronAnnotation> annotations,
                                                             TmNeuron TmNeuron,
                                                             Supplier<Long> neuronIdGenerator) {
        Long neuronId = TmNeuron.getId();
        int putativeRootCount = 0;
        // Cache to avoid re-fetch.
        Map<Integer, Long> nodeIdToAnnotationId = new HashMap<>();
        // Ensure the order of progression through nodes matches node IDs.
        Set<Integer> sortedKeys = new TreeSet<>(annotations.keySet());
        for (Integer nodeId : sortedKeys) {
            boolean isRoot = false;
            TmNeuronAnnotation unlinkedAnnotation = annotations.get(nodeId);

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
            TmNeuronAnnotation linkedAnnotation = createGeometricAnnotationInMemory(TmNeuron,
                    isRoot,
                    parentAnnotationId,
                    unlinkedAnnotation,
                    neuronIdGenerator);
            TmNeuronAnnotation parentAnnotation = TmNeuron.getParentOf(linkedAnnotation);
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

    private static TmNeuronAnnotation createGeometricAnnotationInMemory(TmNeuron neuron,
                                                                        boolean isRoot,
                                                                        Long parentAnnotationId,
                                                                        TmNeuronAnnotation unserializedAnno,
                                                                        Supplier<Long> neuronIdGenerator) {
        return createGeometricAnnotationInMemory(neuron,
                isRoot,
                parentAnnotationId,
                unserializedAnno.getX(), unserializedAnno.getY(), unserializedAnno.getZ(), unserializedAnno.getRadius(),
                neuron.getId(),
                neuronIdGenerator);
    }

    private static TmNeuronAnnotation createGeometricAnnotationInMemory(TmNeuron TmNeuron,
                                                                        boolean isRoot,
                                                                        Long parentAnnotationId,
                                                                        double x, double y, double z, double radius,
                                                                        Long neuronId,
                                                                        Supplier<Long> neuronIdGenerator) {

        Long generatedId = neuronIdGenerator.get();
        Date now = new Date();
        TmNeuronAnnotation geoAnnotation = new TmNeuronAnnotation(generatedId, parentAnnotationId, TmNeuron.getId(), x, y, z, radius, now, now);
        TmNeuron.getGeoAnnotationMap().put(geoAnnotation.getId(), geoAnnotation);
        if (isRoot) {
            TmNeuron.addRootAnnotation(geoAnnotation);
        }
        else {
            if (parentAnnotationId==null) {
                LOG.error("Non-root geometric annotation has null parent id for neuron "+generatedId);
            }
        }
        return geoAnnotation;
    }

}
