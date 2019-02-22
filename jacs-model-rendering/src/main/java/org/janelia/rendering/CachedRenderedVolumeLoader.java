package org.janelia.rendering;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.cdi.WithCache;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@WithCache
public class CachedRenderedVolumeLoader implements RenderedVolumeLoader {

    private static final Cache<URI, Optional<RenderedVolume>> RENDERED_VOLUMES_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .build();

    private static final Cache<URI, Optional<byte[]>> IMAGE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .build();

    private final RenderedVolumeLoader impl;

    @Inject
    public CachedRenderedVolumeLoader(RenderedVolumeLoader impl) {
        this.impl = impl;
    }

    @Override
    public Optional<RenderedVolume> loadVolume(RenderedVolumeLocation rvl) {
        try {
            return RENDERED_VOLUMES_CACHE.get(getRenderedVolumeKey(rvl), () -> impl.loadVolume(rvl));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey) {
        return renderedVolume.getRelativeTilePath(tileKey)
                .map(tilePath -> getRenderedVolumeKey(renderedVolume.getRvl()).resolve(tilePath.toString() + "/").resolve(tileKey.asPathComponents() + "/"))
                .flatMap(tileURI -> {
                    try {
                        return IMAGE_CACHE.get(tileURI, () -> impl.loadSlice(renderedVolume, tileKey));
                    } catch (ExecutionException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    @Override
    public Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int x, int y, int z) {
        return impl.findClosestRawImageFromVoxelCoord(rvl, x, y, z);
    }

    @Override
    public byte[] loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                                    RawImage rawImage,
                                                    int channel,
                                                    int x, int y, int z,
                                                    int dimx, int dimy, int dimz) {
        return impl.loadRawImageContentFromVoxelCoord(rvl, rawImage, channel, x, y, z, dimx, dimy, dimz);
    }

    private URI getRenderedVolumeKey(RenderedVolumeLocation rvl) {
        String renderedVolumePath = rvl.getRenderedVolumePath();
        if (StringUtils.isBlank(renderedVolumePath)) {
            return rvl.getBaseURI();
        } else if (renderedVolumePath.endsWith("/")) {
            return rvl.getBaseURI().resolve(renderedVolumePath);
        } else {
            return rvl.getBaseURI().resolve(renderedVolumePath + "/");
        }
    }
}
