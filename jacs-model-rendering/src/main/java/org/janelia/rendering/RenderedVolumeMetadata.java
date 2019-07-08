package org.janelia.rendering;

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
}
