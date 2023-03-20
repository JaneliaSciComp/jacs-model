package org.janelia.model.access.domain.dao.mongo;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ITestDomainDAOManager;
import org.janelia.model.access.domain.dao.TmMappedNeuronDao;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.access.domain.dao.TmWorkspaceDao;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.security.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class TmNeuronMetadataDAOTest extends AbstractMongoDaoTest {

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;
    private static final DomainDAO dao = ITestDomainDAOManager.getInstance().getDao();

    private SubjectMongoDao subjectMongoDao;
    private WorkspaceNodeMongoDao workspaceNodeMongoDao;
    private TmWorkspaceDao tmWorkspaceDao;
    private TmNeuronMetadataDao tmNeuronMetadataDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        DomainPermissionsMongoHelper permissionsMongoHelper = new DomainPermissionsMongoHelper(subjectMongoDao);
        DomainUpdateMongoHelper mongoHelper = new DomainUpdateMongoHelper(testObjectMapper);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                mongoHelper);
        tmNeuronMetadataDao = new TmNeuronMetadataMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                mongoHelper);
        TmMappedNeuronDao tmMappedNeuronDao = null;
        tmWorkspaceDao = new TmWorkspaceMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                mongoHelper,
                dao,
                tmNeuronMetadataDao,
                tmMappedNeuronDao
        );
    }

    @Test
    public void findCappedCollection() {
        TmWorkspace tmWorkspace = null;
        TmNeuronMetadata tmNeuronMetadata = null;
        try {
            tmWorkspace = createTestWorkspace();
            tmNeuronMetadata = createTestNeuron(tmWorkspace);
            AbstractMongoDao abstractDao = (AbstractMongoDao) tmWorkspaceDao;
            String collectionName = MongoDaoHelper.findOrCreateCappedCollection(abstractDao, testMongoDatabase, "tmNeuron",
                    20000000000L, TmNeuronMetadata.class);
            assertTrue(collectionName.startsWith("tmNeuron"));
        } finally {
            removeTestData(tmWorkspace, tmNeuronMetadata);
        }
    }

    private TmWorkspace createTestWorkspace() {
        User u = subjectMongoDao.createUser(testName, "Tester", null);
        workspaceNodeMongoDao.createDefaultWorkspace(u.getKey());
        TmWorkspace tmWorkspace = new TmWorkspace();
        tmWorkspace.setOwnerKey(u.getKey());
        tmWorkspace.setName("testWorkspace");
        return tmWorkspaceDao.createTmWorkspace(u.getKey(), tmWorkspace);
    }

    private TmNeuronMetadata createTestNeuron(TmWorkspace tmWorkspace) {
        TmNeuronMetadata tmNeuronMetadata = new TmNeuronMetadata();
        tmNeuronMetadata.setName("testNeuron");
        return tmNeuronMetadataDao.createTmNeuronInWorkspace(tmWorkspace.getOwnerKey(), tmNeuronMetadata, tmWorkspace);
    }

    private void removeTestData(TmWorkspace tmWorkspace, TmNeuronMetadata tmNeuronMetadata) {
        if (tmWorkspace != null) {
            tmWorkspaceDao.delete(tmWorkspace);
        }
        if (tmNeuronMetadata != null) {
            tmNeuronMetadataDao.delete(tmNeuronMetadata);
        }
        subjectMongoDao.removeSubjectByKey(testUser);
    }

}
