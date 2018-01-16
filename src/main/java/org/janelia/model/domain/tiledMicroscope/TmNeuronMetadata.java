package org.janelia.model.domain.tiledMicroscope;

import java.awt.Color;
import java.util.*;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.NotCacheable;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.util.ColorUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;

/**
 * Metadata for a tiled microscope neuron in a TmWorkspace. The actual neuron point data
 * is stored in a companion table in MySQL, access via the TiledMicroscopeDAO.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="tmNeuron",label="Tiled Microscope Neuron")
@NotCacheable
public class TmNeuronMetadata extends AbstractDomainObject {

    private Reference workspaceRef;

    private Boolean visible;

    @SearchAttribute(key="color_s",label="Color")
    private String colorHex;

    private Set<String> tags = new HashSet<>();

    // A reference that is used to keep things associated in memory, but persisted separately
    @JsonIgnore
    transient private TmNeuronData neuronData;

    public TmNeuronMetadata() {
    }

    public TmNeuronMetadata(TmWorkspace workspace, String name) {
        setName(name);
        this.workspaceRef = Reference.createFor(workspace);
        this.neuronData = new TmNeuronData();
    }
    
    public static TmNeuronMetadata copy(TmNeuronMetadata neuron) {
        TmNeuronMetadata copy = new TmNeuronMetadata();
        copy.setName(neuron.getName());
        copy.setWorkspaceRef(neuron.getWorkspaceRef());
        copy.setVisible(neuron.isVisible());
        copy.setColorHex(neuron.getColorHex());
        copy.setTags(new HashSet<String>(neuron.getTags()));
        return copy;
    }

    public void merge(TmNeuronMetadata neuron) {
        this.setName(neuron.getName());
        this.setWorkspaceRef(neuron.getWorkspaceRef());
        this.setVisible(neuron.isVisible());
        this.setColorHex(neuron.getColorHex());
        this.setTags(new HashSet<String>(neuron.getTags()));
        this.setNeuronData(neuron.getNeuronData());
    }

    @SearchAttribute(key="workspace_id_l",label="Workspace GUID")
    @JsonIgnore
    public Long getWorkspaceId() {
        return workspaceRef==null?null:workspaceRef.getTargetId();
    }

    @SearchAttribute(key="tags_s",label="Tags")
    @JsonIgnore
    public String getTagDelimitedList() {
        return tags==null?null:Joiner.on(",").join(tags);
    }

    @SearchAttribute(key="anno_count_i",label="Number of Anchors")
    @JsonIgnore
    public Integer getAnnotationCount() {
        return neuronData==null?null:neuronData.getGeoAnnotationMap().size();
    }
    
    @SearchAttribute(key="root_count_i",label="Number of Roots")
    @JsonIgnore
    public Integer getRootCount() {
        return neuronData==null?null:neuronData.getRootAnnotationIds().size();
    }

    @SearchAttribute(key="text_anno_count_i",label="Number of Notes")
    @JsonIgnore
    public Integer getTextAnnotationCount() {
        return neuronData==null?null:neuronData.getStructuredTextAnnotationMap().size();
    }

    @SearchAttribute(key="visible_b",label="Visibility")
    @JsonIgnore
    public Boolean getVisibility() {
        return visible;
    }
    
    public Reference getWorkspaceRef() {
        return workspaceRef;
    }

    public void setWorkspaceRef(Reference workspaceRef) {
        this.workspaceRef = workspaceRef;
    }

    public boolean isVisible() {
        return visible==null || visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    @JsonIgnore
    public Color getColor() {
        if (colorHex==null) return null;
        return ColorUtils.fromHex(colorHex);
    }

    @JsonIgnore
    public void setColor(Color color) {
        this.colorHex = color==null?null:ColorUtils.toHex(color);
    }
    
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * This method offers access to the underlying neuron data. It is intentionally package protected
     * so that it can only be accessed by the serializer.
     */
    @JsonIgnore
    TmNeuronData getNeuronData() {
        return neuronData;
    }

    /**
     * This method offers access to the underlying neuron data. It is intentionally package protected
     * so that it can only be accessed by the deserializer.
     */
    @JsonIgnore
    void setNeuronData(TmNeuronData neuronData) {
        this.neuronData = neuronData;
    }

    /**
     * This method can be used to complete NeuronData construction after deserialization. 
     */
    void initNeuronData() {
        for(Long geoId : neuronData.getGeoAnnotationMap().keySet()) {
            TmGeoAnnotation geoAnnotation = neuronData.getGeoAnnotationMap().get(geoId);
            geoAnnotation.setNeuronId(getId());
        }
    }
    
    public void addGeometricAnnotation(TmGeoAnnotation tmGeoAnnotation) {
    	tmGeoAnnotation.setNeuronId(getId());
        getGeoAnnotationMap().put(tmGeoAnnotation.getId(), tmGeoAnnotation);
    }
    
    @Override
    public String toString() {
        String data = neuronData==null?"null":neuronData.getRootAnnotationIds().size()+" roots";
        return "TmNeuronMetadata[id=" + getId() + ", name=" + getName() + ", neuronData=" + data + "]";
    }

    // ****************************************************************************************************************
    // Business logic that deals with the neuron data
    // ****************************************************************************************************************

    @JsonIgnore
    private void checkNeuronDataLoaded() {
        if (neuronData==null) {
            throw new IllegalStateException("Neuron data has not been loaded");
        }
    }

    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Map<Long, TmGeoAnnotation> getGeoAnnotationMap() {
        checkNeuronDataLoaded();
        return neuronData.getGeoAnnotationMap();
    }

    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Map<TmAnchoredPathEndpoints, TmAnchoredPath> getAnchoredPathMap() {
        checkNeuronDataLoaded();
        return neuronData.getAnchoredPathMap();
    }

    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Map<Long, TmStructuredTextAnnotation> getStructuredTextAnnotationMap() {
        checkNeuronDataLoaded();
        return neuronData.getStructuredTextAnnotationMap();
    }

    /**
     * Returns all the root annotations, such that:
     * + the contents of the returned collection may not be changed;
     * + the annotations within the collection _could_ be changed.
     *
     * Note that this contract guarantees that no new root annotations may
     * be placed into the returned value, nor any added.  This collection is
     * meant for read-only purposes.  Any removal of root annotations, or
     * addition of root annotations, must be done via the id collection.
     *
     * However, nothing may prevent a caller
     * from modifying state of any geo-annotation found in this collection.
     *
     * This collection is immutable to avoid deceiving callers about the
     * effects of modifying the return value.
     */
    @JsonIgnore
    public List<TmGeoAnnotation> getRootAnnotations() {
        checkNeuronDataLoaded();
        List<Long> rootAnnotationIds = neuronData.getRootAnnotationIds();
        TmGeoAnnotation[] tempList = new TmGeoAnnotation[rootAnnotationIds.size()];
        int i = 0;
        for (Long id: rootAnnotationIds) {
            tempList[ i++ ] = neuronData.getGeoAnnotationMap().get(id);
        }
        return Collections.unmodifiableList(Arrays.asList(tempList));
    }

    @JsonIgnore
    public TmGeoAnnotation getFirstRoot() {
        checkNeuronDataLoaded();
        List<Long> rootAnnotationIds = neuronData.getRootAnnotationIds();
        if (rootAnnotationIds.size() > 0) {
            return neuronData.getGeoAnnotationMap().get(rootAnnotationIds.get(0));
        }
        else {
            return null;
        }
    }

    public void removeRootAnnotation(TmGeoAnnotation root) {
        removeRootAnnotation(root.getId());
    }

    public void removeRootAnnotation(Long rootId) {
        checkNeuronDataLoaded();
        neuronData.getRootAnnotationIds().remove(rootId);
    }

    public void addRootAnnotation(TmGeoAnnotation root) {
        addRootAnnotation(root.getId());
    }

    public void addRootAnnotation(Long rootId) {
        checkNeuronDataLoaded();
        neuronData.getRootAnnotationIds().add(rootId);
    }

    public boolean containsRootAnnotation(TmGeoAnnotation root) {
        return containsRootAnnotation(root.getId());
    }

    public boolean containsRootAnnotation(Long rootId) {
        checkNeuronDataLoaded();
        return neuronData.getRootAnnotationIds().contains(rootId);
    }

    @JsonIgnore
    public int getRootAnnotationCount() {
        checkNeuronDataLoaded();
        return neuronData.getRootAnnotationIds().size();
    }

    public void clearRootAnnotations() {
        checkNeuronDataLoaded();
        neuronData.getRootAnnotationIds().clear();
    }

    @JsonIgnore
    public TmGeoAnnotation getParentOf(TmGeoAnnotation annotation) {
        checkNeuronDataLoaded();
        if (annotation == null) {
            return null;
        }
        // arguably this should throw an exception (annotation not in neuron)
        if (!neuronData.getGeoAnnotationMap().containsKey(annotation.getId())) {
            return null;
        }
        return neuronData.getGeoAnnotationMap().get(annotation.getParentId());
    }

    @JsonIgnore
    public List<TmGeoAnnotation> getChildrenOf(TmGeoAnnotation annotation) {
        checkNeuronDataLoaded();
        if (annotation == null) {
            return null;
        }
        // arguably this should throw an exception (annotation not in neuron)
        if (!neuronData.getGeoAnnotationMap().containsKey(annotation.getId())) {
            return null;
        }
        ArrayList<TmGeoAnnotation> children = new ArrayList<>(annotation.getChildIds().size());
        for (Long childID : annotation.getChildIds()) {
            children.add(neuronData.getGeoAnnotationMap().get(childID));
        }
        return children;
    }

    /**
     * returns a list of the child annotations of the input annotation in
     * a predictable, stable order
     *
     * current implementation is based on the angle from the x-axis
     * of the x-y projection of the line connecting the branch to its child
     */
    @JsonIgnore
    public List<TmGeoAnnotation> getChildrenOfOrdered(TmGeoAnnotation annotation) {
        checkNeuronDataLoaded();
        List<TmGeoAnnotation> unorderedList = getChildrenOf(annotation);
        if (unorderedList.size() < 2) {
            return unorderedList;
        }

        // we're going to use the angle in the xy plane of the lines between
        //  the parent and children to sort; for convenience, I'll use
        //  the value returned by math.atan2
        Collections.sort(unorderedList, new Comparator<TmGeoAnnotation>() {
            @Override
            public int compare(TmGeoAnnotation ann1, TmGeoAnnotation ann2) {
                double tan1 = Math.atan2(ann1.getY(), ann1.getX());
                double tan2 = Math.atan2(ann2.getY(), ann2.getX());
                if (tan1 > tan2) {
                    return 1;
                } else if (tan1 < tan2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return unorderedList;
    }

    /**
     * this method returns a list of all children in the subtree of the input
     * annotation, plus the annotation itself; the order is such that the
     * annotation itself is first, and each child is guaranteed to appear
     * after its parent
     * @return list of annotations in subtree rooted at given annotation
     */
    @JsonIgnore
    public List<TmGeoAnnotation> getSubTreeList(TmGeoAnnotation annotation) {
        checkNeuronDataLoaded();
        // this method needs to be nonrecursive, because input can get
        //  large enough to cause a stack overflow
        ArrayList<TmGeoAnnotation> subtreeList = new ArrayList<>();
        ArrayDeque<TmGeoAnnotation> stack = new ArrayDeque<>();
        stack.addFirst(annotation);
        while (stack.size() > 0) {
            TmGeoAnnotation ann = stack.removeFirst();
            subtreeList.add(ann);
            for (TmGeoAnnotation child: getChildrenOf(ann)) {
                stack.addFirst(child);
            }
        }
        return subtreeList;
    }

    public List<String> checkRepairNeuron() {
        checkNeuronDataLoaded();
        return neuronData.checkRepairNeuron(getId(), true);
    }

    public List<String> checkNeuron() {
        checkNeuronDataLoaded();
        return neuronData.checkRepairNeuron(getId(), 	false);
    }

    @JsonIgnore
    public String getDebugString() {
        return neuronData==null?"No neuron data":neuronData.getDebugString();
    }
}