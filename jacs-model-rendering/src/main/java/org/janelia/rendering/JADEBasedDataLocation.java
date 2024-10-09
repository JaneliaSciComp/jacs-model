package org.janelia.rendering;

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
    final StorageOptions storageOptions;
    final HttpClientProvider httpClientProvider;

    public JADEBasedDataLocation(String jadeConnectionURI, String jadeBaseDataStorageURI, String baseDataStoragePath, String authToken, String storageServiceApiKey,
                                 StorageOptions storageOptions,
                                 HttpClientProvider httpClientProvider) {
        Preconditions.checkArgument(StringUtils.isNotBlank(jadeConnectionURI));
        this.jadeConnectionURI = jadeConnectionURI;
        this.jadeBaseDataStorageURI = jadeBaseDataStorageURI;
        this.baseDataStoragePath = StringUtils.replace(StringUtils.defaultIfBlank(baseDataStoragePath, ""), "\\", "/");
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
        this.storageOptions = storageOptions;
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
    public Streamable<InputStream> getContentFromRelativePath(String relativePath, StorageOptions storageOptions) {
        return openContentStreamFromRelativePathToVolumeRoot(relativePath, ImmutableMultimap.of(), storageOptions);
    }

    @Override
    public Streamable<InputStream> getContentFromAbsolutePath(String absolutePath, StorageOptions storageOptions) {
        long startTime = System.currentTimeMillis();
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
            return openContentStreamFromAbsolutePath(absolutePath, ImmutableMultimap.of(), storageOptions);
        } finally {
            LOG.info("Opened content bytes for streaming of {} in {} ms", absolutePath, System.currentTimeMillis() - startTime);
        }
    }

    Streamable<InputStream> openContentStreamFromRelativePathToVolumeRoot(String contentRelativePath, Multimap<String, String> queryParams, StorageOptions storageOptions) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_content")
                    .path(baseDataStoragePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, volume path {}, relative path {} using {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, target.getUri());
            return openContentStream(target, queryParams, storageOptions);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, volume path {}, relative path {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    Streamable<InputStream> openContentStreamFromAbsolutePath(String contentAbsolutePath, Multimap<String, String> queryParams, StorageOptions storageOptions) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeConnectionURI)
                    .path("agent_storage/storage_path/data_content")
                    .path(contentAbsolutePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, base path {}, data path {} using {}", jadeConnectionURI, baseDataStoragePath, contentAbsolutePath, target.getUri());
            return openContentStream(target, queryParams, storageOptions);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, base path {}, data path {}", jadeConnectionURI, baseDataStoragePath, contentAbsolutePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private Streamable<InputStream> openContentStream(WebTarget endpoint, Multimap<String, String> queryParams, StorageOptions storageOptions) {
        try {
            for (Map.Entry<String, String> qe : queryParams.entries()) {
                endpoint = endpoint.queryParam(qe.getKey(), qe.getValue());
            }
            LOG.debug("Open stream from {}", endpoint.getUri());
            Response response = createRequestWithCredentials(endpoint, MediaType.APPLICATION_OCTET_STREAM, storageOptions).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                InputStream is = response.readEntity(InputStream.class);
                int length = response.getLength();
                return Streamable.of(is, length);
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
    public boolean checkContentAtRelativePath(String relativePath, StorageOptions storageOptions) {
        return checkContentAtRelativePathToVolumeRoot(relativePath, storageOptions);
    }

    @Override
    public boolean checkContentAtAbsolutePath(String absolutePath, StorageOptions storageOptions) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeConnectionURI)
                    .path("agent_storage/storage_path/data_content")
                    .path(absolutePath.replace('\\', '/'))
                    ;
            LOG.debug("Open stream from URI {}, base path {}, data path {} using {}", jadeConnectionURI, baseDataStoragePath, absolutePath, target.getUri());
            return checkContent(target, storageOptions);
        } catch (Exception e) {
            LOG.debug("Error opening the stream from URI {}, base path {}, data path {}", jadeConnectionURI, baseDataStoragePath, absolutePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private boolean checkContentAtRelativePathToVolumeRoot(String contentRelativePath, StorageOptions storageOptions) {
        ClientProxy httpClient = getHttpClient();
        try {
            WebTarget target = httpClient.target(jadeBaseDataStorageURI)
                    .path("data_content")
                    .path(baseDataStoragePath)
                    .path(contentRelativePath.replace('\\', '/'))
                    ;
            LOG.debug("Check content from URI {}, base path {}, relative path {} using {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, target.getUri());
            return checkContent(target, storageOptions);
        } catch (Exception e) {
            LOG.debug("Error checking content from URI {}, base path {}, relative path {}", jadeBaseDataStorageURI, baseDataStoragePath, contentRelativePath, e);
            throw new IllegalStateException(e);
        } finally {
            httpClient.close();
        }
    }

    private boolean checkContent(WebTarget endpoint, StorageOptions storageOptions) {
        try {
            LOG.debug("Check content from {}", endpoint.getUri());
            Response response = createRequestWithCredentials(endpoint, null, storageOptions).head();
            int responseStatus = response.getStatus();
            response.close();
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


    Invocation.Builder createRequestWithCredentials(WebTarget webTarget, String mediaType, StorageOptions storageOptions) {
        Invocation.Builder requestInvocationBuilder = StringUtils.isNotBlank(mediaType) ? webTarget.request(mediaType) : webTarget.request();

        if (StringUtils.isNotBlank(authToken)) {
            requestInvocationBuilder.header("Authorization", "Bearer " + authToken);
        } else if (StringUtils.isNotBlank(storageServiceApiKey)) {
            requestInvocationBuilder.header("Authorization", "APIKEY " + storageServiceApiKey);
        }
        for (String storageAttribute : storageOptions.getAttributeNames()) {
            requestInvocationBuilder.header(storageAttribute, storageOptions.getAttributeValue(storageAttribute));
        }
        return requestInvocationBuilder;
    }

    ClientProxy getHttpClient() {
        return httpClientProvider.getClient();
    }
}
