package org.janelia.model.rendering;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RenderedVolume {

    private final RenderingType renderingType;
    private final int[] originVoxel;
    private final int[] volumeSizeInVoxels;
    private final double[] microsPerVoxel;
    private final int numZoomLevels;
    private final TileInfo xyTileInfo;
    private final TileInfo yzTileInfo;
    private final TileInfo zxTileInfo;

    RenderedVolume(RenderingType renderingType,
                   int[] originVoxel,
                   int[] volumeSizeInVoxels,
                   double[] microsPerVoxel,
                   int numZoomLevels,
                   TileInfo xyTileInfo,
                   TileInfo yzTileInfo,
                   TileInfo zxTileInfo) {
        this.renderingType = renderingType;
        this.originVoxel = originVoxel;
        this.volumeSizeInVoxels = volumeSizeInVoxels;
        this.microsPerVoxel = microsPerVoxel;
        this.numZoomLevels = numZoomLevels;
        this.xyTileInfo= xyTileInfo;
        this.yzTileInfo = yzTileInfo;
        this.zxTileInfo = zxTileInfo;
    }

    public int getNumZoomLevels() {
        return numZoomLevels;
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

    public Optional<Path> getTilePath(TileIndex tileIndex) {
        int depth = numZoomLevels - tileIndex.getZoom();
        if (depth < 0) {
            return Optional.empty();
        }
        int[] tile = tileIndex.getCoord();

        List<String> pathComps = new ArrayList<>();
        // start at lowest zoom to build up octree coordinates
        for (int d = 1; d < depth - 1; ++d) {
            int scale = 1 << (numZoomLevels - d);
            int ds[] = {
                    tile[0] / scale,
                    tile[1] / scale,
                    tile[2] / scale
            };
            // Each dimension makes a binary contribution to the
            // octree index.
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
