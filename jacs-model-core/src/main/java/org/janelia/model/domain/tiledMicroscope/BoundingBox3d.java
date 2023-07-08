package org.janelia.model.domain.tiledMicroscope;

public class BoundingBox3d
{
    protected double[] min = new double[3];
    protected double[] max = new double[3];
    public enum Unit {
        Micrometer
    }

    public BoundingBox3d() {
    }

    public BoundingBox3d(double[] min, double[] max) {
        this.min = min;
        this.max = max;
    }

    public double[] getCenter() {
        return new double[]{max[0]+min[0]/2.0, max[1]+min[1]/2.0, max[2]+min[2]/2.0};
    }

    public double getDepth() {
        return max[2] - min[2];
    }

    public double getHeight() {
        return max[1] - min[2];
    }

    public double[] getMax() {
        return max;
    }

    public double getMaxX() {return max[0];}
    public double getMaxY() {return max[1];}
    public double getMaxZ() {return max[2];}

    public double[] getMin() {
        return min;
    }

    public double getMinX() {return min[0];}
    public double getMinY() {return min[1];}
    public double getMinZ() {return min[2];}

    public double getWidth() {
        return max[0] - min[0];
    }

    @Override
    public String toString() {
        return "BoundingBox: [" + min.toString() + ", "+ max.toString() + "]";
    }

}

