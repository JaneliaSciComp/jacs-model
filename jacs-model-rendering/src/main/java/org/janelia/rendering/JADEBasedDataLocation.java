package org.janelia.rendering;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.ClientProxy;
import org.janelia.rendering.utils.HttpClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JADEBasedDataLocation implements DataLocation {

    private static final Logger LOG = LoggerFactory.getLogger(JADEBasedDataLocation.class);

    final String jadeConnectionURI;
    final String jadeBaseDataStorageURI;
    final String baseDataStoragePath;
    final String authToken;
    final String storageServiceApiKey;
    final HttpClientProvider httpClientProvider;

    public JADEBasedDataLocation(String jadeConnectionURI, String jadeBaseDataStorageURI, String baseDataStoragePath, String authToken, String storageServiceApiKey, HttpClientProvider httpClientProvider) {
        Preconditions.checkArgument(StringUtils.isNotBlank(jadeConnectionURI));
        this.jadeConnectionURI = jadeConnectionURI;
        this.jadeBaseDataStorageURI = jadeBaseDataStorageURI;
        this.baseDataStoragePath = StringUtils.replace(StringUtils.defaultIfBlank(baseDataStoragePath, ""), "\\", "/");
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
    public String getBaseDataStoragePath() {
        return baseDataStoragePath;
    }

    @Override
    public String getContentURIFromRelativePath(String relativePath) {
        Preconditions.checkArgument(relativePath != null);
        return getNormalizedURI(jadeBaseDataStorageURI)
                .resolve("data_content/")
                .resolve(getNormalizedURI(relativizePathToRoot(baseDataStoragePath)))
                .resolve(relativePath.replace('\\', '/'))
                .toString()
                ;
    }

    @Override
    public String getContentURIFromAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return getNormalizedURI(jadeConnectionURI)
                .resolve("agent_storage/storage_path/data_content/")
                .resolve(relativizePathToRoot(absolutePath))
                .toString()
                ;
    }

    private String relativizePathToRoot(String p) {
        // replace patterns like C://, file:///D:/, // with ""
        return RegExUtils.removeFirst(StringUtils.replaceChars(p, '\\', '/'), "^((.+:)?/+)+");
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

    Streamable<InputStream> openContentStreamFromRelativePathToVolumeRoot(String contentRelativePath, Multimap<String, String> queryParams) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_content")
                    .path(baseDataStoragePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, volume path {}, relative path {} using {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, target.getUri());
            return openContentStream(target, queryParams);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, volume path {}, relative path {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    Streamable<InputStream> openContentStreamFromAbsolutePath(String contentAbsolutePath, Multimap<String, String> queryParams) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeConnectionURI)
                    .path("agent_storage/storage_path/data_content")
                    .path(contentAbsolutePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, base path {}, data path {} using {}", jadeConnectionURI, baseDataStoragePath, contentAbsolutePath, target.getUri());
            return openContentStream(target, queryParams);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, base path {}, data path {}", jadeConnectionURI, baseDataStoragePath, contentAbsolutePath, e);
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
                InputStream is = response.readEntity(InputStream.class);
                int length = response.getLength();
                return Streamable.of(
                        new FilterInputStream(is) {
                            @Override
                            public void close() throws IOException {
                                try {
                                    super.close();
                                } finally {
                                    // when closing the stream make sure this "connection" gets closed
                                    response.close();
                                }
                            }
                        },
                        length);
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
            LOG.debug("Open stream from URI {}, base path {}, data path {} using {}", jadeConnectionURI, baseDataStoragePath, absolutePath, target.getUri());
            return checkContent(target);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, base path {}, data path {}", jadeConnectionURI, baseDataStoragePath, absolutePath, e);
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
                    .path(baseDataStoragePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Check content from URI {}, base path {}, relative path {} using {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, target.getUri());
            return checkContent(target);
        } catch (Exception e) {
            LOG.debug("Error checking content from URI {}, base path {}, relative path {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private boolean checkContent(WebTarget endpoint) {
        try {
            LOG.debug("Check content from {}", endpoint.getUri());
            Response response;
            response = createRequestWithCredentials(endpoint, null).head();
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


    Invocation.Builder createRequestWithCredentials(WebTarget webTarget, String mediaType) {
        Invocation.Builder requestInvocationBuilder;
        if (StringUtils.isNotBlank(authToken)) {
            if (StringUtils.isNotBlank(mediaType)) {
                requestInvocationBuilder = webTarget.request(mediaType).header(
                        "Authorization",
                        "Bearer " + authToken);
            } else {
                requestInvocationBuilder = webTarget.request().header(
                        "Authorization",
                        "Bearer " + authToken);
            }
        } else if (StringUtils.isNotBlank(storageServiceApiKey)) {
            if (StringUtils.isNotBlank(mediaType)) {
                requestInvocationBuilder = webTarget.request(mediaType).header(
                        "Authorization",
                        "APIKEY " + storageServiceApiKey);
            } else {
                requestInvocationBuilder = webTarget.request().header(
                        "Authorization",
                        "APIKEY " + storageServiceApiKey);
            }
        } else {
            if (StringUtils.isNotBlank(mediaType)) {
                requestInvocationBuilder = webTarget.request(mediaType);
            } else {
                requestInvocationBuilder = webTarget.request();
            }
        }
        return requestInvocationBuilder;
    }

    ClientProxy getHttpClient() {
        return httpClientProvider.getClient();
    }
}
