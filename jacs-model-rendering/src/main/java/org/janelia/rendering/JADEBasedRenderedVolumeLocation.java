package org.janelia.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;

import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.ClientProxy;
import org.janelia.rendering.utils.HttpClientProvider;
import org.janelia.rendering.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JADEBasedRenderedVolumeLocation implements RenderedVolumeLocation {

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

    private final String jadeConnectionURI;
    private final String jadeBaseDataStorageURI;
    private final String renderedVolumePath;
    private final String authToken;
    private final String storageServiceApiKey;
    private final HttpClientProvider httpClientProvider;

    public JADEBasedRenderedVolumeLocation(String jadeConnectionURI, String jadeBaseDataStorageURI, String renderedVolumePath, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        Preconditions.checkArgument(StringUtils.isNotBlank(jadeConnectionURI));
        this.jadeConnectionURI = jadeConnectionURI;
        this.jadeBaseDataStorageURI = jadeBaseDataStorageURI;
        this.renderedVolumePath = StringUtils.replace(StringUtils.defaultIfBlank(renderedVolumePath, ""), "\\", "/");
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public URI getConnectionURI() {
        return getNormalizedURI(jadeConnectionURI);
    }

    @Override
    public URI getDataStorageURI() {
        return getNormalizedURI(jadeBaseDataStorageURI);
    }

    private URI getNormalizedURI(String uri) {
        String normalizedURI;
        if (uri.endsWith("/")) {
            normalizedURI = uri;
        } else {
            normalizedURI = uri + "/";
        }
        return URI.create(normalizedURI);
    }

    @Override
    public String getRenderedVolumePath() {
        return renderedVolumePath;
    }

    @Override
    public List<URI> listImageUris(int level) {
        ClientProxy httpClient = getHttpClient();
        try {
            int detailLevel = level + 1;
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("list")
                    .path(renderedVolumePath)
                    .queryParam("depth", detailLevel)
                    ;
            LOG.debug("List images from URI {}, volume path {}, level {} using {}", jadeBaseDataStorageURI, renderedVolumePath, level, target.getUri());
            Response response;
            response = createRequestWithCredentials(target, MediaType.APPLICATION_JSON).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                List<ContentEntry> storageCotent = response.readEntity(new GenericType<List<ContentEntry>>(){});
                return storageCotent.stream()
                        .filter(ce -> StringUtils.isNotBlank(ce.nodeRelativePath))
                        .filter(ce -> Paths.get(ce.nodeRelativePath).getNameCount() == detailLevel)
                        .filter(ce -> !ce.collectionFlag)
                        .filter(ce -> "image/tiff".equals(ce.mimeType))
                        .map(ce -> URI.create(ce.nodeAccessURL))
                        .collect(Collectors.toList());
            } else {
                LOG.warn("List images from URI {}, volume path {}, level {} ({}) returned status {}", jadeBaseDataStorageURI, renderedVolumePath, level, target.getUri(), responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error listing images from URI {}, volume path {}, level {} returned status {}", jadeBaseDataStorageURI, renderedVolumePath, level, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    @Nullable
    @Override
    public RenderedImageInfo readTileImageInfo(String tileRelativePath) {
        long startTime = System.currentTimeMillis();
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_info")
                    .path(renderedVolumePath)
                    .path(tileRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Read tile imageInfo from URI {}, volume path {}, tile path {} using {}", jadeBaseDataStorageURI, renderedVolumePath, tileRelativePath, target.getUri());
            Response response;
            response = createRequestWithCredentials(target, MediaType.APPLICATION_JSON).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return response.readEntity(RenderedImageInfo.class);
            } else {
                LOG.warn("Retrieve content info from URI {}, volume path {}, tile path {} ({}) returned status {}", jadeBaseDataStorageURI, renderedVolumePath, tileRelativePath, target.getUri(), responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.warn("Error retrieving content info from URI {}, volume path {}, tile path {} returned status {}", jadeBaseDataStorageURI, renderedVolumePath, tileRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            LOG.info("Read tile info for {} in {} ms", tileRelativePath, System.currentTimeMillis() - startTime);
            httpClient.close();
        }
    }

    @Override
    public Streamable<byte[]> readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber) {
        long startTime = System.currentTimeMillis();
        try {
            return openContentStreamFromRelativePathToVolumeRoot(
                    tileRelativePath,
                    ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_MERGE_BANDS")
                        .put("z", String.valueOf(pageNumber))
                        .putAll("selectedEntries", channelImageNames.stream().map(Paths::get).map(p -> p.getFileName()).map(p -> p.toString()).collect(Collectors.toList()))
                        .put("entryPattern", "")
                        .put("maxDepth", String.valueOf(1))
                        .build())
                    .consume(textureStream -> {
                        try {
                            return ByteStreams.toByteArray(textureStream);
                        } catch (IOException e) {
                            LOG.error("Error reading texture bytes for {}[{}]:{}.",
                                    Paths.get(renderedVolumePath, tileRelativePath),
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
                        if (bytes.length != l) {
                            LOG.error("Error reading texture bytes for {}[{}]:{}. Number of bytes read from the stream ({}) must match the size ({})",
                                    Paths.get(renderedVolumePath, tileRelativePath),
                                    channelImageNames.stream().reduce((c1, c2) -> c1 + "," + c2).orElse(""),
                                    pageNumber,
                                    bytes.length, l);
                            throw new IllegalStateException("Expected to read " + l + " bytes, but only read " + bytes.length);
                        }
                        return l;
                    });
        } finally {
            LOG.info("Opened content for reading texture bytes for {}[{}]:{} in {} ms",
                    Paths.get(renderedVolumePath, tileRelativePath),
                    channelImageNames.stream().reduce((c1, c2) -> c1 + "," + c2).orElse(""),
                    pageNumber,
                    System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public Streamable<byte[]> readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        String rawImagePath = rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel));
        return openContentStreamFromAbsolutePath(
                rawImagePath,
                ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_ROI_PIXELS")
                        .put("xCenter", String.valueOf(xCenter))
                        .put("yCenter", String.valueOf(yCenter))
                        .put("zCenter", String.valueOf(zCenter))
                        .put("dimX", String.valueOf(dimx))
                        .put("dimY", String.valueOf(dimy))
                        .put("dimZ", String.valueOf(dimz))
                        .build())
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
                    if (bytes.length != l) {
                        LOG.error("Number of bytes read from the stream ({}) must match the size ({})", bytes.length, l);
                        throw new IllegalStateException("Expected to read " + l + " bytes, but only read " + bytes.length);
                    }
                    return l;
                });
    }

    @Override
    public Streamable<InputStream> getContentFromRelativePath(String relativePath) {
        return openContentStreamFromRelativePathToVolumeRoot(relativePath, ImmutableMultimap.of());
    }

    @Override
    public Streamable<InputStream> getContentFromAbsolutePath(String absolutePath) {
        long startTime = System.currentTimeMillis();
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
            return openContentStreamFromAbsolutePath(absolutePath, ImmutableMultimap.of());
        } finally {
            LOG.info("Opened content bytes for streaming of {} in {} ms", absolutePath, System.currentTimeMillis() - startTime);
        }
    }

    private Streamable<InputStream> openContentStreamFromRelativePathToVolumeRoot(String contentRelativePath, Multimap<String, String> queryParams) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_content")
                    .path(renderedVolumePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, volume path {}, relative path {} using {}", jadeBaseDataStorageURI, renderedVolumePath, contentRelativePath, target.getUri());
            return openContentStream(target, queryParams);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, volume path {}, relative path {}", jadeBaseDataStorageURI, renderedVolumePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private Streamable<InputStream> openContentStreamFromAbsolutePath(String contentAbsolutePath, Multimap<String, String> queryParams) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeConnectionURI)
                    .path("agent_storage/storage_path/data_content")
                    .path(contentAbsolutePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, path {}, tile path {} using {}", jadeConnectionURI, renderedVolumePath, contentAbsolutePath, target.getUri());
            return openContentStream(target, queryParams);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, path {}, tile path {}", jadeConnectionURI, renderedVolumePath, contentAbsolutePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private Streamable<InputStream> openContentStream(WebTarget endpoint, Multimap<String, String> queryParams) {
        try {
            for (Map.Entry<String, String> qe : queryParams.entries()) {
                endpoint = endpoint.queryParam(qe.getKey(), qe.getValue());
            }
            LOG.debug("Open stream from {}", endpoint.getUri());
            Response response;
            response = createRequestWithCredentials(endpoint, MediaType.APPLICATION_OCTET_STREAM).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return Streamable.of(response.readEntity(InputStream.class), response.getLength());
            } else {
                LOG.debug("Open stream from {} returned status {}", endpoint.getUri(), responseStatus);
                return Streamable.empty();
            }
        } catch (Exception e) {
            LOG.debug("Error opening the stream {}", endpoint, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean checkContentAtRelativePath(String relativePath) {
        return checkContentAtRelativePathToVolumeRoot(relativePath);
    }

    @Override
    public boolean checkContentAtAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeConnectionURI)
                    .path("agent_storage/storage_path/data_content")
                    .path(absolutePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, path {}, tile path {} using {}", jadeConnectionURI, renderedVolumePath, absolutePath, target.getUri());
            return checkContent(target);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, path {}, tile path {}", jadeConnectionURI, renderedVolumePath, absolutePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private boolean checkContentAtRelativePathToVolumeRoot(String contentRelativePath) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_content")
                    .path(renderedVolumePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Check content from URI {}, volume path {}, relative path {} using {}", jadeBaseDataStorageURI, renderedVolumePath, contentRelativePath, target.getUri());
            return checkContent(target);
        } catch (Exception e) {
            LOG.debug("Error checking content from URI {}, volume path {}, relative path {}", jadeBaseDataStorageURI, renderedVolumePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private boolean checkContent(WebTarget endpoint) {
        try {
            LOG.debug("Check content from {}", endpoint.getUri());
            Response response;
            response = createRequestWithCredentials(endpoint, MediaType.APPLICATION_OCTET_STREAM).head();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return true;
            } else {
                LOG.debug("Check content from {} returned status {}", endpoint.getUri(), responseStatus);
                return false;
            }
        } catch (Exception e) {
            LOG.debug("Error opening the stream {}", endpoint, e);
            throw new IllegalStateException(e);
        }
    }

    private Invocation.Builder createRequestWithCredentials(WebTarget webTarget, String mediaType) {
        Invocation.Builder requestInvocationBuilder;
        if (StringUtils.isNotBlank(authToken)) {
            requestInvocationBuilder = webTarget.request(mediaType).header(
                    "Authorization",
                    "Bearer " + authToken);
        } else if (StringUtils.isNotBlank(storageServiceApiKey)) {
            requestInvocationBuilder = webTarget.request(mediaType).header(
                    "Authorization",
                    "APIKEY " + storageServiceApiKey);
        } else {
            requestInvocationBuilder = webTarget.request(mediaType);
        }
        return requestInvocationBuilder;
    }

    private ClientProxy getHttpClient() {
        return httpClientProvider.getClient();
    }
}
