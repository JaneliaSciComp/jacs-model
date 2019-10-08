package org.janelia.rendering.ymlrepr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawTile {
    private String path;
    private Aabb aabb;
    private Shape shape;
    private Double[] transform;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Aabb getAabb() {
        return aabb;
    }

    public void setAabb(Aabb aabb) {
        this.aabb = aabb;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Double[] getTransform() {
        return transform;
    }

    public void setTransform(Double[] transform) {
        this.transform = transform;
    }

    /**
     *  Homography and Transform are identical and either may be provided.
     *
     * @return transform matrix
     */
    public Double[] getHomography() {
        return transform;
    }

    public void setHomography(Double[] homography) {
        this.transform = homography;
    }
}
