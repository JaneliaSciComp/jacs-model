package org.janelia.model.access.domain.dao.mongo;

import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.SyncedPathDao;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.files.N5Container;
import org.janelia.model.domain.files.SyncedPath;
import org.janelia.model.domain.files.SyncedRoot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SyncedPathDaoTest extends AbstractMongoDaoTest {

    private static final String TEST_NAME = "unittester";
    private static final String TEST_OWNER = "user:"+ TEST_NAME;

    private SubjectMongoDao subjectMongoDao;
    private SyncedPathDao syncedPathDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        subjectMongoDao.createUser(TEST_NAME, null, null);
        syncedPathDao = new SyncedPathMongoDao(
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
    public void testGetChildren() {

        SyncedRoot syncedRoot = new SyncedRoot();
        syncedRoot.setFilepath("/test/file/path");
        syncedRoot.setExistsInStorage(true);
        syncedRoot.addSyncClass(N5Container.class);
        syncedPathDao.saveBySubjectKey(syncedRoot, TEST_OWNER);

        N5Container n5 = new N5Container();
        n5.setRootRef(Reference.createFor(syncedRoot));
        n5.setFilepath("/test/file/path/something.n5");
        n5.setExistsInStorage(false);
        syncedPathDao.saveBySubjectKey(n5, TEST_OWNER);

        List<SyncedPath> children = syncedPathDao.getChildren(TEST_OWNER, syncedRoot, 0, 1);
        assertNotNull(children);
        assertEquals(1, children.size());

        SyncedPath savedN5 = children.get(0);
        assertEquals(n5.getId(), savedN5.getId());
        assertEquals(n5.getFilepath(), savedN5.getFilepath());
        assertEquals(n5.isExistsInStorage(), savedN5.isExistsInStorage());

    }


}
