package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.HttpClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JADEBasedRenderedVolumeLocation extends AbstractRenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(JADEBasedRenderedVolumeLocation.class);

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

    final String jadeBaseURI;
    final String renderedVolumePath;
    final String authToken;
    final String storageServiceApiKey;
    final HttpClientProvider httpClientProvider;

    public JADEBasedRenderedVolumeLocation(String jadeBaseURI, String renderedVolumePath, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        this.jadeBaseURI = jadeBaseURI;
        this.renderedVolumePath = StringUtils.replace(StringUtils.defaultIfBlank(renderedVolumePath, ""), "\\", "/");
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public URI getBaseURI() {
        return URI.create(jadeBaseURI);
    }

    @Override
    public String getRenderedVolumePath() {
        return renderedVolumePath;
    }

    @Override
    public List<URI> listImageUris(int level) {
        Client httpClient = null;
        try {
            httpClient = httpClientProvider.getClient();
            int detailLevel = level + 1;
            WebTarget target = httpClient.target(jadeBaseURI)
                    .path("list")
                    .path(renderedVolumePath)
                    .queryParam("depth", detailLevel)
                    ;
            LOG.debug("List images from URI {}, volume path {}, level {} using {}", jadeBaseURI, renderedVolumePath, level, target.getUri());
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
                LOG.warn("List {} - depth {} returned {} status", jadeBaseURI, level, responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error listing content {} with depth {}", jadeBaseURI, level, e);
            throw new IllegalStateException(e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    @Nullable
    @Override
    public RenderedImageInfo readTileImageInfo(String tileRelativePath) {
        Client httpClient = null;
        try {
            httpClient = httpClientProvider.getClient();
            WebTarget target = httpClient.target(jadeBaseURI)
                    .path("data_info")
                    .path(renderedVolumePath)
                    .path(tileRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Read tile imageInfo from URI {}, volume path {}, tile path {} using {}", jadeBaseURI, renderedVolumePath, tileRelativePath, target.getUri());
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_JSON)).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return response.readEntity(RenderedImageInfo.class);
            } else {
                LOG.warn("Retrieve content info {} - {} returned {} status", jadeBaseURI, tileRelativePath, responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error retrieving content info from {} - {}", jadeBaseURI, tileRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    @Nullable
    @Override
    public byte[] readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber) {
        InputStream tileImageStream = openContentStream(tileRelativePath,
                ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_MERGE_BANDS")
                        .put("z", String.valueOf(pageNumber))
                        .putAll("selectedEntries", channelImageNames.stream().map(Paths::get).map(p -> p.getFileName()).map(p -> p.toString()).collect(Collectors.toList()))
                        .put("entryPattern", "")
                        .put("maxDepth", String.valueOf(1))
                        .build()
        );
        try {
            if (tileImageStream == null) {
                return null;
            } else {
                return ByteStreams.toByteArray(tileImageStream);
            }
        } catch (Exception e) {
            LOG.error("Error reading {} from {}", channelImageNames, tileRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            closeContentStream(tileImageStream);
        }
    }

    @Override
    public byte[] readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        Path rawImagePath = rawImage.getRawImagePath(String.format(RAW_CH_TIFF_PATTERN, channel));
        InputStream rawImageStream = openContentStream(rawImagePath.toString(),
                ImmutableMultimap.<String, String>builder()
                        .put("filterType", "TIFF_ROI_PIXELS")
                        .put("xCenter", String.valueOf(xCenter - dimx / 2))
                        .put("yCenter", String.valueOf(yCenter - dimy / 2))
                        .put("zCenter", String.valueOf(zCenter - dimz / 2))
                        .put("dimX", String.valueOf(dimx))
                        .put("dimY", String.valueOf(dimy))
                        .put("dimZ", String.valueOf(dimz))
                        .build()
        );
        try {
            if (rawImageStream == null) {
                return null;
            } else {
                return ByteStreams.toByteArray(rawImageStream);
            }
        } catch (Exception e) {
            LOG.error("Error reading {} from {}", rawImagePath, rawImage, e);
            throw new IllegalStateException(e);
        } finally {
            closeContentStream(rawImageStream);
        }
    }

    @Nullable
    @Override
    public InputStream readTransformData() {
        return openContentStream(TRANSFORM_FILE_NAME, ImmutableMultimap.of());
    }

    @Nullable
    @Override
    public InputStream readTileBaseData() {
        return openContentStream(TILED_VOL_BASE_FILE_NAME, ImmutableMultimap.of());
    }

    private InputStream openContentStream(String contentRelativePath, Multimap<String, String> queryParams) {
        Client httpClient = null;
        try {
            httpClient = httpClientProvider.getClient();
            WebTarget target = httpClient.target(jadeBaseURI)
                    .path("data_content")
                    .path(renderedVolumePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            for (Map.Entry<String, String> qe : queryParams.entries()) {
                target = target.queryParam(qe.getKey(), qe.getValue());
            }
            LOG.debug("Open stream for {} with {} for {}", contentRelativePath, queryParams, target.getUri());
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_OCTET_STREAM)).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return response.readEntity(InputStream.class);
            } else {
                LOG.warn("{} - {} returned {} status", jadeBaseURI, contentRelativePath, responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error streaming data from {} - {}", jadeBaseURI, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
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
