package org.janelia.rendering.utils;

import javax.ws.rs.client.Client;

public class CloseableClientDelegate extends AbstractClientDelegate {

    public CloseableClientDelegate(Client delegate) {
        super(delegate);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
