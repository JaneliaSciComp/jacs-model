package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.HttpClientProvider;
import org.janelia.rendering.utils.ImageUtils;
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
        private String storageId;
        @JsonProperty
        private String storageRootLocation;
        @JsonProperty
        private String storageRootPathURI;
        @JsonProperty
        private String nodeAccessURL;
        @JsonProperty
        private String nodeInfoURL;
        @JsonProperty
        private String nodeRelativePath; // node path relative to the root
        @JsonProperty
        private long size;
        @JsonProperty
        private String mimeType;
        @JsonProperty
        private boolean collectionFlag; // true if the node identifies a directory
        @JsonProperty
        private Date creationTime;
        @JsonProperty
        private Date lastModified;
    }

    private final String volumeBaseURI;
    private final String authToken;
    private final String storageServiceApiKey;
    private final HttpClientProvider httpClientProvider;

    public JADEBasedRenderedVolumeLocation(String volumeBaseURI, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        this.volumeBaseURI = volumeBaseURI;
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public URI getBaseURI() {
        return URI.create(volumeBaseURI);
    }

    @Override
    public List<URI> listImageUris(int level) {
        Client httpClient = null;
        try {
            httpClient = httpClientProvider.getClient();
            int detailLevel = level + 1;
            WebTarget target = httpClient.target(volumeBaseURI)
                    .path("list")
                    .queryParam("depth", detailLevel)
                    ;
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
                LOG.warn("List {} - depth {} returned {} status", volumeBaseURI, level, responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error listing content {} with depth {}", volumeBaseURI, level, e);
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
            WebTarget target = httpClient.target(volumeBaseURI)
                    .path("entry_info")
                    .path(tileRelativePath.replace('\\', '/'))
                    ;
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_JSON)).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return response.readEntity(RenderedImageInfo.class);
            } else {
                LOG.warn("Retrieve content info {} - {} returned {} status", volumeBaseURI, tileRelativePath, responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error retrieving content info from {} - {}", volumeBaseURI, tileRelativePath, e);
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
                        .putAll("selected", channelImageNames.stream().map(Paths::get).map(p -> p.getFileName()).map(p -> p.toString()).collect(Collectors.toList()))
                        .build()
        );
        try {
            return ByteStreams.toByteArray(tileImageStream);
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
            return ByteStreams.toByteArray(rawImageStream);
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
            WebTarget target = httpClient.target(volumeBaseURI)
                    .path("entry_content")
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            for (Map.Entry<String, String> qe : queryParams.entries()) {
                target = target.queryParam(qe.getKey(), qe.getValue());
            }
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_OCTET_STREAM)).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                return response.readEntity(InputStream.class);
            } else {
                LOG.warn("{} - {} returned {} status", volumeBaseURI, contentRelativePath, responseStatus);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error streaming data from {} - {}", volumeBaseURI, contentRelativePath, e);
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
