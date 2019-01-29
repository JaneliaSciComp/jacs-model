package org.janelia.rendering.ymlrepr;

public class Shape {
    private String type;
    private Integer[] dims;
    private Integer[] crop;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer[] getDims() {
        return dims;
    }

    public void setDims(Integer[] dims) {
        this.dims = dims;
    }

    public Integer[] getCrop() {
        return crop;
    }

    public void setCrop(Integer[] crop) {
        this.crop = crop;
    }
}
