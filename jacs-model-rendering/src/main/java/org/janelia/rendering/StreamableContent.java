package org.janelia.rendering;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamableContent implements AutoCloseable, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(StreamableContent.class);

    private final long size;
    private final InputStream stream;

    public StreamableContent(long size, InputStream stream) {
        this.size = size;
        this.stream = stream;
    }

    public long getSize() {
        return size;
    }

    @Nonnull
    public InputStream getStream() {
        return stream;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
