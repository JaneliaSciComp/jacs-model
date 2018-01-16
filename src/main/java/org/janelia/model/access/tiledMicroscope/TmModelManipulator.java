package org.janelia.model.access.tiledMicroscope;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.janelia.model.domain.tiledMicroscope.TmAnchoredPath;
import org.janelia.model.domain.tiledMicroscope.TmAnchoredPathEndpoints;
import org.janelia.model.domain.tiledMicroscope.TmGeoAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmStructuredTextAnnotation;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.util.IdSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the relationships, additions and deletions from Tiled Microscope
 * data, based at a Workspace.
 *
 * Implementation note: be careful regarding neuron persistence!  Some methods
 * in this class persist neurons, and others do not (letting the calling
 * routine persist).  You should only persist neurons exactly once, after all
 * operations are complete.
 *
 * @author fosterl
 */
public class TmModelManipulator {

    private static final Logger log = LoggerFactory.getLogger(TmModelManipulator.class);

    private final TmModelAdapter dataSource;
    private final IdSource idSource;
    
    private final Map<Long, TmNeuronMetadata> neuronMap = new ConcurrentHashMap<>();
    private final Map<String, List> neuronsInWaiting = new ConcurrentHashMap<>();
    private CompletableFuture<Boolean> ownershipRequest;

    public TmModelManipulator(TmModelAdapter dataSource) {
        this(dataSource, new IdSource());
    }

    public TmModelManipulator(TmModelAdapter dataSource, IdSource idSource) {
        this.dataSource = dataSource;
        this.idSource = idSource;
    }
    
    public Collection<TmNeuronMetadata> getNeurons() {
        return neuronMap.values();
    }

    public void addNeuron(TmNeuronMetadata neuron) {
        // if neuron exists, do a deep merge so references throughout Horta/LVV
        // pick up the changes
        if (neuronMap.containsKey(neuron.getId())) {
            neuronMap.get(neuron.getId()).merge(neuron);
        } else {
            neuronMap.put(neuron.getId(), neuron);
        }
    }

    /**
     * neurons submitted for creation are waiting around for callback from message server
     * store in a map by name for async updating when callback from message server arrives
     */
    public void addNeuronInWaiting(String name, List neuronData) {
        neuronsInWaiting.put(name, neuronData);
    }

    public TmNeuronMetadata removeNeuron(TmNeuronMetadata neuron) {
        return neuronMap.remove(neuron.getId());
    }
    
    public TmNeuronMetadata getNeuronById(Long id) {
        return neuronMap.get(id);
    }
    
    /**
     * The workspace's neurons will not have been loaded with the workspace,
     * because we wish to be able to load them from any client, including
     * one which happens to be on the server.
     */
    public void loadWorkspaceNeurons(TmWorkspace workspace) throws Exception {
        neuronMap.clear();
        for(TmNeuronMetadata neuron : dataSource.loadNeurons(workspace)) {
            addNeuron(neuron);
        }
    }

    public void mergeCreatedNeuron(TmNeuronMetadata newNeuron) {
        String key = newNeuron.getName();
        if (this.neuronsInWaiting.containsKey(newNeuron.getName())) {
            addNeuron(newNeuron);
            List neuronData = neuronsInWaiting.get(newNeuron.getName());

            // check if callbacks
            if (neuronData.size()>1) {
                Consumer<Map<String,Object>> callback = (Consumer<Map<String,Object>>)neuronData.get(1);
                Map<String,Object> parameters = (Map<String,Object>)neuronData.get(2);
                parameters.put("neuron", newNeuron);
                callback.accept(parameters);
            }
            neuronsInWaiting.remove(neuronsInWaiting);
        }
    }

    /**
     * We make an ownership request to take ownership of this neuron; we'd like to perform a fast
     * block in this case in the calling function and fulfill the future (hopefully rapidly)
     * when the approval request comes in from the NeuronBroker.
     * @param neuron
     * @throws Exception
     */
    public CompletableFuture<Boolean> requestOwnershipChange(TmNeuronMetadata neuron) throws Exception {
        ownershipRequest = dataSource.requestOwnership(neuron);
        return ownershipRequest;
    }

    /**
     * We complete the ownership request future once we get the decision from the NeuronBroker
     * @param decision
     * @throws Exception
     */
    public void completeOwnershipRequest(boolean decision) {
        if (ownershipRequest!=null) {
            ownershipRequest.complete(new Boolean(decision));
        }
    }


    /**
     * Makes a new neuron.
     *
     * @todo may need to add create, update dates + ownerKey
     * @param workspace will contain this neuron.
     * @param name of new neuron.
     * @return that neuron
     * @throws Exception
     */
    public TmNeuronMetadata createTiledMicroscopeNeuron(TmWorkspace workspace, String name) throws Exception {
        if (workspace == null || name == null) {
            throw new IllegalStateException("Tiled Neuron must be created in a valid workspace.");
        }
        TmNeuronMetadata neuron = new TmNeuronMetadata(workspace, name);
        List neuronList = new ArrayList();
        neuronList.add(neuron);
        addNeuronInWaiting(name, neuronList);
        createTiledMicroscopeNeuron(neuron);
        return neuron;
    }

    /**
     * adds a callback for finish async activities, like SWC import
     * @param workspace
     * @param name
     * @return
     * @throws Exception
     */
    public TmNeuronMetadata createTiledMicroscopeNeuron(TmWorkspace workspace,
                                                        String name,
                                                        Consumer<Map<String,Object>> callback,
                                                        Map<String,Object> parameters) throws Exception {
        if (workspace == null || name == null) {
            throw new IllegalStateException("Tiled Neuron must be created in a valid workspace.");
        }
        TmNeuronMetadata neuron = new TmNeuronMetadata(workspace, name);
        List neuronList = new ArrayList();
        neuronList.add(neuron);
        neuronList.add(callback);
        neuronList.add(parameters);
        addNeuronInWaiting(name, neuronList);
        createTiledMicroscopeNeuron(neuron);
        return neuron;
    }

    private void createTiledMicroscopeNeuron(TmNeuronMetadata neuron) throws Exception {
        if (dataSource == null) {
            throw new IllegalStateException("Cannot create neuron without data source.");
        }
        dataSource.asyncCreateNeuron(neuron);
    }
    
    /**
     * Add an anchored path to an existing neuron.
     * 
     * @todo may need to add create, update dates + ownerKey
     * @param tmNeuronMetadata add path to this.
     * @return the anchored path thus created.
     * @throws Exception 
     */
    public TmAnchoredPath addAnchoredPath(TmNeuronMetadata tmNeuronMetadata, Long annotationID1, Long annotationID2,
                                          List<List<Integer>> pointlist) throws Exception {
        if (!tmNeuronMetadata.getGeoAnnotationMap().containsKey(annotationID1)
                || !tmNeuronMetadata.getGeoAnnotationMap().containsKey(annotationID2)) {
            throw new Exception("both annotations must be in neuron");
        }
        final TmAnchoredPathEndpoints key = new TmAnchoredPathEndpoints(annotationID1, annotationID2);
        final TmAnchoredPath value = new TmAnchoredPath( idSource.next(), key, pointlist );
        tmNeuronMetadata.getAnchoredPathMap().put(key,value);
        saveNeuronData(tmNeuronMetadata);
        return value;        
    }

    /**
     * Geometric annotations are essentially points in the growing neuron.
     * 
     * @todo may need to add create, update dates + ownerKey
     * @param tmNeuronMetadata will receive this point.
     * @param parentAnnotationId linkage.
     * @param x cords...
     * @param y
     * @param z
     * @return the completed point-rep.
     * @throws Exception 
     */
    public TmGeoAnnotation addGeometricAnnotation(
            TmNeuronMetadata tmNeuronMetadata, Long parentAnnotationId,
            double x, double y, double z
    ) throws Exception {
        TmGeoAnnotation rtnVal = new TmGeoAnnotation();
        rtnVal.setX(x);
        rtnVal.setY(y);
        rtnVal.setZ(z);
        rtnVal.setCreationDate(new Date());
        rtnVal.setNeuronId(tmNeuronMetadata.getId());
        rtnVal.setParentId(parentAnnotationId);
        rtnVal.setId(idSource.next());

        // If non-root, add this as a child of its parent.
        if (parentAnnotationId != null) {
            TmGeoAnnotation parent = tmNeuronMetadata.getGeoAnnotationMap().get(parentAnnotationId);
            // Parent might be the neuron itself, if this is a root.
            // Otherwise, ensure the inter-annotation linkage.
            if (parent != null) {
                parent.addChild(rtnVal);
            }
        }
		// Handle root geo annotations.
		if (parentAnnotationId == null  ||  parentAnnotationId == tmNeuronMetadata.getId()) {
	        tmNeuronMetadata.addRootAnnotation(rtnVal);
		}

        tmNeuronMetadata.getGeoAnnotationMap().put(rtnVal.getId(), rtnVal);

        saveNeuronData(tmNeuronMetadata);
        // Ensure that the geo-annotation known to the neuron, after the
        // save, is the one we return.  Get the other one out of circulation.
        rtnVal = tmNeuronMetadata.getGeoAnnotationMap().get(rtnVal.getId());
        return rtnVal;        
    }

    /**
     * Change the parentage of the annotation to the new annotation ID.  This
     * operation is partial.  It can be applied to annotations which are
     * incomplete.  Therefore, this operation does not carry out exchange with
     * the database.
     * 
     * @todo may need to add create, update dates + ownerKey
     * @param annotation this gets different parent.
     * @param newParentAnnotationID this becomes the new parent.
     * @param tmNeuronMetadata the annotation and new parent must be under this neuron.
     * @throws Exception thrown if condition(s) above not met.
     */
    public void reparentGeometricAnnotation(
            TmGeoAnnotation annotation, Long newParentAnnotationID,
            TmNeuronMetadata tmNeuronMetadata
    ) throws Exception {
        
        // verify that both annotations are in the input neuron
        if (!tmNeuronMetadata.getGeoAnnotationMap().containsKey(annotation.getId())) {
            throw new Exception("input neuron doesn't contain child annotation " + annotation.getId());
        }
        if (!tmNeuronMetadata.getGeoAnnotationMap().containsKey(newParentAnnotationID)) {
            throw new Exception("input neuron doesn't contain new parent annotation " + newParentAnnotationID);
        }
        
        // is it already the parent?
        if (annotation.getParentId().equals(newParentAnnotationID)) {
            return;
        }
        
        // Ensure: using the very same copy that is known to the neuron.
        annotation = tmNeuronMetadata.getGeoAnnotationMap().get(annotation.getId());
        
        // do NOT create cycles! new parent cannot be in original annotation's subtree:
        for (TmGeoAnnotation testAnnotation : tmNeuronMetadata.getSubTreeList(annotation)) {
            if (newParentAnnotationID.equals(testAnnotation.getId())) {
                return;
            }
        }
        
        // The reparented annotation will no longer be a root.
        if (annotation.isRoot()) {
            tmNeuronMetadata.removeRootAnnotation(annotation);
        }

        // Change child/down linkage.
        TmGeoAnnotation parentAnnotation = tmNeuronMetadata.getParentOf(annotation);
        if (parentAnnotation != null) {
            parentAnnotation.getChildIds().remove(annotation.getId());
        }
        // Change parent ID.
        annotation.setParentId(newParentAnnotationID);
        TmGeoAnnotation newParentAnnotation = tmNeuronMetadata.getGeoAnnotationMap().get(newParentAnnotationID);
        // Belt-and-suspenders: above tests that the map has this ID.
        if (newParentAnnotation != null) {
            newParentAnnotation.getChildIds().add(annotation.getId());
        }
    }

    // @todo may need to add create, update dates + ownerKey
    public TmStructuredTextAnnotation addStructuredTextAnnotation(TmNeuronMetadata neuron, Long parentID, String data) throws Exception {

        // parent must not already have a structured text annotation
        if (neuron.getStructuredTextAnnotationMap().containsKey(parentID)) {
            throw new Exception("parent ID already has a structured text annotation; use update, not add");
        }

        TmStructuredTextAnnotation annotation = new TmStructuredTextAnnotation(idSource.next(), parentID, data);
        
        neuron.getStructuredTextAnnotationMap().put( parentID, annotation );

        return annotation;
    }
	
	public void updateStructuredTextAnnotation(TmNeuronMetadata neuron, TmStructuredTextAnnotation annotation, String jsonString) throws Exception {
        // note for the future: this method and the delete one below ought to be just
        //  in-lined in AnnotationModel; maybe the add method above, too
        annotation.setDataString(jsonString);
		neuron.getStructuredTextAnnotationMap().put(annotation.getParentId(), annotation);
    }
	
	public void deleteStructuredTextAnnotation(TmNeuronMetadata neuron, long annotationId) {
		if (neuron.getStructuredTextAnnotationMap().containsKey(annotationId)) {
			neuron.getStructuredTextAnnotationMap().remove(annotationId);
		}
    }

    // for debugging, if needed
    @SuppressWarnings("unused")
    private void printStructureTextAnnotationMap(TmNeuronMetadata neuron) {
        Map<Long, TmStructuredTextAnnotation> staMap = neuron.getStructuredTextAnnotationMap();
        System.out.println("structure text map for neuron " + neuron.getName() + ", neuron ID " + neuron.getId());
        for (Long annID: staMap.keySet()) {
            TmStructuredTextAnnotation sta = staMap.get(annID);
            System.out.println("    key:" + annID + " str text ID: " + sta.getId() + " parent ID: "  + annID +
                " data string:" + sta.getDataString());
        }
    }

    /**
     * This can be a partial operation.  Therefore, it will not carry out any
     * communication with the databse.
     * 
     * @param tmNeuronMetadata gets a new root.
     * @param newRoot becomes a root, and added to neuron.
     * @throws Exception 
     */
    public void rerootNeurite(TmNeuronMetadata tmNeuronMetadata, TmGeoAnnotation newRoot) throws Exception {
        if (newRoot == null || tmNeuronMetadata == null) {
            return;
        }
                
        if (!tmNeuronMetadata.getGeoAnnotationMap().containsKey(newRoot.getId())) {
            throw new Exception(String.format("input neuron %d doesn't contain new root annotation %d",
                    tmNeuronMetadata.getId(), newRoot.getId()));
        }

        // is it already a root?
        if (newRoot.isRoot()) {
            return;
        }

        // from input, follow parents up to current root, keeping them all
        List<TmGeoAnnotation> parentList = new ArrayList<>();
        TmGeoAnnotation testAnnotation = newRoot;
        while (!testAnnotation.isRoot()) {
            parentList.add(testAnnotation);
            testAnnotation = tmNeuronMetadata.getParentOf(testAnnotation);
        }
        parentList.add(testAnnotation);
        Long neuronId = tmNeuronMetadata.getId();
        testAnnotation.setNeuronId(neuronId);

        // reparent intervening annotations; skip the first item, which is the
        //  new root (which we've already dealt with)
        log.info("Inverting child/parent relationships.");
        for (int i = 1; i < parentList.size(); i++) {
            // change the parent ID/add as child, and save
            TmGeoAnnotation ann = parentList.get(i);

            // Remove from old parent.
            TmGeoAnnotation oldParentAnnotation = tmNeuronMetadata.getParentOf(ann);
            if (oldParentAnnotation != null) {
                oldParentAnnotation.getChildIds().remove(ann.getId());
            }
            else if (ann.isRoot()) {
                tmNeuronMetadata.removeRootAnnotation(ann);
            }
            
            Long newParentAnnotationID = parentList.get(i - 1).getId();
            ann.setParentId(newParentAnnotationID);
            // If it is now the parent, it cannot any longer be a child.
            ann.getChildIds().remove(newParentAnnotationID);
            ann.setNeuronId(neuronId);
            
            // Add to new parent.
            TmGeoAnnotation newParentAnnotation = tmNeuronMetadata.getGeoAnnotationMap().get(newParentAnnotationID);
            newParentAnnotation.addChild(ann);            
        }
        
        // Finally, make this thing a root.
        makeRoot(tmNeuronMetadata, newRoot);
    }
    
    public void splitNeurite(TmNeuronMetadata tmNeuronMetadata, TmGeoAnnotation newRoot) throws Exception {        
        if (newRoot == null || tmNeuronMetadata == null) {
            return;
        }

        if (!tmNeuronMetadata.getGeoAnnotationMap().containsKey(newRoot.getId())) {
            throw new Exception(String.format("input neuron %d doesn't contain new root annotation %d",
                    tmNeuronMetadata.getId(), newRoot.getId()));
        }

        // is it already a root?  then you can't split it (should have been 
        //  checked before it gets here)
        if (newRoot.isRoot()) {
            return;
        }

        // Remove this new root as a child of its parent.
        TmGeoAnnotation oldParent = tmNeuronMetadata.getParentOf(newRoot);
        if (oldParent != null) {
            oldParent.getChildIds().remove(newRoot.getId());
        }
        // Ensure neuron knows this root; reset its parent
        //  to the neuron (as one does for a root).
        if (tmNeuronMetadata.getId() == null) {
            log.error("Neuron ID= null.  Setting annotation as root will fail.");
        }
        makeRoot(tmNeuronMetadata, newRoot);
    }    
    
    /**
     * Moves the annotation and its tree from source to destination neuron.
     * Does not refresh from database, or save to database.
     * 
     * @param annotation this will be moved.
     * @param oldTmNeuronMetadata this is the current (source) container of the annotation.
     * @param newTmNeuronMetadata this is the destination container of the annotation.
     * @throws Exception thrown by called methods.
     */
    public void moveNeurite(TmGeoAnnotation annotation, TmNeuronMetadata oldTmNeuronMetadata, TmNeuronMetadata newTmNeuronMetadata) throws Exception {
    	 long newNeuronId = newTmNeuronMetadata.getId();

        // already same neuron?  done!
        if (oldTmNeuronMetadata.getId().equals(newNeuronId)) {
            return;
        }

        // Find the root annotation.  Ultimate parent of the annotation.
        TmGeoAnnotation rootAnnotation = annotation;
        while (!rootAnnotation.isRoot()) {
            rootAnnotation = oldTmNeuronMetadata.getParentOf(rootAnnotation);
        }

        // Move all the geo-annotations from the old to the new neuron.
        Map<Long,TmGeoAnnotation> movedAnnotationIDs = new HashMap<>();
        final Map<Long, TmStructuredTextAnnotation> oldStructuredTextAnnotationMap = oldTmNeuronMetadata.getStructuredTextAnnotationMap();
        final Map<Long, TmStructuredTextAnnotation> newStructuredTextAnnotationMap = newTmNeuronMetadata.getStructuredTextAnnotationMap();
        for (TmGeoAnnotation ann: oldTmNeuronMetadata.getSubTreeList(rootAnnotation)) {
            movedAnnotationIDs.put(ann.getId(), ann);
            ann.setNeuronId(newNeuronId);
            newTmNeuronMetadata.getGeoAnnotationMap().put(ann.getId(), ann);
            // move any TmStructuredTextAnnotations as well:
            if (oldStructuredTextAnnotationMap.containsKey(ann.getId())) {
                TmStructuredTextAnnotation note = oldStructuredTextAnnotationMap.get(ann.getId());
                oldStructuredTextAnnotationMap.remove(ann.getId());
                newStructuredTextAnnotationMap.put(ann.getId(), note);
            }

        }
        
        // loop over anchored paths; if endpoints are in set of moved annotations,
        //  move the path as well
        Map<TmAnchoredPathEndpoints,TmAnchoredPath> oldNeuronAnchoredPathMap = oldTmNeuronMetadata.getAnchoredPathMap();
        Map<TmAnchoredPathEndpoints,TmAnchoredPath> newNeuronAnchoredPathMap = newTmNeuronMetadata.getAnchoredPathMap();

        Iterator<TmAnchoredPathEndpoints> iter = oldNeuronAnchoredPathMap.keySet().iterator();
        while(iter.hasNext()) {
            TmAnchoredPathEndpoints endpoints = iter.next();
            // both endpoints are necessarily in the same neurite, so only need
            //  to test one:
            if (movedAnnotationIDs.containsKey(endpoints.getFirstAnnotationID())) {
                TmAnchoredPath anchoredPath = oldNeuronAnchoredPathMap.get(endpoints);
                iter.remove();
                // TmAnchoredPath anchoredPath = oldNeuronAnchoredPathMap.remove(endpoints);
                newNeuronAnchoredPathMap.put(endpoints, anchoredPath);
            }
        }


        // Need to remove all these annotations from the old map, after
        // iteration through the map above, to avoid concurrent modification.
        for (Long movedAnnotationID: movedAnnotationIDs.keySet()) {
            oldTmNeuronMetadata.getGeoAnnotationMap().remove(movedAnnotationID);
        }
        
        // if it's the root, also change its parent annotation to the new neuron
        rootAnnotation.setParentId(newTmNeuronMetadata.getId());
        oldTmNeuronMetadata.removeRootAnnotation(rootAnnotation);
        newTmNeuronMetadata.addRootAnnotation(rootAnnotation);
    }


    public void saveNeuronMetadata(TmNeuronMetadata neuron) throws Exception {
        dataSource.asyncSaveNeuronMetadata(neuron);
    }
    
    public void saveNeuronData(TmNeuronMetadata neuron) throws Exception {
        dataSource.asyncSaveNeuron(neuron);
    }
    
    private static final String UNREMOVE_NEURON_WARN_FMT = "Attempted to remove neuron %d that was not in workspace %d.";
    public void deleteNeuron(TmWorkspace tmWorkspace, TmNeuronMetadata tmNeuronMetadata) throws Exception {
        TmNeuronMetadata oldValue = removeNeuron(tmNeuronMetadata);
        if (oldValue!=null) {
            // Need to signal to DB that this entitydata must be deleted.
            deleteNeuronData(tmNeuronMetadata);
        }
        else {
            log.warn(String.format(UNREMOVE_NEURON_WARN_FMT, tmNeuronMetadata.getId(), tmWorkspace.getId()));
        }
    }

    private void deleteNeuronData(TmNeuronMetadata neuron) throws Exception {
        dataSource.asyncDeleteNeuron(neuron);
    }

    /** Encapsulating all steps linking an annotation as a root. */
    private void makeRoot(TmNeuronMetadata tmNeuronMetadata, TmGeoAnnotation newRoot) {
        if (tmNeuronMetadata.getId() == null) {
            log.warn("Attempting to set null parent from null neuron-id.");
        }
        newRoot.setParentId(tmNeuronMetadata.getId());
		if (! tmNeuronMetadata.containsRootAnnotation( newRoot )) {
			tmNeuronMetadata.addRootAnnotation( newRoot );
		}        
    }
    /**
     * Given a collection of annotations, under a common neuron, make
     * annotations for each in the database, preserving the linkages implied in
     * the "value" target of the map provided.
     *
     * @param annotations map of node offset id vs "unserialized" annotation.
     * @param nodeParentLinkage map of node offset id vs parent node offset id.
     */
    public void addLinkedGeometricAnnotationsInMemory(
            Map<Integer, Integer> nodeParentLinkage,
            Map<Integer, TmGeoAnnotation> annotations,
            TmNeuronMetadata tmNeuronMetadata,
            Iterator<Long> idSource) {
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
            TmGeoAnnotation linkedAnnotation = createGeometricAnnotationInMemory(tmNeuronMetadata, isRoot, parentAnnotationId, unlinkedAnnotation, idSource);
            TmGeoAnnotation parentAnnotation = tmNeuronMetadata.getParentOf(linkedAnnotation);
            if (parentAnnotation != null) {
                parentAnnotation.addChild(linkedAnnotation);
            }
            nodeIdToAnnotationId.put(nodeId, linkedAnnotation.getId());

            log.trace("Node " + nodeId + " at " + linkedAnnotation.toString() + ", has id " + linkedAnnotation.getId()
                    + ", has parent " + linkedAnnotation.getParentId() + ", under neuron " + linkedAnnotation.getNeuronId());
        }

        if (putativeRootCount > 1) {
            log.warn("Number of nodes with neuron as parent is " + putativeRootCount);
        }
    }

    public void addLinkedGeometricAnnotationsInMemory(
            Map<Integer, Integer> nodeParentLinkage,
            Map<Integer, TmGeoAnnotation> annotations,
            TmNeuronMetadata tmNeuronMetadata) {
        addLinkedGeometricAnnotationsInMemory(nodeParentLinkage, annotations, tmNeuronMetadata, idSource);
    }

    private TmGeoAnnotation createGeometricAnnotationInMemory(TmNeuronMetadata neuron, boolean isRoot, Long parentAnnotationId, TmGeoAnnotation unserializedAnno, Iterator<Long> idSource) {
        return createGeometricAnnotationInMemory(neuron, isRoot, parentAnnotationId, unserializedAnno.getX(), unserializedAnno.getY(), unserializedAnno.getZ(), unserializedAnno.getRadius(), neuron.getId(), idSource);
    }

    private TmGeoAnnotation createGeometricAnnotationInMemory(
            TmNeuronMetadata tmNeuronMetadata, boolean isRoot, Long parentAnnotationId, double x, double y, double z, double radius, Long neuronId, Iterator<Long> idSource) {

        long generatedId = idSource.next();
        Date now = new Date();
        TmGeoAnnotation geoAnnotation = new TmGeoAnnotation(generatedId, parentAnnotationId, tmNeuronMetadata.getId(), x, y, z, radius, now, now);
        tmNeuronMetadata.getGeoAnnotationMap().put(geoAnnotation.getId(), geoAnnotation);
        if (isRoot) {
            tmNeuronMetadata.addRootAnnotation(geoAnnotation);
        }
        else {
            if (parentAnnotationId==null) {
                log.error("Non-root geometric annotation has null parent id for neuron "+generatedId);
            }
        }
        return geoAnnotation;
    }

}
