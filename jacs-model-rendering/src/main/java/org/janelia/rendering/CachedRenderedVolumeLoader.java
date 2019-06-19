package org.janelia.rendering;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedRenderedVolumeLoader implements RenderedVolumeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CachedRenderedVolumeLoader.class);

    private final RenderedVolumeLoader impl;
    private final Cache<URI, Optional<RenderedVolume>> renderedVolumesCache;
    private final Cache<URI, Optional<byte[]>> renderedTileImagesCache;

    public CachedRenderedVolumeLoader(RenderedVolumeLoader impl, int volumesCacheSize, int tileImagesCacheSize) {
        this.impl = impl;
        if (volumesCacheSize > 0) {
            renderedVolumesCache = CacheBuilder.newBuilder()
                    .maximumSize(volumesCacheSize)
                    .build();
        } else {
            renderedVolumesCache = null;
        }
        if (tileImagesCacheSize > 0) {
            renderedTileImagesCache = CacheBuilder.newBuilder()
                    .maximumSize(tileImagesCacheSize)
                    .build();
        } else {
            renderedTileImagesCache = null;
        }
    }

    @Override
    public Optional<RenderedVolume> loadVolume(RenderedVolumeLocation rvl) {
        if (renderedVolumesCache == null) {
            return impl.loadVolume(rvl);
        } else {
            try {
                return renderedVolumesCache.get(rvl.getVolumeLocation(), () -> impl.loadVolume(rvl));
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey) {
        return renderedVolume.getRelativeTilePath(tileKey)
                .map(tilePath -> renderedVolume.getRvl().getVolumeLocation().resolve(tilePath + "/").resolve(tileKey.asPathComponents() + "/"))
                .flatMap(tileURI -> {
                    if (renderedTileImagesCache == null) {
                        return impl.loadSlice(renderedVolume, tileKey);
                    } else {
                        try {
                            LOG.trace("Try to retrieve tile {} from {} cache using {}", tileKey, renderedVolume.volumeLocation(), tileURI);
                            return renderedTileImagesCache.get(tileURI, () -> impl.loadSlice(renderedVolume, tileKey));
                        } catch (ExecutionException e) {
                            throw new IllegalStateException(e);
                        }
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

    @Override
    public List<RawImage> loadVolumeRawImageTiles(RenderedVolumeLocation rvl) {
        return impl.loadVolumeRawImageTiles(rvl);
    }
}
