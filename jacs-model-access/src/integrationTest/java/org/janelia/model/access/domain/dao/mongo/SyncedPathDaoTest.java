package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.Sets;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.NDContainerDao;
import org.janelia.model.access.domain.dao.SyncedRootDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.files.DiscoveryAgentType;
import org.janelia.model.domain.files.N5Container;
import org.janelia.model.domain.files.SyncedRoot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SyncedPathDaoTest extends AbstractMongoDaoTest {

    private static final String TEST_NAME = "unittester";
    private static final String TEST_OWNER = "user:"+ TEST_NAME;

    private SubjectMongoDao subjectMongoDao;
    private SyncedRootDao syncedRootDao;
    private NDContainerDao ndContainerDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        subjectMongoDao.createUser(TEST_NAME, null, null);
        syncedRootDao = new SyncedRootMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper)) {
        };
        ndContainerDao = new NDContainerMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper)) {
        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetRoots() {

        SyncedRoot syncedRoot = new SyncedRoot();
        syncedRoot.setFilepath("/test/file/path");
        syncedRoot.setExistsInStorage(true);
        syncedRoot.addDiscoveryAgent(DiscoveryAgentType.n5DiscoveryAgent);
        syncedRootDao.saveBySubjectKey(syncedRoot, TEST_OWNER);

        SyncedRoot syncedRoot2 = new SyncedRoot();
        syncedRoot2.setFilepath("/test/file/path");
        syncedRoot2.setExistsInStorage(true);
        syncedRoot2.addDiscoveryAgent(DiscoveryAgentType.n5DiscoveryAgent);
        syncedRootDao.saveBySubjectKey(syncedRoot2, TEST_OWNER);

        List<SyncedRoot> syncedRoots = syncedRootDao.getSyncedRoots(TEST_OWNER);
        assertEquals(2, syncedRoots.size());
        assertEquals(Sets.newHashSet(syncedRoot, syncedRoot2), Sets.newHashSet(syncedRoots));
    }

    @Test
    public void testGetChildren() {

        SyncedRoot syncedRoot = new SyncedRoot();
        syncedRoot.setFilepath("/test/file/path");
        syncedRoot.setExistsInStorage(true);
        syncedRoot.addDiscoveryAgent(DiscoveryAgentType.n5DiscoveryAgent);
        syncedRootDao.saveBySubjectKey(syncedRoot, TEST_OWNER);

        N5Container n5 = new N5Container();
        n5.setFilepath("/test/file/path/something.n5");
        n5.setExistsInStorage(false);
        ndContainerDao.saveBySubjectKey(n5, TEST_OWNER);

        syncedRootDao.updateChildren(TEST_OWNER, syncedRoot, Collections.singletonList(Reference.createFor(n5)));

        SyncedRoot found = null;
        for(SyncedRoot root : syncedRootDao.getSyncedRoots(TEST_OWNER)) {
            if (root.getId().equals(syncedRoot.getId())) {
                found = root;
                break;
            }
        }

        assertNotNull(found);
        assertEquals(1, found.getChildren().size());

        Reference savedN5Ref = found.getChildren().get(0);
        assertEquals(n5.getId(), savedN5Ref.getTargetId());
    }
}
