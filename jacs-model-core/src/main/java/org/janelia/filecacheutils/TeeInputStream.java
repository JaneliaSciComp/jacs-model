package org.janelia.filecacheutils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TeeInputStream extends ContentStream {
    private static final Logger LOG = LoggerFactory.getLogger(TeeInputStream.class);

    private final ContentStream baseContentStream;
    private final WritableByteChannel outputChannel;
    private final Consumer<Void> onCloseHandler;
    private final ExecutorService outputExecutor;
    private CompletionStage<Void> writeOp;
    // InputStream implementation sometime uses read() to implement read(buf)
    // so we use writingInProgress flag to prevent duplicated bytes written to the output
    // however this is not thread safe
    private volatile boolean writingInProgress;

    TeeInputStream(ContentStream in, OutputStream os, Consumer<Void> onCloseHandler, ExecutorService outputExecutor) {
        this.baseContentStream = in;
        this.outputChannel = Channels.newChannel(os);
        this.onCloseHandler = onCloseHandler;
        this.outputExecutor = outputExecutor;
        writeOp = CompletableFuture.completedFuture(null);
        writingInProgress = false;
    }

    @Override
    long getStreamPos() {
        return baseContentStream.getStreamPos();
    }

    @Override
    protected int readBytes(byte[] buf, int off, int len) throws IOException {
        int n;
        try {
            n = baseContentStream.readBytes(buf, off, len);
            if (n > 0) {
                writeBuffer(buf, off, n);
            }
            executeAfterRead(n);
            return n;
        } catch (IOException e) {
            handleIOException(e);
            throw e;
        }
    }

    private void executeAfterRead(int n) {
        if (outputExecutor != null) {
            writeOp = writeOp.thenAcceptAsync((Void) -> afterRead(n), outputExecutor);
        } else {
            writeOp = writeOp.thenAccept((Void) -> afterRead(n));
        }
    }

    protected void handleIOException(IOException e) {
    }

    protected void afterRead(int n) {
    }

    /**
     * Write the given buffer to the output.
     * @param buf
     * @param offset start write position
     * @param length how many bytes to write
     */
    private void writeBuffer(byte[] buf, int offset, int length) {
        if (writingInProgress) {
            return;
        }
        writingInProgress = true;
        // this seems costly especially if the buffer is large but because
        // write buffer is an asynchronous operation, simply wrapping it may not be enough
        // so we actually copy the bytes to a new allocated buffer
        ByteBuffer readyToReadFromBuffer = ByteBuffer.allocate(length);
        readyToReadFromBuffer.put(buf, offset, length);
        readyToReadFromBuffer.flip();
        executeWrite(() -> {
            try {
                while(readyToReadFromBuffer.hasRemaining()){
                    outputChannel.write(readyToReadFromBuffer);
                }
                readyToReadFromBuffer.clear();
            } catch (IOException e) {
                LOG.error("Error writing {} bytes to output", length, e);
                handleIOException(e);
            }
        });
        writingInProgress = false;
    }

    private void executeWrite(Runnable r) {
        if (outputExecutor != null) {
            writeOp = writeOp.thenRunAsync(r, outputExecutor);
        } else {
            writeOp = writeOp.thenRun(r);
        }
    }

    @Override
    public void close() {
        baseContentStream.close();
        if (onCloseHandler != null) {
            if (outputExecutor != null) {
                writeOp.thenAcceptAsync(onCloseHandler, outputExecutor);
            } else {
                writeOp.thenAccept(onCloseHandler);
            }
        }
    }

}
