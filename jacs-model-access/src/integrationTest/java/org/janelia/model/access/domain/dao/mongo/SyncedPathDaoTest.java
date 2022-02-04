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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SyncedPathDaoTest extends AbstractMongoDaoTest {

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;

    private SubjectMongoDao subjectMongoDao;
    private SyncedPathDao syncedPathDao;

    private List<SyncedPath> testPaths = new ArrayList<>();


    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        syncedPathDao = new SyncedPathMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper)) {
        };
    }

    @After
    public void tearDown() {
        for (SyncedPath test : testPaths) {
            //syncedPathDao.delete(test);
        }
    }

    @Test
    public void testGetChildren() {

        SyncedRoot syncedRoot = new SyncedRoot();
        syncedRoot.setFilepath("/test/file/path");
        syncedRoot.setExistsInStorage(true);
        syncedRoot.addSyncClass(N5Container.class);
        syncedPathDao.save(syncedRoot);
        testPaths.add(syncedRoot);

        N5Container n5 = new N5Container();
        n5.setRootRef(Reference.createFor(syncedRoot));
        n5.setFilepath("/test/file/path/something.n5");
        n5.setExistsInStorage(false);
        syncedPathDao.save(n5);
        testPaths.add(n5);

        List<SyncedPath> children = syncedPathDao.getChildren(syncedRoot, 0, 1);
        SyncedPath savedN5 = children.get(0);

        assertEquals(1, children.size());
        assertEquals(n5.getId(), savedN5.getId());
        assertEquals(n5.getFilepath(), savedN5.getFilepath());
        assertEquals(n5.isExistsInStorage(), savedN5.isExistsInStorage());

    }


}
