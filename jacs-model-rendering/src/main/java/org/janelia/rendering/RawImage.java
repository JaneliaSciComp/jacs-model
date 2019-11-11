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
    /**
     * Default raw tile channel suffix pattern
     */
    private String DEFAULT_RAW_CH_SUFFIX_PATTERN = "-ngc.%s.tif";

    private String renderedVolumePath;
    private String acquisitionPath;
    private String relativePath;
    private Integer[] originInNanos; // image origin in microscope coordinates
    private Integer[] dimsInNanos; // image dimensions in microscope coordinates
    private int bytesPerIntensity;
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

    public Integer[] getOriginInNanos() {
        return originInNanos;
    }

    public void setOriginInNanos(Integer[] originInNanos) {
        this.originInNanos = originInNanos;
    }

    public Integer[] getDimsInNanos() {
        return dimsInNanos;
    }

    public void setDimsInNanos(Integer[] dimsInNanos) {
        this.dimsInNanos = dimsInNanos;
    }

    public int getBytesPerIntensity() {
        return bytesPerIntensity;
    }

    public void setBytesPerIntensity(int bytesPerIntensity) {
        this.bytesPerIntensity = bytesPerIntensity;
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
    public Double[] getCenterInNanos() {
        Preconditions.checkArgument(originInNanos != null && dimsInNanos != null && originInNanos.length == dimsInNanos.length,
                "Incompatible originInNanos and dimsInNanos vectors in " + this.toString());
        return Streams.zip(Arrays.stream(originInNanos), Arrays.stream(dimsInNanos), (coord, size) -> (coord + size / 2.)).toArray(Double[]::new);
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
    public String getRawImagePath(int channel) {
        Path imagePath;
        if (StringUtils.isBlank(relativePath)) {
            imagePath = Paths.get(acquisitionPath);
        } else {
            imagePath = Paths.get(acquisitionPath, StringUtils.stripStart(relativePath, "/"));
        }
        String channelSuffix = String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel);
        Path fullImagePath = imagePath.resolve(imagePath.getFileName().toString() + channelSuffix);
        return fullImagePath.toString().replace('\\', '/');
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
