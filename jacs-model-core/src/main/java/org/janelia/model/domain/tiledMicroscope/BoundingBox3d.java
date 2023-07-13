package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BoundingBox3d
{
    protected double[] min = new double[3];
    protected double[] max = new double[3];
    private Long domainId;

    public enum Unit {
        Micrometer
    }

    public BoundingBox3d() {
    }

    public BoundingBox3d(double[] min, double[] max) {
        this.min = min;
        this.max = max;
    }

    @JsonIgnore
    public double[] getCenter() {
        return new double[]{max[0]+min[0]/2.0, max[1]+min[1]/2.0, max[2]+min[2]/2.0};
    }

    @JsonIgnore
    public double getDepth() {
        return max[2] - min[2];
    }

    @JsonIgnore
    public double getHeight() {
        return max[1] - min[2];
    }

    public double[] getMax() {
        return max;
    }

    @JsonIgnore
    public double getMaxX() {return max[0];}
    @JsonIgnore
    public double getMaxY() {return max[1];}
    @JsonIgnore
    public double getMaxZ() {return max[2];}

    public double[] getMin() {
        return min;
    }

    @JsonIgnore
    public double getMinX() {return min[0];}
    @JsonIgnore
    public double getMinY() {return min[1];}
    @JsonIgnore
    public double getMinZ() {return min[2];}

    @JsonIgnore
    public double getWidth() {
        return max[0] - min[0];
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String toString() {
        return "BoundingBox: [" + min.toString() + ", "+ max.toString() + ", domainID:" + domainId + "]";
    }

}

