package org.janelia.filecacheutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceContentStream implements ContentStream {
    private static final Logger LOG = LoggerFactory.getLogger(SourceContentStream.class);

    private final StreamSupplier inputStreamSupplier;
    private final Consumer<Void> closeHandler;
    private InputStream currentInputStream;

    public SourceContentStream(StreamSupplier inputStreamSupplier) {
        this(inputStreamSupplier, null);
    }

    public SourceContentStream(StreamSupplier inputStreamSupplier, Consumer<Void> closeHandler) {
        this.inputStreamSupplier = inputStreamSupplier;
        this.closeHandler = closeHandler;
        this.currentInputStream = null;
    }

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

    private boolean open() throws FileNotFoundException {
        // close current stream
        close();
        // then reopen it
        this.currentInputStream = inputStreamSupplier.open();
        return this.currentInputStream != null;
    }

    public int readBytes(byte[] buf, int off, int len) throws IOException {
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

    private void handleIOException(IOException e) {
        close();
    }

}
