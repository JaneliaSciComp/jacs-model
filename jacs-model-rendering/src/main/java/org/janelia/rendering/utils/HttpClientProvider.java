package org.janelia.rendering.utils;

import javax.ws.rs.client.Client;

public interface HttpClientProvider {
    Client getClient();

    default Client getCloseableClient() {
        return new CloseableClientDelegate(getClient());
    }

    default Client getNonCloseableClientDelegate() {
        return new NonCloseableClientDelegate(getClient());
    }
}
