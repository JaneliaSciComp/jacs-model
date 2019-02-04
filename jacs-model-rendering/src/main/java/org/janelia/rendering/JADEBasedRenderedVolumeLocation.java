package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.janelia.rendering.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JADEBasedRenderedVolumeLocation implements RenderedVolumeLocation {

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

    public JADEBasedRenderedVolumeLocation(String volumeBaseURI, String authToken, String storageServiceApiKey) {
        this.volumeBaseURI = volumeBaseURI;
        this.authToken = authToken;
        this.storageServiceApiKey = storageServiceApiKey;
    }

    @Override
    public URI getBaseURI() {
        return URI.create(volumeBaseURI);
    }

    @Override
    public List<URI> listImageUris(int level) {
        Client httpClient = null;
        try {
            httpClient = createHttpClient();
            WebTarget target = httpClient.target(volumeBaseURI)
                    .path("list")
                    .queryParam("depth", level)
                    ;
            Response response;
            response = createRequestWithCredentials(target.request(MediaType.APPLICATION_JSON)).get();
            int responseStatus = response.getStatus();
            if (responseStatus == Response.Status.OK.getStatusCode()) {
                List<ContentEntry> storageCotent = response.readEntity(new GenericType<List<ContentEntry>>(){});
                return storageCotent.stream().map(ce -> URI.create(ce.nodeAccessURL).resolve(ce.nodeRelativePath)).collect(Collectors.toList()); // !!!! FIXME
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
    public InputStream readTileImage(String tileRelativePath) {
        return openContentStream(tileRelativePath, ImmutableMap.of());
    }

    @Nullable
    @Override
    public byte[] readTileImagePages(String tileRelativePath, int startPage, int nPages) {
        InputStream tileImageStream = openContentStream(tileRelativePath,
                ImmutableMap.<String, String>builder()
                        .put("filterType", "TIFF_IMAGE")
                        .put("z0", String.valueOf(startPage))
                        .put("deltaz", String.valueOf(nPages))
                        .build()
        );
        if (tileImageStream == null) {
            return null;
        } else
        return ImageUtils.loadRenderedImageBytesFromTiffStream(tileImageStream, 0, -1);
    }

    @Nullable
    @Override
    public InputStream readTransformData() {
        return openContentStream(TRANSFORM_FILE_NAME, ImmutableMap.of());
    }

    @Nullable
    @Override
    public InputStream readTileBaseData() {
        return openContentStream(TILED_VOL_BASE_FILE_NAME, ImmutableMap.of());
    }

    private InputStream openContentStream(String contentRelativePath, Map<String, String> queryParams) {
        Client httpClient = null;
        try {
            httpClient = createHttpClient();
            WebTarget target = httpClient.target(volumeBaseURI)
                    .path("entry_content")
                    .path(contentRelativePath)
                    ;
            for (Map.Entry<String, String> qe : queryParams.entrySet()) {
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

    private Client createHttpClient() {
        SSLContext sslContext = createSSLContext();
        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ClientConfig clientConfig = new ClientConfig()
                .register(jacksonProvider);
        return ClientBuilder.newBuilder()
                .withConfig(clientConfig)
                .connectTimeout(5, TimeUnit.SECONDS)
                .sslContext(sslContext)
                .hostnameVerifier((s, sslSession) -> true)
                .build();
    }

    private static SSLContext createSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            TrustManager[] trustManagers = {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
                            // Everyone is trusted
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
                            // Everyone is trusted
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Error initilizing SSL context", e);
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
