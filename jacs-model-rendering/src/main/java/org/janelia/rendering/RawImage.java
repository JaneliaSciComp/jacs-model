package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * RawImage contains octant data extracted from the YML file.
 */
public class RawImage {

    private String renderedVolumePath;
    private String acquisitionPath;
    private String relativePath;
    private Integer[] originInMicros; // image origin in microscope coordinates
    private Integer[] dimsInMicros; // image dimensions in microscope coordinates
    private Double[] transform;
    private Integer[] tileDims; // image tile dimensions in form of an array [deltax, deltay, deltaz, nchannels]

    public String getRenderedVolumePath() {
        return renderedVolumePath;
    }

    public void setRenderedVolumePath(String renderedVolumePath) {
        this.renderedVolumePath = renderedVolumePath;
    }

    public String getAcquisitionPath() {
        return acquisitionPath;
    }

    public void setAcquisitionPath(String acquisitionPath) {
        this.acquisitionPath = acquisitionPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public Integer[] getOriginInMicros() {
        return originInMicros;
    }

    public void setOriginInMicros(Integer[] originInMicros) {
        this.originInMicros = originInMicros;
    }

    public Integer[] getDimsInMicros() {
        return dimsInMicros;
    }

    public void setDimsInMicros(Integer[] dimsInMicros) {
        this.dimsInMicros = dimsInMicros;
    }

    public Double[] getTransform() {
        return transform;
    }

    public void setTransform(Double[] transform) {
        this.transform = transform;
    }

    @JsonIgnore
    double[][] getTransformMatrix() {
        int matrixDim = 4;
        double[][] transformMatrix = new double[matrixDim][matrixDim];
        int origColCount = 5;
        // Weird matrix on input: translation column is last column, rather than one after x,y,z.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                transformMatrix[row][col] =
                        transform[row * origColCount + col];
            }
        }
        for (int row = 0; row < 4; row++) {
            transformMatrix[row][3] =
                    transform[row * origColCount + (origColCount - 1)];
        }
        transformMatrix[matrixDim - 1][matrixDim - 1] = 1.0; // To satisfy invertible requirement.
        return transformMatrix;
    }

    public Integer[] getTileDims() {
        return tileDims;
    }

    public void setTileDims(Integer[] tileDims) {
        this.tileDims = tileDims;
    }

    @JsonIgnore
    public Integer[] getCenter() {
        Preconditions.checkArgument(originInMicros != null && dimsInMicros != null && originInMicros.length == dimsInMicros.length,
                "Incompatible originInMicros and dimsInMicros vectors in " + this.toString());
        return Streams.zip(Arrays.stream(originInMicros), Arrays.stream(dimsInMicros), (coord, size) -> coord + size / 2).toArray(Integer[]::new);
    }

    @JsonIgnore
    Optional<Integer> getNumberOfChannels() {
        if (tileDims == null || tileDims.length < 4) {
            return Optional.empty();
        } else {
            return Optional.of(tileDims[3]);
        }
    }

    @JsonIgnore
    Path getRawImagePath(String suffix) {
        Path imagePath;
        if (StringUtils.isBlank(relativePath)) {
            imagePath = Paths.get(acquisitionPath);
        } else {
            imagePath = Paths.get(acquisitionPath).resolve(StringUtils.stripStart(relativePath, "/"));
        }
        return imagePath.resolve(imagePath.getFileName().toString() + suffix);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}