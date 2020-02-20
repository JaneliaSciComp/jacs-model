package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Proxy for accessing content using http.
 */
public class HttpFileProxy implements FileProxy {

    private final String url;
    private final Function<String, InputStream> httpContentStreamProvider;
    private final Function<String, Boolean> httpCheckContentProvider;

    public HttpFileProxy(String url,
                         Function<String, InputStream> httpContentStreamProvider,
                         Function<String, Boolean> httpCheckContentProvider) {
        this.url = url;
        this.httpContentStreamProvider = httpContentStreamProvider;
        this.httpCheckContentProvider = httpCheckContentProvider;
    }

    @Override
    public String getFileId() {
        return url;
    }

    @Override
    public Long estimateSizeInBytes(boolean alwaysCheck) {
        return 0L; // don't know how to estimate the size
    }

    @Override
    public ContentStream openContentStream(boolean alwaysDownload) throws FileNotFoundException {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return new RetriedContentStream(() -> {
                try {
                    return httpContentStreamProvider.apply(url);
                } catch (Exception e) {
                    throw new IllegalStateException("Error opening " + url, e);
                }
            }, 2);
        } else if (url.startsWith("file://")) {
            return new ContentStream(() -> {
                try {
                    return new FileInputStream(url);
                } catch (IOException e) {
                    throw new IllegalStateException("Error opening " + url, e);
                }
            });
        } else {
            throw new IllegalArgumentException("URL scheme is not supported " + url);
        }
    }

    @Override
    public File getLocalFile(boolean alwaysDownload) {
        if (url.startsWith("file://")) {
            return new File(URI.create(url));
        } else {
            return null;
        }
    }

    @Override
    public boolean exists(boolean alwaysCheck) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return httpCheckContentProvider.apply(url);
        } else if (url.startsWith("file://")) {
            return Files.exists(Paths.get(url));
        } else {
            throw new IllegalArgumentException("URL scheme is not supported " + url);
        }
    }

    @Override
    public boolean deleteProxy() {
        return false;
    }
}
