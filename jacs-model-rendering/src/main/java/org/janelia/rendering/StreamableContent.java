package org.janelia.rendering;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class StreamableContent implements AutoCloseable, Closeable {

    private final long size;
    private final InputStream stream;

    public StreamableContent(long size, InputStream stream) {
        this.size = size;
        this.stream = stream;
    }

    public long getSize() {
        return size;
    }

    public InputStream getStream() {
        return stream;
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
}
