package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.io.CountingInputStream;

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
    public Optional<Long> estimateSizeInBytes() {
        if (contentStream == null) {
            return Optional.empty();
        } else {
            return Optional.of(contentStream.getCount());
        }
    }

    @Override
    public InputStream getContentStream() {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            contentStream = new CountingInputStream(httpToContentStreamProvider.apply(url));
        } else if (url.startsWith("file://")) {
            try {
                contentStream = new CountingInputStream(new FileInputStream(url));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
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
