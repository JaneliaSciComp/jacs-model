package org.janelia.rendering;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.ByteStreams;

import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.ClientProxy;
import org.janelia.rendering.utils.HttpClientProvider;
import org.janelia.rendering.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JADEBasedRenderedVolumeLocation extends JADEBasedDataLocation implements RenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(JADEBasedRenderedVolumeLocation.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ContentEntry {
        @JsonProperty
        String storageId;
        @JsonProperty
        String storageRootLocation;
        @JsonProperty
        String storageRootPathURI;
        @JsonProperty
        String nodeAccessURL;
        @JsonProperty
        String nodeInfoURL;
        @JsonProperty
        String nodeRelativePath; // node path relative to the root
        @JsonProperty
        long size;
        @JsonProperty
        String mimeType;
        @JsonProperty
        boolean collectionFlag; // true if the node identifies a directory
        @JsonProperty
        Date creationTime;
        @JsonProperty
        Date lastModified;
    }

    public JADEBasedRenderedVolumeLocation(String jadeConnectionURI, String jadeBaseDataStorageURI,
                                           String renderedVolumePath, String authToken, String storageServiceApiKey,
                                           StorageOptions storageOptions,
                                           HttpClientProvider httpClientProvider) {
        super(jadeConnectionURI, jadeBaseDataStorageURI, renderedVolumePath, authToken, storageServiceApiKey, storageOptions, httpClientProvider);
    }

    public JADEBasedRenderedVolumeLocation(JADEBasedDataLocation jadeBasedDataLocation) {
        super(jadeBasedDataLocation.jadeConnectionURI,
                jadeBasedDataLocation.jadeBaseDataStorageURI,
                jadeBasedDataLocation.baseDataStoragePath,
                jadeBasedDataLocation.authToken,
                jadeBasedDataLocation.storageServiceApiKey,
                jadeBasedDataLocation.storageOptions,
                jadeBasedDataLocation.httpClientProvider);
    }

    @Override
    public List<URI> listImageUris(int level) {
        ClientProxy httpClient = getHttpClient();
        try {
            int detailLevel = level + 1;
            WebTarget target = httpClient.target(getDataStorageURI())
                    .path("list")
                    .path(getBaseDataStoragePath())
                    .queryParam("depth", detailLevel)
                    ;
            LOG.debug("List images from URI {}, volume path {}, level {} using {}", getDataStorageURI(), getBaseDataStoragePath(), level, target.getUri());
            Response response = createRequestWithCredentials(target, MediaType.APPLICATION_JSON, storageOptions).get();
            int responseStatus = response.getStatus();
            List<URI> result;
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                List<ContentEntry> storageCotent = response.readEntity(new GenericType<List<ContentEntry>>(){});
                result = storageCotent.stream()
                        .filter(ce -> StringUtils.isNotBlank(ce.nodeRelativePath))
                        .filter(ce -> {
                            String imageRelativePath;
                            if (ce.nodeRelativePath.startsWith(getBaseDataStoragePath())) {
                                imageRelativePath = ce.nodeRelativePath.substring(getBaseDataStoragePath().length());
                            } else {
                                imageRelativePath = ce.nodeRelativePath;
                            }
                            return Paths.get(imageRelativePath).getNameCount() == detailLevel;
                        })
                        .filter(ce -> !ce.collectionFlag)
                        .filter(ce -> "image/tiff".equals(ce.mimeType))
                        .map(ce -> URI.create(ce.nodeAccessURL))
                        .collect(Collectors.toList());
            } else {
                LOG.warn("List images from URI {}, volume path {}, level {} ({}) returned status {}", getDataStorageURI(), getBaseDataStoragePath(), level, target.getUri(), responseStatus);
                result = null;
            }
            response.close();
            return result;
        } catch (Exception e) {
            LOG.error("Error listing images from URI {}, volume path {}, level {} returned status {}", getDataStorageURI(), getBaseDataStoragePath(), level, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    @Nullable
    @Override
    public RenderedImageInfo readTileImageInfo(String tileRelativePath, StorageOptions storageOptions) {
        long startTime = System.currentTimeMillis();
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(getDataStorageURI())
                    .path("data_info")
                    .path(getBaseDataStoragePath())
                    .path(tileRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Read tile imageInfo from URI {}, volume path {}, tile path {} using {}", getDataStorageURI(), getBaseDataStoragePath(), tileRelativePath, target.getUri());
            Response response = createRequestWithCredentials(
                    target,
                    MediaType.APPLICATION_JSON,
                    StorageOptions.combine(this.storageOptions, storageOptions)).get();
            int responseStatus = response.getStatus();
            RenderedImageInfo renderedImageInfo;
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                renderedImageInfo = response.readEntity(RenderedImageInfo.class);
            } else {
                LOG.warn("Retrieve content info from URI {}, volume path {}, tile path {} ({}) returned status {}", getDataStorageURI(), getBaseDataStoragePath(), tileRelativePath, target.getUri(), responseStatus);
                renderedImageInfo = null;
            }
            response.close();
            return renderedImageInfo;
        } catch (Exception e) {
            LOG.warn("Error retrieving content info from URI {}, volume path {}, tile path {} returned status {}", getDataStorageURI(), getBaseDataStoragePath(), tileRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            LOG.info("Read tile info for {} in {} ms", tileRelativePath, System.currentTimeMillis() - startTime);
            httpClient.close();
        }
    }

    @Override
    public Streamable<byte[]> readTiffPageAsTexturedBytes(String imageRelativePath, List<String> channelImageNames, int pageNumber, StorageOptions storageOptions) {
        long startTime = System.currentTimeMillis();
        // the imageRelativePath may be a path for filesystem storage or an URI for S3 storage
        URI imageLocation = URI.create(getBaseDataStoragePath()).resolve(imageRelativePath);
        try {
            return openContentStreamFromRelativePathToVolumeRoot(
                    imageRelativePath,
                    ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_MERGE_BANDS")
                        .put("z", String.valueOf(pageNumber))
                        .putAll("selectedEntries", channelImageNames.stream().map(this::fileNameFromChannelImagePath).collect(Collectors.toList()))
                        .put("entryPattern", "")
                        .put("maxDepth", String.valueOf(1))
                        .build(),
                    StorageOptions.combine(this.storageOptions, storageOptions))
                    .consume(textureStream -> {
                        try {
                            return ByteStreams.toByteArray(textureStream);
                        } catch (IOException e) {
                            LOG.error("Error reading texture bytes for {}[{}]:{}.",
                                    imageLocation,
                                    channelImageNames.stream().reduce((c1, c2) -> c1 + "," + c2).orElse(""),
                                    pageNumber,
                                    e);
                            throw new IllegalStateException(e);
                        } finally {
                            try {
                                textureStream.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }, (bytes, l) -> {
                        // if l is -1 the content-length was not set so in that case we take the bytes as they are
                        if (l != -1 && bytes.length != l) {
                            LOG.error("Error reading texture bytes for {}[{}]:{}. Number of bytes read from the stream ({}) must match the size ({})",
                                    imageLocation,
                                    channelImageNames.stream().reduce((c1, c2) -> c1 + "," + c2).orElse(""),
                                    pageNumber,
                                    bytes.length, l);
                            throw new IllegalStateException("Expected to read " + l + " bytes, but only read " + bytes.length);
                        }
                        return (long) bytes.length;
                    });
        } finally {
            LOG.info("Opened content for reading texture bytes for {}[{}]:{} in {} ms",
                    imageLocation,
                    channelImageNames.stream().reduce((c1, c2) -> c1 + "," + c2).orElse(""),
                    pageNumber,
                    System.currentTimeMillis() - startTime);
        }
    }

    private String fileNameFromChannelImagePath(String channelImagePath) {
        if (StringUtils.isBlank(channelImagePath)) {
            return "";
        } else {
            int separatorIndex = channelImagePath.lastIndexOf('/');
            if (separatorIndex != -1) {
                return channelImagePath.substring(separatorIndex + 1);
            } else {
                return channelImagePath;
            }
        }
    }

    @Override
    public Streamable<byte[]> readTiffImageROIPixels(String imagePath, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz, StorageOptions storageOptions) {
        return openContentStreamFromAbsolutePath(
                imagePath,
                ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_ROI_PIXELS")
                        .put("xCenter", String.valueOf(xCenter))
                        .put("yCenter", String.valueOf(yCenter))
                        .put("zCenter", String.valueOf(zCenter))
                        .put("dimX", String.valueOf(dimx))
                        .put("dimY", String.valueOf(dimy))
                        .put("dimZ", String.valueOf(dimz))
                        .build(),
                StorageOptions.combine(this.storageOptions, storageOptions))
                .consume(imageStream -> {
                    try {
                        return ImageUtils.loadImagePixelBytesFromTiffStream(imageStream, xCenter, yCenter, zCenter, dimx, dimy, dimz);
                    } finally {
                        try {
                            imageStream.close();
                        } catch (Exception ignore) {
                        }
                    }
                }, (bytes, l) -> {
                    // if l is -1 the content-length was not set so in that case we take the bytes as they are
                    if (l != -1 && bytes.length != l) {
                        LOG.error("Number of bytes read from the stream ({}) must match the size ({})", bytes.length, l);
                        throw new IllegalStateException("Expected to read " + l + " bytes, but only read " + bytes.length);
                    }
                    return (long) bytes.length;
                });
    }

}
