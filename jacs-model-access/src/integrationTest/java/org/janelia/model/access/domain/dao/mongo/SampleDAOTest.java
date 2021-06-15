package org.janelia.model.access.domain.dao.mongo;

import java.util.List;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.ITestDomainDAOManager;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.security.User;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleDAOTest extends AbstractMongoDaoTest {

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;
    private static final String testDataSetIdentifier = testUser+"_screen";
    private static final DomainDAO dao = ITestDomainDAOManager.getInstance().getDao();

    private SubjectMongoDao subjectMongoDao;
    private WorkspaceNodeMongoDao workspaceNodeMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
    }

    private DataSet createTestData() throws Exception {
        User u = subjectMongoDao.createUser(testName, "Tester", null);
        workspaceNodeMongoDao.createDefaultWorkspace(u.getKey());
        DataSet dataSet = new DataSet();
        dataSet.setName("Screen");
        dataSet.setIdentifier(testDataSetIdentifier);
        return dao.createDataSet(testUser, dataSet);
    }

    private void removeTestData() throws Exception {
        DataSet dataSet = dao.getDataSetByIdentifier(testUser,testDataSetIdentifier);
        if (dataSet != null) {
            dao.remove(dataSet.getOwnerKey(), dataSet);
        }
        subjectMongoDao.removeSubjectByKey(testUser);
    }

    @Test
    public void testGetDataSetById() throws Exception {
        try {
            createTestData();
            DataSet dataSet = dao.getDataSetByIdentifier(testUser, testDataSetIdentifier);
            assertEquals(testDataSetIdentifier, dataSet.getIdentifier());
        } finally {
            removeTestData();
        }
    }

    @Test
    public void testGetUserDataSets() throws Exception {
        try {
            DataSet dataSet = createTestData();
            List<DataSet> dataSets = dao.getUserDataSets(testUser);
            assertTrue("Test user has no data sets", dataSets.size() > 0);
            for (DataSet dataSet2 : dataSets) {
                assertEquals(dataSet.getId(), dataSet2.getId());
                assertEquals(testUser, dataSet2.getOwnerKey());
            }
        } finally {
            removeTestData();
        }
    }

    @Test
    public void testGetDataSetByIdentifier() throws Exception {
        try {
            DataSet dataSet = createTestData();
            for (DataSet dataSet2 : dao.getUserDataSets(testUser)) {
                assertEquals(dataSet.getId(), dataSet2.getId());
                assertEquals(testUser, dataSet2.getOwnerKey());
                assertEquals(testDataSetIdentifier, dataSet2.getIdentifier());
            }
        } finally {
            removeTestData();
        }
    }

}
