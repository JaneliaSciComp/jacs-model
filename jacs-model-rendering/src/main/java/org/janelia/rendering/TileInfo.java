package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.regex.Pattern;

public class TileInfo {

    private static final String XY_CH_TIFF_PATTERN = "default.%s.tif";
    private static final String YZ_CH_TIFF_PATTERN = "YZ.%s.tif";
    private static final String ZX_CH_TIFF_PATTERN = "ZX.%s.tif";

    static Coordinate getSliceAxisFromImageNameForChannel(String fn) {
        Pattern xyTilePattern = Pattern.compile("^" + String.format(XY_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
        Pattern yzTilePattern = Pattern.compile("^" + String.format(YZ_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
        Pattern zxTilePattern = Pattern.compile("^" + String.format(ZX_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
        if (xyTilePattern.asPredicate().test(fn)) {
            return Coordinate.Z;
        } else if (yzTilePattern.asPredicate().test(fn)) {
            return Coordinate.X;
        } else if (zxTilePattern.asPredicate().test(fn)) {
            return Coordinate.Y;
        } else {
            return null;
        }
    }

    static String getImageNameForChannel(Coordinate sliceAxis, int channelNumber) {
        switch (sliceAxis) {
            case X:
                return String.format(YZ_CH_TIFF_PATTERN, channelNumber);
            case Y:
                return String.format(ZX_CH_TIFF_PATTERN, channelNumber);
            case Z:
                return String.format(XY_CH_TIFF_PATTERN, channelNumber);
            default:
                throw new IllegalArgumentException("Invalid slice axis " + sliceAxis);
        }
    }


    private final Coordinate sliceAxis;
    private final int channelCount;
    private final int[] tileSize; // tileSize in pixels
    private final int bitDepth;
    private final boolean srgb;

    @JsonCreator
    public TileInfo(@JsonProperty("sliceAxis") Coordinate sliceAxis,
                    @JsonProperty("channelCount") int channelCount,
                    @JsonProperty("tileSize") int[] tileSize,
                    @JsonProperty("bitDepth") int bitDepth,
                    @JsonProperty("srgb") boolean srgb) {
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

    public Coordinate getSliceAxis() {
        return sliceAxis;
    }

    public int[] getTileSize() {
        return tileSize;
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
