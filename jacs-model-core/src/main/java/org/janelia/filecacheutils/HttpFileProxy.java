package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import com.google.common.io.CountingInputStream;

/**
 * Proxy for accessing content using http.
 */
public class HttpFileProxy implements FileProxy {

    private final String url;
    private final Function<String, InputStream> httpContentStreamProvider;
    private final Function<String, Boolean> httpCheckContentProvider;
    private CountingInputStream contentStream;

    public HttpFileProxy(String url,
                         Function<String, InputStream> httpContentStreamProvider,
                         Function<String, Boolean> httpCheckContentProvider) {
        this.url = url;
        this.httpContentStreamProvider = httpContentStreamProvider;
        this.httpCheckContentProvider = httpCheckContentProvider;
        this.contentStream = null;
    }

    @Override
    public String getFileId() {
        return url;
    }

    @Override
    public Long estimateSizeInBytes() {
        if (contentStream == null) {
            return 0L;
        } else {
            return contentStream.getCount();
        }
    }

    @Override
    public InputStream openContentStream() throws FileNotFoundException {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            contentStream = new CountingInputStream(httpContentStreamProvider.apply(url));
        } else if (url.startsWith("file://")) {
            contentStream = new CountingInputStream(new FileInputStream(url));
        } else {
            throw new IllegalArgumentException("URL scheme is not supported " + url);
        }
        return contentStream;
    }

    @Override
    public File getLocalFile() {
        return null;
    }

    @Override
    public boolean exists() {
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
