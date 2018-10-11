package org.janelia.model.rendering;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class RawCoord {
    private int[] originInNanos = new int[3];
    private double[] lowestResNanosPerVoxel = new double[3]; // nanos per voxel at lowest resolution (max zoon level)
    private int numZoomLevels = -1;

    void setOriginInNanos(CoordinateAxis axis, Integer value) {
        originInNanos[axis.index()] = value;
    }

    void setLowestResNanosPerVoxel(CoordinateAxis axis, Double value) {
        lowestResNanosPerVoxel[axis.index()] = value;
    }

    int getNumZoomLevels() {
        return numZoomLevels;
    }

    void setNumZoomLevels(int numZoolLevels) {
        this.numZoomLevels = numZoolLevels;
    }

    double[] getHighestResMicromsPerVoxel() {
        double scaleFactor = getScaleFactor();
        return Arrays.stream(lowestResNanosPerVoxel)
                .map(lowResVal -> lowResVal / scaleFactor / 1000)
                .toArray();
    }

    double getScaleFactor() {
        Preconditions.checkArgument(numZoomLevels >= 0, "Number of zoom levels is not set");
        return Math.pow(2, numZoomLevels - 1);
    }

    int[] getOriginVoxel() {
        double scaleFactor = getScaleFactor();
        return Arrays.stream(originInNanos)
                .map(originCoord -> (int) (originCoord / scaleFactor))
                .toArray();
    }
}
