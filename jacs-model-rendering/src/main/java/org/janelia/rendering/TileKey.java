package org.janelia.rendering;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;

/**
 * An efficiently hashable key that uniquely identifies a particular
 * Tile2d or TileTexture in a RavelerTileServer.
 * 
 * @author brunsc
 *
 */
public class TileKey {

    static TileKey fromTileCoord(int xTile, int yTile, int zTile,
                                 int zoom,
                                 Coordinate sliceAxis,
                                 int sliceIndex) {
        return new TileKey(xTile, yTile, zTile, zoom, sliceAxis, sliceIndex);
    }

    public static TileKey fromRavelerTileCoord(int xTile, int yTile, int zTile,
                                               int zoom,
                                               Coordinate sliceAxis,
                                               TileInfo tileInfo) {
        int zoomFactor = 1 << zoom;
        switch (sliceAxis) {
            case X:
                return fromTileCoord(xTile / tileInfo.getVolumeSize()[Coordinate.X.index()] / zoomFactor, yTile, zTile,
                        zoom, sliceAxis, (xTile / zoomFactor) % tileInfo.getNumPages());
            case Y:
                // Raveller y is flipped so flip when slicing in Y
                return fromTileCoord(xTile, yTile / tileInfo.getVolumeSize()[Coordinate.Y.index()] / zoomFactor, zTile,
                        zoom, sliceAxis, tileInfo.getNumPages() - ((yTile / zoomFactor) % tileInfo.getNumPages()) - 1);
            case Z:
                return fromTileCoord(xTile, yTile, zTile / tileInfo.getVolumeSize()[Coordinate.Z.index()] / zoomFactor,
                        zoom, sliceAxis, (zTile / zoomFactor) % tileInfo.getNumPages());
            default:
                throw new IllegalArgumentException("Invalid slice axis " + sliceAxis);
        }
    }

    private final int xyz[] = new int[3];
    private final int zoom;
    private final Coordinate sliceAxis;
    private final int sliceIndex;

    private TileKey(int x, int y, int z, int zoom,
                    Coordinate sliceAxis,
                    int sliceIndex) {
        this.xyz[0] = x;
        this.xyz[1] = y;
        this.xyz[2] = z;
        this.zoom = zoom;
        this.sliceAxis = sliceAxis;
        this.sliceIndex = sliceIndex;
    }

    public int[] getCoord() {
        return Arrays.copyOf(xyz, 3);
    }

    public int getZoom() {
        return zoom;
    }

    public int getZoomFactor() {
        return 1 << zoom;
    }

    public Coordinate getSliceAxis() {
        return sliceAxis;
    }

    public int getSliceIndex() {
        return sliceIndex;
    }

    String asUri() {
        return sliceAxis.name() + "/"
                + zoom + "/"
                + xyz[0] + "/" + xyz[1] + "/" + xyz[1] + "/"
                + sliceIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TileKey other = (TileKey) obj;
        if (sliceAxis != other.sliceAxis) return false;
        for (int coordIndex = 0; coordIndex < xyz.length; ++coordIndex) {
            if (coordIndex == sliceAxis.index()) continue; // We will compare canonicalSliceAxis below
            if (xyz[coordIndex] != other.xyz[coordIndex]) return false;
        }
        if (zoom != other.zoom) return false;
        if (sliceIndex != other.sliceIndex) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + sliceAxis.index();
        for (int coordIndex = 0; coordIndex < xyz.length; ++coordIndex) {
            if (coordIndex == sliceAxis.index())
                continue; // We will use canonicalSliceAxis below
            else
                result = prime * result + xyz[coordIndex];
        }
        result = prime * result + zoom;
        result = prime * result + sliceIndex;
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
