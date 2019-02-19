package org.janelia.rendering;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.HttpClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JADEBundleBasedRenderedVolumeLocation extends AbstractJADEBasedRenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(JADEBundleBasedRenderedVolumeLocation.class);

    public JADEBundleBasedRenderedVolumeLocation(String jadeBaseURI, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        super(jadeBaseURI, authToken, storageServiceApiKey, httpClientProvider);
    }

    @Override
    public List<URI> listImageUris(int level) {
        Client httpClient = null;
        try {
            httpClient = httpClientProvider.getClient();
            int detailLevel = level + 1;
            WebTarget target = httpClient.target(jadeBaseURI)
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
                    .path("entry_info")
                    .path(tileRelativePath.replace('\\', '/'))
                    ;
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

    @Override
    InputStream openContentStream(String contentRelativePath, Multimap<String, String> queryParams) {
        Client httpClient = null;
        try {
            httpClient = httpClientProvider.getClient();
            WebTarget target = httpClient.target(jadeBaseURI)
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

}
