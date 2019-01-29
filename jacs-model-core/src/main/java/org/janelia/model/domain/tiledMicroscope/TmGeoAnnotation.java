package org.janelia.model.domain.tiledMicroscope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.protostuff.Tag;

/**
 * Created with IntelliJ IDEA.
 * User: murphys
 * Date: 5/1/13
 * Time: 1:23 PM
 */
public class TmGeoAnnotation implements Serializable {
    @Tag(1)
    private Long id;
    // parentId is the neuron (if root annotation) or another TmGeoAnn
    @Tag(2)
    private Long parentId;
    @Tag(3)
    private Double x;
    @Tag(4)
    private Double y;
    @Tag(5)
    private Double z;
    @Tag(6)
    private Double radius;
    @Tag(7)
    private Date creationDate;
    // child and neuron ID fields only filled in when the annotation is in a neuron!
    //  they are null otherwise
    // I'd like to have a flag that is set when these are correct, but there's no
    //  way for the GeoAnn to keep it up-to-date, as it's not involved when operations
    //  are performed on other GeoAnns (creation, deletion, update), so the info
    //  would get stale fast
    // what updates the modification date?  changes to x, y, z, and radius;
    //  also comment and index, which are unused; also, outside routines may
    //  update it (eg, change to attached note); connectivity changes do
    //  not trigger (change in parent or children)
    @Tag(8)
    private Date modificationDate;
    @Tag(9)
    private List<Long> childIds = new ArrayList<>();

    // Populated after deserialization
    transient private Long neuronId = null;

    // implementation note: at one point we stored the parent and child objects,
    //  but serializing them for calling remote server routines caused the
    //  whole tree to get walked recursively, overflowing the stack; so
    //  now we just use the IDs (FW-2728)

    public TmGeoAnnotation() {
    }
    
    public TmGeoAnnotation(Long id, Long parentId, Long neuronId, Double x, Double y, Double z, Double radius, Date creationDate, Date modificationDate) {
        this.id = id;
        this.parentId = parentId;
        this.neuronId = neuronId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
    }

    @Override
    public String toString() {
        //return String.format("ann id %d", id);
        // return String.format("(%.1f, %.1f, %.1f)", x, y, z);
        return String.format("%d, %d, %d", x.intValue(), y.intValue(), z.intValue());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
        updateModificationDate();
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
        updateModificationDate();
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
        updateModificationDate();
    }

    public void addChild(TmGeoAnnotation child) {
        childIds.add(child.getId());
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    public List<Long> getChildIds() {
        return childIds;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        if (modificationDate != null) {
            return modificationDate;
        }
        else {
            return creationDate;
        }
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void updateModificationDate() {
        this.modificationDate = new Date();
    }

    public boolean isRoot() {
        return neuronId != null && parentId.equals(neuronId);
    }

    public boolean isBranch() {
        return getChildIds().size() > 1;
    }

    public boolean isEnd() {
        return getChildIds().size() == 0;
    }

    public boolean isLink() {
        return !isRoot() && getChildIds().size() == 1;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
        updateModificationDate();
    }

    public Long getNeuronId() {
        return neuronId;
    }

    public void setNeuronId(Long neuronId) {
        this.neuronId = neuronId;
    }
}