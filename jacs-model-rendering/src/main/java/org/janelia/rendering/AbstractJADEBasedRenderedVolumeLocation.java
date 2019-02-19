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
import javax.ws.rs.client.Invocation;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractJADEBasedRenderedVolumeLocation extends AbstractRenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJADEBasedRenderedVolumeLocation.class);

    static class ContentEntry {
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
    final String authToken;
    final String storageServiceApiKey;
    final HttpClientProvider httpClientProvider;

    AbstractJADEBasedRenderedVolumeLocation(String jadeBaseURI, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        this.jadeBaseURI = jadeBaseURI;
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public URI getBaseURI() {
        return URI.create(jadeBaseURI);
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

    abstract InputStream openContentStream(String contentRelativePath, Multimap<String, String> queryParams);

    Invocation.Builder createRequestWithCredentials(Invocation.Builder requestBuilder) {
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
