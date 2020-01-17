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

@MongoMapped(collectionName = "tmNeuron", label = "Tiled Microscope Neuron")
@NotCacheable
public class TmNeuron extends AbstractDomainObject {

    private Reference workspaceRef;

    @SearchAttribute(key = "color_s", label = "Color")
    private String colorHex;

    private Set<String> tags = new HashSet<>();

    // A reference that is used to keep things associated in memory, but persisted separately
    private TmNeuronSkeletons annotations = new TmNeuronSkeletons();


    public TmNeuron() {
    }

    public TmNeuron(TmWorkspace workspace, String name) {
        setName(name);
        this.workspaceRef = Reference.createFor(workspace);
        this.annotations = new TmNeuronSkeletons();
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

    public void merge(TmNeuronMetadata neuron) {
        this.setName(neuron.getName());
        this.setWorkspaceRef(neuron.getWorkspaceRef());
        //this.setVisible(neuron.isVisible());
        this.setColorHex(neuron.getColorHex());
        this.setTags(new HashSet<String>(neuron.getTags()));
        this.setAnnotations(neuron.getNeuronData());
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
        return annotations == null ? null : annotations.getGeoAnnotationMap().size();
    }

    @SearchAttribute(key = "root_count_i", label = "Number of Roots")
    @JsonIgnore
    public Integer getRootCount() {
        return annotations == null ? null : annotations.getRootAnnotationIds().size();
    }

    @SearchAttribute(key = "text_anno_count_i", label = "Number of Notes")
    @JsonIgnore
    public Integer getTextAnnotationCount() {
        return annotations == null ? null : annotations.getStructuredTextAnnotationMap().size();
    }

    public Reference getWorkspaceRef() {
        return workspaceRef;
    }

    public void setWorkspaceRef(Reference workspaceRef) {
        this.workspaceRef = workspaceRef;
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

    public TmNeuronSkeletons getAnnotations() {
        return annotations;
    }

    public void setAnnotations(TmNeuronSkeletons annotations) {
        this.annotations = annotations;
    }

    /**
     * This method can be used to complete construction after deserialization.
     */
    public void initNeuronAnnotations() {
        for (Long geoId : annotations.getGeoAnnotationMap().keySet()) {
            TmNeuronAnnotation geoAnnotation = annotations.getGeoAnnotationMap().get(geoId);
            geoAnnotation.setNeuronId(getId());
        }
    }

    public void addGeometricAnnotation(TmNeuronAnnotation tmNeuronAnnotation) {
        tmNeuronAnnotation.setNeuronId(getId());
        getGeoAnnotationMap().put(tmNeuronAnnotation.getId(), tmNeuronAnnotation);
    }

    /**
     * Pass-through method to TmNeuronSkeletons
     */
    @JsonIgnore
    public Map<Long, TmNeuronAnnotation> getGeoAnnotationMap() {
        return annotations.getGeoAnnotationMap();
    }

    /**
     * Pass-through method to TmNeuronSkeletons
     */
    @JsonIgnore
    public Map<TmAnchoredPathEndpoints, TmAnchoredPath> getAnchoredPathMap() {
        return annotations.getAnchoredPathMap();
    }

    /**
     * Pass-through method to TmNeuronSkeletons
     */
    @JsonIgnore
    public Map<Long, TmStructuredTextAnnotation> getStructuredTextAnnotationMap() {
        return annotations.getStructuredTextAnnotationMap();
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
    public List<TmNeuronAnnotation> getRootAnnotations() {
        List<Long> rootAnnotationIds = annotations.getRootAnnotationIds();
        TmNeuronAnnotation[] tempList = new TmNeuronAnnotation[rootAnnotationIds.size()];
        int i = 0;
        for (Long id : rootAnnotationIds) {
            tempList[i++] = annotations.getGeoAnnotationMap().get(id);
        }
        return Collections.unmodifiableList(Arrays.asList(tempList));
    }

    @JsonIgnore
    public TmNeuronAnnotation getFirstRoot() {
        List<Long> rootAnnotationIds = annotations.getRootAnnotationIds();
        if (rootAnnotationIds.size() > 0) {
            return annotations.getGeoAnnotationMap().get(rootAnnotationIds.get(0));
        } else {
            return null;
        }
    }

    public void removeRootAnnotation(TmNeuronAnnotation root) {
        removeRootAnnotation(root.getId());
    }

    public void removeRootAnnotation(Long rootId) {
        annotations.getRootAnnotationIds().remove(rootId);
    }

    public void addRootAnnotation(TmNeuronAnnotation root) {
        addRootAnnotation(root.getId());
    }

    public void addRootAnnotation(Long rootId) {
        annotations.getRootAnnotationIds().add(rootId);
    }

    public boolean containsRootAnnotation(TmNeuronAnnotation root) {
        return containsRootAnnotation(root.getId());
    }

    public boolean containsRootAnnotation(Long rootId) {
        return annotations.getRootAnnotationIds().contains(rootId);
    }

    @JsonIgnore
    public int getRootAnnotationCount() {
        return annotations.getRootAnnotationIds().size();
    }

    public void clearRootAnnotations() {
        annotations.getRootAnnotationIds().clear();
    }

    @JsonIgnore
    public TmNeuronAnnotation getParentOf(TmNeuronAnnotation annotation) {
        if (annotation == null) {
            return null;
        }
        // arguably this should throw an exception (annotation not in neuron)
        if (!annotations.getGeoAnnotationMap().containsKey(annotation.getId())) {
            return null;
        }
        return annotations.getGeoAnnotationMap().get(annotation.getParentId());
    }

    @JsonIgnore
    public List<TmNeuronAnnotation> getChildrenOf(TmNeuronAnnotation annotation) {
        if (annotation == null) {
            return null;
        }
        // arguably this should throw an exception (annotation not in neuron)
        if (!annotations.getGeoAnnotationMap().containsKey(annotation.getId())) {
            return null;
        }
        ArrayList<TmNeuronAnnotation> children = new ArrayList<>(annotation.getChildIds().size());
        for (Long childID : annotation.getChildIds()) {
            children.add(annotations.getGeoAnnotationMap().get(childID));
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
    public List<TmNeuronAnnotation> getChildrenOfOrdered(TmNeuronAnnotation annotation) {
        List<TmNeuronAnnotation> unorderedList = getChildrenOf(annotation);
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
    public List<TmNeuronAnnotation> getSubTreeList(TmNeuronAnnotation annotation) {
        // this method needs to be nonrecursive, because input can get
        //  large enough to cause a stack overflow
        ArrayList<TmNeuronAnnotation> subtreeList = new ArrayList<>();
        ArrayDeque<TmNeuronAnnotation> stack = new ArrayDeque<>();
        stack.addFirst(annotation);
        while (stack.size() > 0) {
            TmNeuronAnnotation ann = stack.removeFirst();
            subtreeList.add(ann);
            List<TmNeuronAnnotation> childList = getChildrenOf(ann);
            for (TmNeuronAnnotation child : childList) {
                stack.addFirst(child);
            }
        }
        return subtreeList;
    }

    public List<String> checkRepairNeuron() {
        return annotations.checkRepairNeuron(getId(), true);
    }

    public List<String> checkNeuron() {
        return annotations.checkRepairNeuron(getId(), false);
    }

    @JsonIgnore
    String getDebugString() {
        return annotations == null ? "No neuron data" : annotations.getDebugString();
    }

}