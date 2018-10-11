package org.janelia.model.rendering;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileInfo {

    private final CoordinateAxis sliceAxis;
    private final int channelCount;
    private final int[] tileSize; // tileSize in pixels
    private final int bitDepth;
    private final boolean srgb;

    TileInfo(CoordinateAxis sliceAxis,
             int channelCount,
             int[] tileSize,
             int bitDepth,
             boolean srgb) {
        this.sliceAxis = sliceAxis;
        this.channelCount = channelCount;
        this.tileSize = tileSize;
        this.bitDepth = bitDepth;
        this.srgb = srgb;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getTileWidth() {
        return tileSize[0];
    }

    public int getTileHeight() {
        return tileSize[1];
    }

    public int getNumPages() {
        return tileSize[2];
    }

    /**
     * @return an array of int representing the X, Y, Z dimensions (in this order) keeping in mind that the tile dimension
     * are mapped to the cartesian coordinates based on the slice axis.
     */
    public int[] getVolumeSize() {
        switch (sliceAxis) {
            case X:
                return new int[] {getNumPages(), getTileWidth(), getTileHeight()};
            case Y:
                return new int[] {getTileHeight(), getNumPages(), getTileWidth()};
            case Z:
                return new int[] {getTileWidth(), getTileHeight(), getNumPages()};
            default:
                throw new IllegalArgumentException("Slice axis has not been set");
        }
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public boolean isSrgb() {
        return srgb;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("channelCount", channelCount)
                .append("tileSize", tileSize)
                .append("bitDepth", bitDepth)
                .append("srgb", srgb)
                .toString();
    }
}
