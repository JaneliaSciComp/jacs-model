package org.janelia.filecacheutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ContentStream extends AutoCloseable {
    @FunctionalInterface
    interface StreamSupplier {
        InputStream open() throws FileNotFoundException;
    }

    @Override
    void close();

    default long copyTo(OutputStream dst) throws IOException {
        final int BUFFER_SIZE = 16 * 1024; // 16K

        byte[] buffer = new byte[BUFFER_SIZE];
        long nread = 0L;
        for (; ;) {
            int n = readBytes(buffer, 0, buffer.length);
            if (n == -1) {
                return nread == 0L ? -1L : nread;
            } else if (n > 0) {
                nread += n;
                // write to the destination
                dst.write(buffer, 0, n);
            }
        }
    }

    int readBytes(byte[] buf, int off, int len) throws IOException;

    default InputStream asInputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return readBytes(new byte[1], 0, 1);
            }

            @Override
            public int read(byte[] buf, int off, int len) throws IOException {
                return readBytes(buf, off, len);
            }
        };
    }
}
