package org.janelia.model.access.domain.dao.mongo;

import java.util.List;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ITestDomainDAOManager;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.security.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class TmSampleDAOTest extends AbstractMongoDaoTest {

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;
    private static final DomainDAO dao = ITestDomainDAOManager.getInstance().getDao();

    private SubjectMongoDao subjectMongoDao;
    private WorkspaceNodeMongoDao workspaceNodeMongoDao;
    private TmSampleMongoDao tmSampleMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
        tmSampleMongoDao = new TmSampleMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper),
                dao);
    }

    private TmSample createTestData() throws Exception {
        User u = subjectMongoDao.createUser(testName, "Tester", null);
        workspaceNodeMongoDao.createDefaultWorkspace(u.getKey());
        TmSample tmSample = new TmSample();
        tmSample.setOwnerKey(u.getKey());
        tmSample.setName("testName");
        tmSample.setFilepath("testfilepath");
        return tmSampleMongoDao.createTmSample(u.getKey(), tmSample);
    }

    private void removeTestData(TmSample tmSample) {
        if (tmSample != null) {
            tmSampleMongoDao.delete(tmSample);
        }
        subjectMongoDao.removeSubjectByKey(testUser);
    }

    @Test
    public void updateProperty() throws Exception {
        TmSample tmSample = null;
        try {
            tmSample = createTestData();
            dao.updateProperty(tmSample.getOwnerKey(), tmSample.getClass(), tmSample.getId(),
                    "existsInStorage", false);
        } finally {
            removeTestData(tmSample);
        }
    }

}
