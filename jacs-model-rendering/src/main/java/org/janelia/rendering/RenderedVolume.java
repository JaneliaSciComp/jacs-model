package org.janelia.rendering;

public class RenderedVolume {

    private final RenderedVolumeLocation volumeLocation;
    private final RenderedVolumeMetadata volumeMetadata;

    public RenderedVolume(RenderedVolumeLocation volumeLocation, RenderedVolumeMetadata volumeMetadata) {
        this.volumeLocation = volumeLocation;
        this.volumeMetadata = volumeMetadata;
    }

    public RenderedVolumeLocation getVolumeLocation() {
        return volumeLocation;
    }

    public RenderedVolumeMetadata getVolumeMetadata() {
        return volumeMetadata;
    }
}
