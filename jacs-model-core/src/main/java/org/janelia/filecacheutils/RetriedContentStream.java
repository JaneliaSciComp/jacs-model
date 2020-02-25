package org.janelia.filecacheutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetriedContentStream extends ContentStream {
    private static final Logger LOG = LoggerFactory.getLogger(RetriedContentStream.class);
    private static final int DEFAULT_RETRIES = 1;

    private final ContentStream baseContentStream;
    private final int maxRetries;
    private long baseContentOffset;

    public RetriedContentStream(StreamSupplier inputStreamSupplier, int maxRetries) {
        this(inputStreamSupplier, null, maxRetries);
    }

    public RetriedContentStream(StreamSupplier inputStreamSupplier, Consumer<Void> closeHandler, int maxRetries) {
        this.baseContentStream = new ContentStream(inputStreamSupplier, closeHandler);
        this.maxRetries = maxRetries > 0 ? maxRetries : DEFAULT_RETRIES;
        this.baseContentOffset = 0L;
    }

    @Override
    long getStreamPos() {
        return baseContentStream.getStreamPos();
    }

    @Override
    public void close() {
        baseContentStream.close();
    }

    @Override
    protected int readBytes(byte[] buf, int off, int len) throws IOException {
        int nRetries = 0;
        for (;;) {
            try {
                if (nRetries > 0) {
                    skipBytesAlreadyRead();
                }
                int n = baseContentStream.readBytes(buf, off, len);
                if (n == -1) {
                    return n;
                } else if (n > 0 && baseContentOffset < baseContentStream.getStreamPos()) {
                    baseContentOffset = baseContentStream.getStreamPos();
                    return n;
                } else {
                    return 0; // behave as if nothing was read because this was already read in a previous trial
                }
            } catch (FileNotFoundException e) {
                // if the content does not exist no reason to retry
                throw e;
            } catch (Exception e) {
                nRetries++;
                if (nRetries < maxRetries) {
                    LOG.warn("Failed to copy content stream to destination after {} tries - will retry {} more times", nRetries, maxRetries - nRetries, e);
                    baseContentStream.close();
                } else {
                    LOG.warn("Failed to copy content stream to destination after {} tries", nRetries, e);
                    throw e;
                }
            }
        }
    }

    private void skipBytesAlreadyRead() throws IOException {
        final int BUFFER_SIZE = 16 * 1024; // 16K
        byte[] tmpBuffer = new byte[BUFFER_SIZE];
        long currentOffset = 0;
        while (currentOffset < baseContentOffset) {
            long remaining = baseContentOffset - currentOffset;
            int toRead;
            if (remaining < tmpBuffer.length) {
                toRead = (int) remaining;
            } else {
                toRead = tmpBuffer.length;
            }
            int n = baseContentStream.readBytes(tmpBuffer, 0, toRead);
            if (n < 0) {
                return;
            } else {
                currentOffset += n;
            }
        }
    }
}
