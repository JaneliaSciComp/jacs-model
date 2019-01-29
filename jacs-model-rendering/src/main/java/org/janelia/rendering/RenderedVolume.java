package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RenderedVolume {

    private final Path basePath;
    private final RenderingType renderingType;
    private final int[] originVoxel;
    private final int[] volumeSizeInVoxels;
    private final double[] micromsPerVoxel;
    private final int numZoomLevels;
    private final TileInfo xyTileInfo;
    private final TileInfo yzTileInfo;
    private final TileInfo zxTileInfo;

    RenderedVolume(Path basePath,
                   RenderingType renderingType,
                   int[] originVoxel,
                   int[] volumeSizeInVoxels,
                   double[] micromsPerVoxel,
                   int numZoomLevels,
                   TileInfo xyTileInfo,
                   TileInfo yzTileInfo,
                   TileInfo zxTileInfo) {
        this.basePath = basePath;
        this.renderingType = renderingType;
        this.originVoxel = originVoxel;
        this.volumeSizeInVoxels = volumeSizeInVoxels;
        this.micromsPerVoxel = micromsPerVoxel;
        this.numZoomLevels = numZoomLevels;
        this.xyTileInfo= xyTileInfo;
        this.yzTileInfo = yzTileInfo;
        this.zxTileInfo = zxTileInfo;
    }

    Path getBasePath() {
        return basePath;
    }

    public RenderingType getRenderingType() {
        return renderingType;
    }

    public int getNumZoomLevels() {
        return numZoomLevels;
    }

    public int[] getOriginVoxel() {
        return originVoxel;
    }

    public int[] getVolumeSizeInVoxels() {
        return volumeSizeInVoxels;
    }

    public double[] getMicromsPerVoxel() {
        return micromsPerVoxel;
    }

    public Optional<TileInfo> getTileInfo(CoordinateAxis sliceAxis) {
        switch (sliceAxis) {
            case X:
                return yzTileInfo == null ? Optional.empty() : Optional.of(yzTileInfo);
            case Y:
                return zxTileInfo == null ? Optional.empty() : Optional.of(zxTileInfo);
            case Z:
                return xyTileInfo == null ? Optional.empty() : Optional.of(xyTileInfo);
            default:
                return Optional.empty();
        }
    }

    @JsonIgnore
    public int[] getCenterVoxel() {
        return Arrays.stream(volumeSizeInVoxels).map(coord -> coord / 2).toArray();
    }

    public int[] convertToMicroscopeCoord(int[] screenCoord) {
        int[] microscopeCoord = new int[3];
        for ( int i = 0; i < screenCoord.length; i++ ) {
            microscopeCoord[i] = originVoxel[i] + (int)(screenCoord[i] * micromsPerVoxel[i]);
        }
        return microscopeCoord;
    }

    public TileInfo getXyTileInfo() {
        return xyTileInfo;
    }

    public TileInfo getYzTileInfo() {
        return yzTileInfo;
    }

    public TileInfo getZxTileInfo() {
        return zxTileInfo;
    }

    public boolean hasXSlices() {
        return yzTileInfo != null;
    }

    public boolean hasYSlices() {
        return zxTileInfo != null;
    }

    public boolean hasZSlices() {
        return xyTileInfo != null;
    }

    public Optional<Path> getRelativeTilePath(TileKey tileKey) {
        int depth = numZoomLevels - tileKey.getZoom();
        if (depth < 0) {
            return Optional.empty();
        }
        int[] tile = tileKey.getCoord();

        List<String> pathComps = new ArrayList<>();
        // start at lowest zoom to build up octree coordinates
        for (int d = 0; d < depth - 1; ++d) {
            int scale = 1 << (depth - d - 2);
            int ds[] = {
                    tile[0] / scale,
                    tile[1] / scale,
                    tile[2] / scale
            };
            // Each dimension makes a binary contribution to the octree index.
            // Check if the index is valid
            for (int index : ds) {
                if (index < 0) {
                    return Optional.empty();
                }
                if (index > 1) {
                    return Optional.empty();
                }
            }
            // offset x/y/z for next deepest level
            for (int i = 0; i < 3; ++i) {
                tile[i] = tile[i] % scale;
            }

            // Octree coordinates are in z-order
            int octreeCoord = 1 + ds[0]
                    + 2 * (1 - ds[1]) // Raveler Y is at bottom; octree Y is at top
                    + 4 * ds[2];
            pathComps.add(String.valueOf(octreeCoord));
        }
        return Optional.of(Paths.get("", pathComps.toArray(new String[0])));
    }

}
