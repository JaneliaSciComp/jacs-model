package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.io.CountingInputStream;

/**
 * Proxy for accessing content using http.
 */
public class HttpFileProxy implements FileProxy {

    private final String url;
    private final Function<String, InputStream> httpToContentStreamProvider;
    private CountingInputStream contentStream;

    public HttpFileProxy(String url, Function<String, InputStream> httpToContentStreamProvider) {
        this.url = url;
        this.httpToContentStreamProvider = httpToContentStreamProvider;
        this.contentStream = null;
    }

    public HttpFileProxy(URL url, Function<String, InputStream> httpToContentStreamProvider) {
        this(url.toString(), httpToContentStreamProvider);
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
            contentStream = new CountingInputStream(httpToContentStreamProvider.apply(url));
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
    public boolean deleteProxy() {
        return false;
    }
}
