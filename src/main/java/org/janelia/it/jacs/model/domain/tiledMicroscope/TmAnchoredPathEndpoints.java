package org.janelia.it.jacs.model.domain.tiledMicroscope;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * this class encapsulates a pair of TmGeoAnnotation IDs, intended to be
 * neighboring, between which we will have a TmAnchoredPath; the
 * lesser of the two is stored as the first annotation
 *
 * User: olbrisd
 * Date: 10/23/13
 * Time: 12:52 PM
 */
public class TmAnchoredPathEndpoints implements Serializable {
    @Tag(1)
    private Long annotationID1;
    @Tag(2)
    private Long annotationID2;

    // needed by protobuf
    public TmAnchoredPathEndpoints() {}

    public TmAnchoredPathEndpoints(Long annotationID1, Long annotationID2) {
        setAnnotations(annotationID1, annotationID2);
    }

    public TmAnchoredPathEndpoints(TmGeoAnnotation annotation1, TmGeoAnnotation annotation2) {
        setAnnotations(annotation1.getId(), annotation2.getId());
    }

    /**
     * equality implies the pair of IDs are the same in any order
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TmAnchoredPathEndpoints)) {
            return false;
        }

        return (getFirstAnnotationID().equals(((TmAnchoredPathEndpoints) o).getFirstAnnotationID()) &&
                getSecondAnnotationID().equals(((TmAnchoredPathEndpoints) o).getSecondAnnotationID()));
    }

    /**
     * returns annotation ID with lesser ID number
     */
    public Long getFirstAnnotationID() {
        return Math.min(annotationID1, annotationID2);
    }

    /**
     * return annotation ID with greater ID number
     */
    public Long getSecondAnnotationID() {
        return Math.max(annotationID1, annotationID2);
    }

    public void setAnnotations(Long annotationID1, Long annotationID2) {
        this.annotationID1 = annotationID1;
        this.annotationID2 = annotationID2;
    }

    // protobuf needs getters/setters on all fields
    public Long getAnnotationID1() {
        return annotationID1;
    }

    public Long getAnnotationID2() {
        return annotationID2;
    }

    public void setAnnotationID1(Long annotationID1) {
        this.annotationID1 = annotationID1;
    }

    public void setAnnotationID2(Long annotationID2) {
        this.annotationID2 = annotationID2;
    }

    /**
     * hash is independent of the order of the two IDs
     */
    @Override
    public int hashCode() {
        // taken from Joshua Bloch's Effective Java Ch. 3 item 9
        // (widely quoted on the Internet):
        int result = (int) (getFirstAnnotationID() ^ (getFirstAnnotationID() >>> 32));
        result = 31 * result + (int) (getSecondAnnotationID() ^ (getSecondAnnotationID() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TmAnchoredPathEndpoints[" + annotationID1 + "," + annotationID2 + "]";
    }
    
}
