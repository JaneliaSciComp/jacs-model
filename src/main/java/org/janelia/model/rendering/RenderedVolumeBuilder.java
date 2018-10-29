package org.janelia.model.rendering;

import java.nio.file.Path;
import java.util.Arrays;

public class RenderedVolumeBuilder {

    private Path basePath;
    private RenderingType renderingType;
    private int[] originVoxel;
    private int[] volumeSizeInVoxels;
    private double[] micromsPerVoxel;
    private int numZoomLevels;
    private TileInfo xyTileInfo;
    private TileInfo yzTileInfo;
    private TileInfo zxTileInfo;

    public static RenderedVolumeBuilder octreeBuilder(Path basePath) {
        RenderedVolumeBuilder volumeBuilder = new RenderedVolumeBuilder();
        volumeBuilder.renderingType = RenderingType.OCTREE;
        volumeBuilder.basePath = basePath;
        return volumeBuilder;
    }

    public RenderedVolumeBuilder withOrigin(int[] origin) {
        this.originVoxel = Arrays.copyOf(origin, origin.length);
        return this;
    }

    public RenderedVolumeBuilder withSize(int[] size) {
        this.volumeSizeInVoxels = Arrays.copyOf(size, size.length);
        return this;
    }

    public RenderedVolumeBuilder withMicromsPerVoxel(double[] resolution) {
        this.micromsPerVoxel = Arrays.copyOf(resolution, resolution.length);
        return this;
    }

    public RenderedVolumeBuilder withZoomLevelsCount(int zoomLevelsCount) {
        this.numZoomLevels = zoomLevelsCount;
        return this;
    }

    public RenderedVolumeBuilder withXYTile(int channels, int[] size, int bitDepth, boolean srgb) {
        this.xyTileInfo = new TileInfo(CoordinateAxis.Z, channels, Arrays.copyOf(size, size.length), bitDepth, srgb);
        return this;
    }

    public RenderedVolumeBuilder withYZTile(int channels, int[] size, int bitDepth, boolean srgb) {
        this.yzTileInfo = new TileInfo(CoordinateAxis.X, channels, Arrays.copyOf(size, size.length), bitDepth, srgb);
        return this;
    }

    public RenderedVolumeBuilder withZXTile(int channels, int[] size, int bitDepth, boolean srgb) {
        this.zxTileInfo = new TileInfo(CoordinateAxis.Y, channels, Arrays.copyOf(size, size.length), bitDepth, srgb);
        return this;
    }

    public RenderedVolume build() {
        return new RenderedVolume(
                basePath,
                renderingType,
                originVoxel,
                volumeSizeInVoxels,
                micromsPerVoxel,
                numZoomLevels,
                xyTileInfo,
                yzTileInfo,
                zxTileInfo);
    }
}
