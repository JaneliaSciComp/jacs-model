package org.janelia.model.rendering;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RenderedVolume {

    private static final int[] DEFAULT_TILE_SIZE = new int[] {1024, 1024, 1};

    private int[] origin = new int[] {0, 0, 0};
    private int[] volumeSizeInVoxels = new int[] {0, 0, 0};
    private volatile int[] tileSizeInVoxels = DEFAULT_TILE_SIZE;
    private double[] voxelResolutionInMicroMeters = new double[] {1., 1., 1.};
    private int maxZoomLevel = 0;
    private int bitDepth;
    private int channelCount;
    private int intensityMax = 255;
    private int intensityMin = 0;
    private RenderingType renderingType = RenderingType.QUADTREE;
    private boolean[] orthoViews = new boolean[] {
            false /* X slices (yz views) */,
            false /* Y slices (zx views) */ ,
            true /* Z slices (xy views) */
    };

    public Optional<Path> getTilePath(TileIndex tileIndex) {
        int depth = maxZoomLevel - tileIndex.getZoom();
        if (depth < 0) {
            return Optional.empty();
        }
        int[] tile = tileIndex.getCoord();

        List<String> pathComps = new ArrayList<>();
        // start at lowest zoom to build up octree coordinates
        for (int d = 1; d < depth - 1; ++d) {
            int scale = 1 << (maxZoomLevel - d);
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