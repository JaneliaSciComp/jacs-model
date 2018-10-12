package org.janelia.model.rendering;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;

/**
 * An efficiently hashable key that uniquely identifies a particular
 * Tile2d or TileTexture in a RavelerTileServer.
 * 
 * @author brunsc
 *
 */
public class TileIndex {

	public static TileIndex fromTileCoord(int xTile, int yTile, int zTile,
										  int zoom,
										  CoordinateAxis sliceAxis,
										  int sliceIndex) {
		return new TileIndex(xTile, yTile, zTile, zoom, sliceAxis, sliceIndex);
	}

	public static TileIndex fromRavelerTileCoord(int xTile, int yTile, int zTile,
												 int zoom,
												 CoordinateAxis sliceAxis,
												 TileInfo tileInfo) {
		int zoomFactor = 1 << zoom;
		switch (sliceAxis) {
			case X:
				return new TileIndex(xTile / tileInfo.getVolumeSize()[CoordinateAxis.X.index()] / zoomFactor, yTile, zTile,
						zoom, sliceAxis, (xTile / zoomFactor) % tileInfo.getNumPages());
			case Y:
				return new TileIndex(xTile, yTile / tileInfo.getVolumeSize()[CoordinateAxis.Y.index()] / zoomFactor, zTile,
						zoom, sliceAxis, (yTile / zoomFactor) % tileInfo.getNumPages());
			case Z:
				return new TileIndex(xTile, yTile, zTile / tileInfo.getVolumeSize()[CoordinateAxis.Z.index()] / zoomFactor,
						zoom, sliceAxis, (zTile / zoomFactor) % tileInfo.getNumPages());
			default:
				throw new IllegalArgumentException("Invalid slice axis " + sliceAxis);
		}
	}

	private final int xyz[] = new int[3];
	private final int zoom;
	private final CoordinateAxis sliceAxis;
	private final int sliceIndex;
	
	private TileIndex(int x, int y, int z, int zoom,
                      CoordinateAxis sliceAxis,
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

	public CoordinateAxis getSliceAxis() {
		return sliceAxis;
	}

	public int getSliceIndex() {
		return sliceIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileIndex other = (TileIndex) obj;
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
