package org.janelia.model.domain;

import org.janelia.model.domain.files.DiscoveryAgentType;
import org.janelia.model.domain.files.N5Container;
import org.janelia.model.domain.files.SyncedRoot;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SyncedPathTests {

    @Test
    public void testSynchedRoot() {

        SyncedRoot syncedRoot = new SyncedRoot();
        syncedRoot.setFilepath("/test/file/path");
        syncedRoot.setExistsInStorage(true);
        syncedRoot.addDiscoveryAgent(DiscoveryAgentType.zarrDiscoveryAgent.name());

        assertEquals("/test/file/path", syncedRoot.getFilepath());
        assertTrue(syncedRoot.isExistsInStorage());
        assertEquals(Collections.singletonList(DiscoveryAgentType.zarrDiscoveryAgent.name()), syncedRoot.getDiscoveryAgents());

    }

    @Test
    public void testN5Container() {

        SyncedRoot syncedRoot = new SyncedRoot();
        syncedRoot.setFilepath("/test/file/path");
        syncedRoot.setExistsInStorage(true);
        syncedRoot.setId(1L);

        N5Container n5 = new N5Container();
        n5.setRootRef(Reference.createFor(syncedRoot));
        n5.setFilepath("/test/file/path/something.n5");
        n5.setExistsInStorage(true);

        assertEquals("/test/file/path/something.n5", n5.getFilepath());
        assertTrue(n5.isExistsInStorage());
        assertEquals(syncedRoot.getId(), n5.getRootRef().getTargetId());
    }
}
