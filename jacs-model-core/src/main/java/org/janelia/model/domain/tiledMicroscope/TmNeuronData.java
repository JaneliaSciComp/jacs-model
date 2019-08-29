package org.janelia.model.domain.tiledMicroscope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.protostuff.Tag;
import org.janelia.model.util.MapFacade;

/**
 * Tiled microscope neuron annotations stored in protobuf format.
 *
 * Requires no-args constructor for use with Protostuff/Protobuf.
 *
 * @author murphys
 */
public class TmNeuronData implements Serializable {

    @Tag(1)
    private final List<TmGeoAnnotation> geoAnnotations = new ArrayList<>();
    @Tag(2)
    private final List<Long> rootAnnotationIds = new ArrayList<>();
    @Tag(3)
    private final List<TmAnchoredPath> anchoredPaths = new ArrayList<>();
    @Tag(4)
    private final List<TmStructuredTextAnnotation> textAnnotations = new ArrayList<>();

    /*
    This class used to serialize maps of data, but that's highly inefficient. In order to move to a better list-based representation, 
    we use these map facades are used to simulate the old maps so that we don't need to rewrite all the client code. They are transient 
    and not serialized to protobuf. They need to be instantiated lazily, because protobuf surreptitiously replaces the list instances 
    above when deserializing. The instances above are only used when creating a new instance.
     */
    transient private Map<Long, TmGeoAnnotation> geoAnnotationMap;
    transient private Map<TmAnchoredPathEndpoints, TmAnchoredPath> anchoredPathMap;
    transient private Map<Long, TmStructuredTextAnnotation> textAnnotationMap;

    public TmNeuronData() {
    }
    
    public List<Long> getRootAnnotationIds() {
        return rootAnnotationIds;
    }

    // maps geo ann ID to geo ann object
    public Map<Long, TmGeoAnnotation> getGeoAnnotationMap() {
        if (geoAnnotationMap==null) {
                geoAnnotationMap = new MapFacade<Long, TmGeoAnnotation>(geoAnnotations) {
                @Override
                public Long getKey(TmGeoAnnotation object) {
                    return object.getId();
                }
            };
        }
        return geoAnnotationMap;
    }

    // maps endpoints of path to path object
    public Map<TmAnchoredPathEndpoints, TmAnchoredPath> getAnchoredPathMap() {
        if (anchoredPathMap==null) {
            anchoredPathMap = new MapFacade<TmAnchoredPathEndpoints, TmAnchoredPath>(anchoredPaths) {
                @Override
                public TmAnchoredPathEndpoints getKey(TmAnchoredPath object) {
                    return object.getEndpoints();
                }
            };
        }
        return anchoredPathMap;
    }

    // maps ID of parent to text annotation
    public Map<Long, TmStructuredTextAnnotation> getStructuredTextAnnotationMap() {
        if (textAnnotationMap==null) {
            textAnnotationMap = new MapFacade<Long, TmStructuredTextAnnotation>(textAnnotations) {
                @Override
                public Long getKey(TmStructuredTextAnnotation object) {
                    return object.getParentId();
                }
            };
        }
        return textAnnotationMap;
    }

    String getDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("rootAnnotationIds:\n");
        for(Long rootAnnotationId : new ArrayList<>(rootAnnotationIds)) {
            sb.append("    ").append(rootAnnotationId).append("\n");
        }
        sb.append("geoAnnotations:\n");
        for(TmGeoAnnotation geoAnnotation : new ArrayList<>(geoAnnotations)) {
            sb.append("    ").append(geoAnnotation.getId()).append(" at (").append(geoAnnotation).append(")\n");
            for(Long childId : geoAnnotation.getChildIds()) {
            	sb.append("      child ").append(childId).append("\n");
            }
        }
        sb.append("anchoredPaths:\n");
        for(TmAnchoredPath anchoredPath : new ArrayList<>(anchoredPaths)) {
            sb.append("    ").append(anchoredPath).append("\n");
        }
        sb.append("textAnnotations:\n");
        for(TmStructuredTextAnnotation textAnnotation : new ArrayList<>(textAnnotations)) {
            sb.append("    ").append(textAnnotation.getDataString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * check a neuron for problems and potentially repair;
     * check that its relationships are consistent, and any
     * referred-to annotations are actually present
     *
     * note that these repairs are performed on the object;
     * the calling routine will need to persist the fixes
     *
     * returns list of problems found and/or fixed; empty
     * list = no problems
     */
    public List<String> checkRepairNeuron(Long neuronId, boolean repair) {

        List<Long> rootAnnotationIds = getRootAnnotationIds();
        Map<Long, TmGeoAnnotation> geoAnnotationMap = getGeoAnnotationMap();

        List<String> results = new ArrayList<>();

        boolean noRoot = false;
        
        // if there are annotations, is there at least one root?
        if (!geoAnnotationMap.isEmpty() && rootAnnotationIds.isEmpty()) {
            results.add("neuron has anns but no root");
            noRoot = true;
        }
        
        // are all roots in ann map?
        Set<Long> rootIDsNotInMap = new HashSet<>();
        for (Long rootID: rootAnnotationIds) {
        	if (geoAnnotationMap.isEmpty()) {
                results.add("ann ID " + rootID + " is a root but ann map is empty!");
                rootIDsNotInMap.add(rootID);
        	}
        	else if (!geoAnnotationMap.containsKey(rootID)) {
                results.add("ann ID " + rootID + " is a root but not in ann map: "+geoAnnotationMap.keySet());
                rootIDsNotInMap.add(rootID);
            }
        }
        if (repair) {
        	if (noRoot) {
        		TmGeoAnnotation ann = geoAnnotations.get(0);
                ann.setParentId(neuronId);
                rootAnnotationIds.add(ann.getId());
                results.add("promoted ann ID " + ann.getId() + " to root annotation");
        	}
        	
            // remove bad ID from root ID list
            for (Long r: rootIDsNotInMap) {
                rootAnnotationIds.remove(r);
                results.add("removed root ID " + r + " from root list");
            }

            for (TmGeoAnnotation ann: geoAnnotationMap.values()) {
            	// Fix null parents
                if (ann.getParentId()==null) {
                    results.add("promoted ann ID " + ann.getId() + " to root annotation because it has a null parent");
                	ann.setParentId(neuronId);
                }
                // check that no annotations have it as a parent;
                //  if one does, promote it to root (set parent to neuron,
                //  add to root list)
                if (rootIDsNotInMap.contains(ann.getParentId())) {
                    ann.setParentId(neuronId);
                    rootAnnotationIds.add(ann.getId());
                    results.add("promoted ann ID " + ann.getId() + " to root annotation");
                }
            }
        }

        // note on repairs: a lot of this isn't implemented yet; when
        //  you get to it, follow the model above: usually you'll want
        //  to record the problems in one loop, then loop over problems
        //  and solve them; that way you'll avoid concurrent modification issues


        // check annotation parents
        // all anns have parent in map?  roots in root list?
        for (TmGeoAnnotation ann: geoAnnotationMap.values()) {
            if (ann.getParentId().equals(neuronId)) {
                // if parent = neuron, it's a root; in the root list?
                if (!rootAnnotationIds.contains(ann.getId())) {
                    results.add("root ID " + ann.getId() + " not in root list");
                    if (repair) {
                        results.add("repair not implemented for this problem");
                        // to repair: add it to the root list
                    }
                }
            }
            else if (!geoAnnotationMap.containsKey(ann.getParentId())) {
                // otherwise, is parent in map?
                results.add("ann ID " + ann.getId() + "'s parent ("+ann.getParentId()+") not in ann map");
                if (repair) {
                    results.add("repair not implemented for this problem");
                    // to repair: promote the annotation to root (see above)
                }
            }
        }
        // check that endpoints of anchored paths are present; if not,
        //  remove the paths
        // (unimplemented)

        // check that structured text annotations are attached to valid things
        //  if not, remove them
        // (unimplemented)

        return results;
    }

}
