package org.janelia.filecacheutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;

import javax.annotation.Nullable;

public class HttpFileProxy implements FileProxy {

    private final String url;

    private final Function<String, InputStream> httpToContentStreamProvider;

    public HttpFileProxy(String url, Function<String, InputStream> httpToContentStreamProvider) {
        this.url = url;
        this.httpToContentStreamProvider = httpToContentStreamProvider;
    }

    public HttpFileProxy(URL url, Function<String, InputStream> httpToContentStreamProvider) {
        this(url.toString(), httpToContentStreamProvider);
    }

    @Override
    public String getFileId() {
        return url;
    }

    @Nullable
    @Override
    public Long getSizeInBytes() {
        return null;
    }

    @Override
    public InputStream getContentStream() {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return httpToContentStreamProvider.apply(url);
        } else if (url.startsWith("file://")) {
            try {
                return new FileInputStream(url);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalArgumentException("URL scheme is not supported " + url);
        }
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
