package org.janelia.filecacheutils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeeInputStream extends FilterInputStream {
    private static final Logger LOG = LoggerFactory.getLogger(TeeInputStream.class);

    private final WritableByteChannel outputChannel;
    private final ExecutorService outputExecutor;

    TeeInputStream(InputStream in, OutputStream os, ExecutorService outputExecutor) {
        super(in);
        this.outputChannel = Channels.newChannel(os);
        this.outputExecutor = outputExecutor;
    }

    @Override
    public int read() throws IOException {
        int b = this.in.read();
        if (b != -1) {
            writeBuffer(new byte[] {(byte) b}, 0, 1);
        }
        afterRead(b);
        return b;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        int n = super.read(buf);
        if (n > 0) {
            writeBuffer(buf, 0, n);
        }
        afterRead(n);
        return n;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int n = super.read(buf, off, len);
        if (n > 0) {
            writeBuffer(buf, off, n);
        }
        afterRead(n);
        return n;
    }

    protected void afterRead(int n) throws IOException {
    }

    /**
     * Write the given buffer to the output.
     * @param buf
     * @param offset start write position
     * @param length how many bytes to write
     */
    private void writeBuffer(byte[] buf, int offset, int length) {
        // this seems costly especially if the buffer is large but because
        // write buffer is an asynchronous operation, simply wrapping it may not be enough
        // so we actually copy the bytes to a new allocated buffer
        ByteBuffer readyToReadFromBuffer = ByteBuffer.allocate(length);
        readyToReadFromBuffer.put(buf, offset, length);
        readyToReadFromBuffer.flip();
        performOutput(() -> {
            try {
                while(readyToReadFromBuffer.hasRemaining()){
                    outputChannel.write(readyToReadFromBuffer);
                }
                readyToReadFromBuffer.clear();
            } catch (IOException e) {
                LOG.error("Error writing {} bytes to output", length, e);
            }
        });
    }

    private void performOutput(Runnable r) {
        if (outputExecutor != null) {
            outputExecutor.submit(r);
        } else {
            r.run();
        }
    }
}
