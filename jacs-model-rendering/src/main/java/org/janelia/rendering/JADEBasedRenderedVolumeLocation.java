package org.janelia.rendering;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
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
import org.janelia.rendering.utils.HttpClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JADEBasedRenderedVolumeLocation extends AbstractRenderedVolumeLocation {

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
    private final Client httpClient;

    public JADEBasedRenderedVolumeLocation(String jadeConnectionURI, String jadeBaseDataStorageURI, String renderedVolumePath, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        Preconditions.checkArgument(StringUtils.isNotBlank(jadeConnectionURI));
        this.jadeConnectionURI = jadeConnectionURI;
        this.jadeBaseDataStorageURI = jadeBaseDataStorageURI;
        this.renderedVolumePath = StringUtils.replace(StringUtils.defaultIfBlank(renderedVolumePath, ""), "\\", "/");
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
        this.httpClient = httpClientProvider.getClient();
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
        try {
            int detailLevel = level + 1;
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("list")
                    .path(renderedVolumePath)
                    .queryParam("depth", detailLevel)
                    ;
            LOG.debug("List images from URI {}, volume path {}, level {} using {}", jadeBaseDataStorageURI, renderedVolumePath, level, target.getUri());
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_JSON)).get();
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
        }
    }

    @Nullable
    @Override
    public RenderedImageInfo readTileImageInfo(String tileRelativePath) {
        long startTime = System.currentTimeMillis();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_info")
                    .path(renderedVolumePath)
                    .path(tileRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Read tile imageInfo from URI {}, volume path {}, tile path {} using {}", jadeBaseDataStorageURI, renderedVolumePath, tileRelativePath, target.getUri());
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_JSON)).get();
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
        }
    }

    @Nullable
    @Override
    public byte[] readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber) {
        long startTime = System.currentTimeMillis();
        Optional<StreamableContent> tileImageContent = openContentStreamFromRelativePathToVolumeRoot(tileRelativePath,
                ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_MERGE_BANDS")
                        .put("z", String.valueOf(pageNumber))
                        .putAll("selectedEntries", channelImageNames.stream().map(Paths::get).map(p -> p.getFileName()).map(p -> p.toString()).collect(Collectors.toList()))
                        .put("entryPattern", "")
                        .put("maxDepth", String.valueOf(1))
                        .build()
        );
        return tileImageContent.map(streamableTileImage -> {
            try {
                return ByteStreams.toByteArray(streamableTileImage.getStream());
            } catch (Exception e) {
                LOG.error("Error reading {} from {}", channelImageNames, tileRelativePath, e);
                throw new IllegalStateException(e);
            } finally {
                closeContentStream(streamableTileImage);
                LOG.info("Read texture bytes for {}[{}]:{} in {} ms",
                        Paths.get(renderedVolumePath, tileRelativePath),
                        channelImageNames.stream().reduce((c1, c2) -> c1 + "," + c2).orElse(""),
                        pageNumber,
                        System.currentTimeMillis() - startTime);
            }
        }).orElse(null);
    }

    @Nullable
    @Override
    public byte[] readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        String rawImagePath = rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel));
        Optional<StreamableContent> rawImageContent = openContentStreamFromAbsolutePath(rawImagePath,
                ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_ROI_PIXELS")
                        .put("xCenter", String.valueOf(xCenter))
                        .put("yCenter", String.valueOf(yCenter))
                        .put("zCenter", String.valueOf(zCenter))
                        .put("dimX", String.valueOf(dimx))
                        .put("dimY", String.valueOf(dimy))
                        .put("dimZ", String.valueOf(dimz))
                        .build()
        );
        return rawImageContent.map(streamableRawImage -> {
            try {
                return ByteStreams.toByteArray(streamableRawImage.getStream());
            } catch (Exception e) {
                LOG.error("Error reading {} from {}", rawImagePath, rawImage, e);
                throw new IllegalStateException(e);
            } finally {
                closeContentStream(streamableRawImage);
            }
        }).orElse(null);
    }

    @Override
    public Optional<StreamableContent> getContentFromRelativePath(String relativePath) {
        return openContentStreamFromRelativePathToVolumeRoot(relativePath, ImmutableMultimap.of());
    }

    @Override
    public Optional<StreamableContent> getContentFromAbsolutePath(String absolutePath) {
        long startTime = System.currentTimeMillis();
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
            return openContentStreamFromAbsolutePath(absolutePath, ImmutableMultimap.of());
        } finally {
            LOG.info("Opened content bytes for streaming of {} in {} ms", absolutePath, System.currentTimeMillis() - startTime);
        }
    }

    private Optional<StreamableContent> openContentStreamFromRelativePathToVolumeRoot(String contentRelativePath, Multimap<String, String> queryParams) {
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_content")
                    .path(renderedVolumePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, volume path {}, tile path {} using {}", jadeBaseDataStorageURI, renderedVolumePath, contentRelativePath, target.getUri());
            return openContentStream(target, queryParams);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, volume path {}, tile path {}", jadeBaseDataStorageURI, renderedVolumePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        }
    }

    private Optional<StreamableContent> openContentStreamFromAbsolutePath(String contentAbsolutePath, Multimap<String, String> queryParams) {
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
        }
    }

    private Optional<StreamableContent> openContentStream(WebTarget endpoint, Multimap<String, String> queryParams) {
        try {
            for (Map.Entry<String, String> qe : queryParams.entries()) {
                endpoint = endpoint.queryParam(qe.getKey(), qe.getValue());
            }
            LOG.debug("Open stream from {}", endpoint.getUri());
            Response response;
            response = createRequestWithCredentials(endpoint.request(MediaType.APPLICATION_OCTET_STREAM)).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return Optional.of(new StreamableContent(response.getLength(), response.readEntity(InputStream.class)));
            } else {
                LOG.debug("Open stream from {} returned status {}", endpoint.getUri(), responseStatus);
                return Optional.empty();
            }
        } catch (Exception e) {
            LOG.debug("Error opening the stream {}", endpoint, e);
            throw new IllegalStateException(e);
        }
    }

    private Invocation.Builder createRequestWithCredentials(Invocation.Builder requestBuilder) {
        Invocation.Builder requestWithCredentialsBuilder = requestBuilder;
        if (StringUtils.isNotBlank(authToken)) {
            requestWithCredentialsBuilder = requestWithCredentialsBuilder.header(
                    "Authorization",
                    "Bearer " + authToken);
        } else if (StringUtils.isNotBlank(storageServiceApiKey)) {
            requestWithCredentialsBuilder = requestWithCredentialsBuilder.header(
                    "Authorization",
                    "APIKEY " + storageServiceApiKey);
        }
        return requestWithCredentialsBuilder;
    }

}
