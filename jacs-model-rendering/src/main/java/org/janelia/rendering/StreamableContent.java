package org.janelia.rendering;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.google.common.io.ByteStreams;

public class StreamableContent implements AutoCloseable, Closeable {

    public static StreamableContent empty() {
        return new StreamableContent(0L, null);
    }

    public static StreamableContent of(long size, InputStream stream) {
        return new StreamableContent(size, stream);
    }

    private final long size;
    private final InputStream stream;

    StreamableContent(long size, InputStream stream) {
        this.size = size;
        this.stream = stream;
    }

    public long getSize() {
        return size;
    }

    public InputStream getStream() {
        return stream;
    }

    public byte[] getBytes() {
        if (stream != null) {
            try {
                return ByteStreams.toByteArray(stream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
}
