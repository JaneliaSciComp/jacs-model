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
@MongoMapped(collectionName = "tmNeuron", label = "Tiled Microscope Neuron")
@NotCacheable
public class TmNeuronMetadata extends AbstractDomainObject {

    private Reference workspaceRef;
    private Boolean largeNeuron;
    private Boolean visible;

    @SearchAttribute(key = "color_s", label = "Color")
    private String colorHex;

    private Set<String> tags = new HashSet<>();

    // A reference that is used to keep things associated in memory, but persisted separately
    private TmNeuronData neuronData = new TmNeuronData();

    @JsonIgnore
    transient private boolean synced;
    @JsonIgnore
    transient private int syncLevel = 0;

    public TmNeuronMetadata() {
        setLargeNeuron(false);
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
        copy.setOwnerKey(neuron.getOwnerKey());
        copy.setTags(new HashSet<String>(neuron.getTags()));
        copy.setNeuronData(neuron.getNeuronData());
        return copy;
    }

    public void updateEdges() {

    }

    public void merge(TmNeuronMetadata neuron) {
        this.setName(neuron.getName());
        this.setWorkspaceRef(neuron.getWorkspaceRef());
        this.setVisible(neuron.isVisible());
        this.setColorHex(neuron.getColorHex());
        this.setTags(new HashSet<String>(neuron.getTags()));
        this.setNeuronData(neuron.getNeuronData());
    }

    @JsonIgnore
    public int getSyncLevel() {
        return syncLevel;
    }

    public synchronized void decrementSyncLevel() {
        this.syncLevel--;
    }

    public synchronized void incrementSyncLevel() {
        this.syncLevel++;
    }

    @JsonIgnore
    public boolean isSynced() {
        return synced;
    }

    @JsonIgnore
    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    @SearchAttribute(key = "workspace_id_l", label = "Workspace GUID")
    @JsonIgnore
    public Long getWorkspaceId() {
        return workspaceRef == null ? null : workspaceRef.getTargetId();
    }

    @SearchAttribute(key = "tags_s", label = "Tags")
    @JsonIgnore
    public String getTagDelimitedList() {
        return tags == null ? null : Joiner.on(",").join(tags);
    }

    @SearchAttribute(key = "anno_count_i", label = "Number of Anchors")
    @JsonIgnore
    public Integer getAnnotationCount() {
        return neuronData == null ? null : neuronData.getGeoAnnotationMap().size();
    }

    @SearchAttribute(key = "root_count_i", label = "Number of Roots")
    @JsonIgnore
    public Integer getRootCount() {
        return neuronData == null ? null : neuronData.getRootAnnotationIds().size();
    }

    @SearchAttribute(key = "text_anno_count_i", label = "Number of Notes")
    @JsonIgnore
    public Integer getTextAnnotationCount() {
        return neuronData == null ? null : neuronData.getStructuredTextAnnotationMap().size();
    }

    @SearchAttribute(key = "visible_b", label = "Visibility")
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
        return visible == null || visible;
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
        if (colorHex == null) return null;
        return ColorUtils.fromHex(colorHex);
    }

    @JsonIgnore
    public void setColor(Color color) {
        this.colorHex = color == null ? null : ColorUtils.toHex(color);
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public TmNeuronData getNeuronData() {
        return neuronData;
    }

    public void setNeuronData(TmNeuronData neuronData) {
        this.neuronData = neuronData;
    }

    /**
     * This method can be used to complete NeuronData construction after deserialization.
     */
    public void initNeuronData() {
        for (Long geoId : neuronData.getGeoAnnotationMap().keySet()) {
            TmGeoAnnotation geoAnnotation = neuronData.getGeoAnnotationMap().get(geoId);
            geoAnnotation.setNeuronId(getId());
        }
    }

    public void addGeometricAnnotation(TmGeoAnnotation tmGeoAnnotation) {
        tmGeoAnnotation.setNeuronId(getId());
        getGeoAnnotationMap().put(tmGeoAnnotation.getId(), tmGeoAnnotation);
    }


    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Collection<TmNeuronEdge> getEdges() {
        return neuronData.getEdges();
    }

    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Map<Long, TmGeoAnnotation> getGeoAnnotationMap() {
        return neuronData.getGeoAnnotationMap();
    }

    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Map<TmAnchoredPathEndpoints, TmAnchoredPath> getAnchoredPathMap() {
        return neuronData.getAnchoredPathMap();
    }

    /**
     * Pass-through method to TmNeuronData
     */
    @JsonIgnore
    public Map<Long, TmStructuredTextAnnotation> getStructuredTextAnnotationMap() {
        return neuronData.getStructuredTextAnnotationMap();
    }

    /**
     * Returns all the root annotations, such that:
     * + the contents of the returned collection may not be changed;
     * + the annotations within the collection _could_ be changed.
     * <p>
     * Note that this contract guarantees that no new root annotations may
     * be placed into the returned value, nor any added.  This collection is
     * meant for read-only purposes.  Any removal of root annotations, or
     * addition of root annotations, must be done via the id collection.
     * <p>
     * However, nothing may prevent a caller
     * from modifying state of any geo-annotation found in this collection.
     * <p>
     * This collection is immutable to avoid deceiving callers about the
     * effects of modifying the return value.
     */
    @JsonIgnore
    public List<TmGeoAnnotation> getRootAnnotations() {
        List<Long> rootAnnotationIds = neuronData.getRootAnnotationIds();
        TmGeoAnnotation[] tempList = new TmGeoAnnotation[rootAnnotationIds.size()];
        int i = 0;
        for (Long id : rootAnnotationIds) {
            tempList[i++] = neuronData.getGeoAnnotationMap().get(id);
        }
        return Collections.unmodifiableList(Arrays.asList(tempList));
    }

    @JsonIgnore
    public TmGeoAnnotation getFirstRoot() {
        List<Long> rootAnnotationIds = neuronData.getRootAnnotationIds();
        if (rootAnnotationIds.size() > 0) {
            return neuronData.getGeoAnnotationMap().get(rootAnnotationIds.get(0));
        } else {
            return null;
        }
    }

    public void removeRootAnnotation(TmGeoAnnotation root) {
        removeRootAnnotation(root.getId());
    }

    public void removeRootAnnotation(Long rootId) {
        neuronData.getRootAnnotationIds().remove(rootId);
    }

    public void addRootAnnotation(TmGeoAnnotation root) {
        addRootAnnotation(root.getId());
    }

    public void addRootAnnotation(Long rootId) {
        neuronData.getRootAnnotationIds().add(rootId);
    }

    public boolean containsRootAnnotation(TmGeoAnnotation root) {
        return containsRootAnnotation(root.getId());
    }

    public boolean containsRootAnnotation(Long rootId) {
        return neuronData.getRootAnnotationIds().contains(rootId);
    }

    @JsonIgnore
    public int getRootAnnotationCount() {
        return neuronData.getRootAnnotationIds().size();
    }

    public void clearRootAnnotations() {
        neuronData.getRootAnnotationIds().clear();
    }

    @JsonIgnore
    public TmGeoAnnotation getParentOf(TmGeoAnnotation annotation) {
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
     * <p>
     * current implementation is based on the angle from the x-axis
     * of the x-y projection of the line connecting the branch to its child
     */
    @JsonIgnore
    public List<TmGeoAnnotation> getChildrenOfOrdered(TmGeoAnnotation annotation) {
        List<TmGeoAnnotation> unorderedList = getChildrenOf(annotation);
        if (unorderedList.size() < 2) {
            return unorderedList;
        }

        // we're going to use the angle in the xy plane of the lines between
        //  the parent and children to sort; for convenience, I'll use
        //  the value returned by math.atan2
        Collections.sort(unorderedList, (ann1, ann2) -> {
            double tan1 = Math.atan2(ann1.getY(), ann1.getX());
            double tan2 = Math.atan2(ann2.getY(), ann2.getX());
            return Double.compare(tan1, tan2);
        });
        return unorderedList;
    }

    /**
     * this method returns a list of all children in the subtree of the input
     * annotation, plus the annotation itself; the order is such that the
     * annotation itself is first, and each child is guaranteed to appear
     * after its parent
     *
     * @return list of annotations in subtree rooted at given annotation
     */
    @JsonIgnore
    public List<TmGeoAnnotation> getSubTreeList(TmGeoAnnotation annotation) {
        // this method needs to be nonrecursive, because input can get
        //  large enough to cause a stack overflow
        ArrayList<TmGeoAnnotation> subtreeList = new ArrayList<>();
        ArrayDeque<TmGeoAnnotation> stack = new ArrayDeque<>();
        stack.addFirst(annotation);
        while (stack.size() > 0) {
            TmGeoAnnotation ann = stack.removeFirst();
            subtreeList.add(ann);
            List<TmGeoAnnotation> childList = getChildrenOf(ann);
            for (TmGeoAnnotation child : childList) {
                stack.addFirst(child);
            }
        }
        return subtreeList;
    }

    public List<String> checkRepairNeuron() {
        return neuronData.checkRepairNeuron(getId(), true);
    }

    public List<String> checkNeuron() {
        return neuronData.checkRepairNeuron(getId(), false);
    }

    @JsonIgnore
    String getDebugString() {
        return neuronData == null ? "No neuron data" : neuronData.getDebugString();
    }

    public Boolean isLargeNeuron() {
        return largeNeuron;
    }

    public void setLargeNeuron(Boolean largeNeuron) {
        this.largeNeuron = largeNeuron;
    }
}