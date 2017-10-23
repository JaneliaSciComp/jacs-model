package org.janelia.it.jacs.model.domain.tiledMicroscope;

import java.io.Serializable;
import java.util.List;

import io.protostuff.Tag;

/**
 * This class represents a list of points, probably computer generated, that
 * trace a path between two anchors/annotations.  Although we typically reserve
 * anchors to mean the glyphs/objects drawn in the 2D view, and this class is
 * more like an annotation class, the word "anchored" nonetheless well describes
 * the path.
 *
 * User: olbrisd
 * Date: 10/17/13
 * Time: 10:14 AM
 */
public class TmAnchoredPath implements Serializable {
    @Tag(1)
    Long id;

    // two IDs of the annotations between which the path runs
    @Tag(2)
    TmAnchoredPathEndpoints endpoints;

    // VoxelIndex and Vec3 not available to model, so wing it; these
    //  will be 3-vectors (x, y, z):
    @Tag(3)
    List<List<Integer>> pointList;

    // needed by protobuf:
    public TmAnchoredPath() {}

    public TmAnchoredPath(Long id, TmAnchoredPathEndpoints endpoints, List<List<Integer>> pointList) {
        this.id = id;
        this.endpoints = endpoints;
        setPointList(pointList);
    }

    public String toString() {
        if (endpoints != null) {
            return String.format("<path between %d, %d>", endpoints.getFirstAnnotationID(), endpoints.getSecondAnnotationID());
        } else {
            return "<uninitialized path>";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TmAnchoredPathEndpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(TmAnchoredPathEndpoints endpoints) {
        this.endpoints = endpoints;
    }

    public List<List<Integer>> getPointList() {
        return pointList;
    }

    /**
     * points must be ordered list of 3-vectors (x, y, z)
     */
    public final void setPointList(List<List<Integer>> pointList) {
        for (List<Integer> point: pointList) {
            if (point.size() != 3) {
                throw new IllegalArgumentException("found point with dimension not equal to three!");
            }
        }
        this.pointList = pointList;
    }
}
