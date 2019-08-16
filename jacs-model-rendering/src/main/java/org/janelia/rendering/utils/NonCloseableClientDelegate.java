package org.janelia.rendering.utils;

import javax.ws.rs.client.Client;

public class NonCloseableClientDelegate extends AbstractClientDelegate {

    public NonCloseableClientDelegate(Client delegate) {
        super(delegate);
    }

    @Override
    public void close() {
    }
}
