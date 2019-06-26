package org.janelia.cacheutils;

import java.io.InputStream;
import java.util.Map;

public interface RemoteFileRetriever {
    InputStream retrieveRemoteFile(String remoteFileName, Map<String, String> remoteFileParams);
}
