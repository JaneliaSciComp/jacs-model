package org.janelia.rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;

public class RenderedVolumeMetadata {
    private String connectionURI;
    private String dataStorageURI;
    private String volumeBasePath;
    private RenderingType renderingType;
    private int[] originVoxel;
    private int[] volumeSizeInVoxels;
    private double[] micromsPerVoxel;
    private int numZoomLevels;
    private TileInfo xyTileInfo;
    private TileInfo yzTileInfo;
    private TileInfo zxTileInfo;

    public String getConnectionURI() {
        return connectionURI;
    }

    public void setConnectionURI(String connectionURI) {
        this.connectionURI = connectionURI;
    }

    public String getDataStorageURI() {
        return dataStorageURI;
    }

    public void setDataStorageURI(String dataStorageURI) {
        this.dataStorageURI = dataStorageURI;
    }

    public String getVolumeBasePath() {
        return volumeBasePath;
    }

    public void setVolumeBasePath(String volumeBasePath) {
        this.volumeBasePath = volumeBasePath;
    }

    public RenderingType getRenderingType() {
        return renderingType;
    }

    public void setRenderingType(RenderingType renderingType) {
        this.renderingType = renderingType;
    }

    public int[] getOriginVoxel() {
        return originVoxel;
    }

    public void setOriginVoxel(int[] originVoxel) {
        this.originVoxel = originVoxel;
    }

    public int[] getVolumeSizeInVoxels() {
        return volumeSizeInVoxels;
    }

    public void setVolumeSizeInVoxels(int[] volumeSizeInVoxels) {
        this.volumeSizeInVoxels = volumeSizeInVoxels;
    }

    public double[] getMicromsPerVoxel() {
        return micromsPerVoxel;
    }

    public void setMicromsPerVoxel(double[] micromsPerVoxel) {
        this.micromsPerVoxel = micromsPerVoxel;
    }

    public int getNumZoomLevels() {
        return numZoomLevels;
    }

    public void setNumZoomLevels(int numZoomLevels) {
        this.numZoomLevels = numZoomLevels;
    }

    public TileInfo getXyTileInfo() {
        return xyTileInfo;
    }

    public void setXyTileInfo(TileInfo xyTileInfo) {
        this.xyTileInfo = xyTileInfo;
    }

    public TileInfo getYzTileInfo() {
        return yzTileInfo;
    }

    public void setYzTileInfo(TileInfo yzTileInfo) {
        this.yzTileInfo = yzTileInfo;
    }

    public TileInfo getZxTileInfo() {
        return zxTileInfo;
    }

    public void setZxTileInfo(TileInfo zxTileInfo) {
        this.zxTileInfo = zxTileInfo;
    }

    public Optional<TileInfo> getTileInfo(Coordinate sliceAxis) {
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

    public boolean hasXSlices() {
        return yzTileInfo != null;
    }

    public boolean hasYSlices() {
        return zxTileInfo != null;
    }

    public boolean hasZSlices() {
        return xyTileInfo != null;
    }

    @JsonIgnore
    public int[] getCenterVoxel() {
        return Arrays.stream(volumeSizeInVoxels).map(coord -> coord / 2).toArray();
    }

    public Optional<String> getRelativeTilePath(TileKey tileKey) {
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
        return Optional.of(pathComps.stream().reduce(
                "",
                (c1, c2) -> {
                    if (StringUtils.isBlank(c1)) {
                        return c2;
                    } else {
                        return c1 + "/" + c2;
                    }
                }));
    }
}

