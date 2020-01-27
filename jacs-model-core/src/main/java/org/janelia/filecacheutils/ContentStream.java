package org.janelia.filecacheutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(ContentStream.class);

    @FunctionalInterface
    public interface StreamSupplier {
        InputStream open() throws FileNotFoundException;
    }

    private final StreamSupplier inputStreamSupplier;
    private final Consumer<Void> closeHandler;
    private InputStream currentInputStream;

    protected ContentStream() {
        this(() -> null);
    }

    public ContentStream(StreamSupplier inputStreamSupplier) {
        this(inputStreamSupplier, null);
    }

    public ContentStream(StreamSupplier inputStreamSupplier, Consumer<Void> closeHandler) {
        this.inputStreamSupplier = inputStreamSupplier;
        this.closeHandler = closeHandler;
        this.currentInputStream = null;
    }

    @Override
    public int read() throws IOException {
        return readBytes(new byte[1], 0, 1);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return readBytes(buf, off, len);
    }

    protected int readBytes(byte[] buf, int off, int len) throws IOException {
        int n;
        try {
            if (currentInputStream == null && !open()) {
                return -1;
            }
            n = currentInputStream.read(buf, off, len);
            if (n == -1) {
                close();
            }
            return n;
        } catch (IOException e) {
            handleIOException(e);
            throw e;
        }
    }

    private boolean open() throws FileNotFoundException {
        // close current stream
        close();
        // then reopen it
        this.currentInputStream = inputStreamSupplier.open();
        return this.currentInputStream != null;
    }

    private void handleIOException(IOException e) {
        close();
    }

    @Override
    public void close() {
        if (currentInputStream != null) {
            try {
                currentInputStream.close();
            } catch (IOException e) {
                LOG.debug("Close stream exception", e);
            } finally {
                currentInputStream = null;
            }
        }
        if (closeHandler != null) {
            closeHandler.accept(null);
        }
    }

}