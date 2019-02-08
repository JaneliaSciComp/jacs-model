package org.janelia.rendering.utils;

import javax.ws.rs.client.Client;

public interface HttpClientProvider {
    Client getClient();
}
