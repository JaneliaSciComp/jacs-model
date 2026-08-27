package org.janelia.model.access.domain.dao.mongo;

import java.util.Arrays;
import java.util.List;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.SampleQuery;
import org.janelia.model.access.domain.dao.ITestDomainDAOManager;
import org.janelia.model.access.domain.dao.SampleDao;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.security.User;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    private SampleMongoDao sampleMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
        sampleMongoDao = new SampleMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper)
        );
    }

    private User createTestUser() throws Exception {
        User u = subjectMongoDao.createUser(testName, "Tester", null);
        workspaceNodeMongoDao.createDefaultWorkspace(u.getKey());
        return u;
    }

    private DataSet createTestDataset(User u, String dsId, String name) throws Exception {
        // create a test dataset
        DataSet dataSet = new DataSet();
        dataSet.setIdentifier(dsId);
        dataSet.setName(name);
        return dao.createDataSet(u.getKey(), dataSet);
    }

    private Sample createTestSample(User u, DataSet ds) throws Exception {
        // create a test Sample
        Sample testSample = new Sample();
        testSample.setName("testSampleName");
        testSample.setSlideCode("testSlideCode");
        testSample.setDataSet(ds.getIdentifier());
        testSample.setCrossBarcode(1234);
        testSample.setOwnerKey(u.getKey());
        return sampleMongoDao.saveBySubjectKey(testSample, u.getKey());
    }

    private void removeTestData() throws Exception {
        DataSet dataSet = dao.getDataSetByIdentifier(testUser,testDataSetIdentifier);
        List<Sample> datasetSamples = dao.getSamplesByDataSet(testUser, testDataSetIdentifier);
        if (datasetSamples != null) {
            for (Sample sample : datasetSamples) {
                dao.remove(sample.getOwnerKey(), sample);
            }
        }
        if (dataSet != null) {
            dao.remove(dataSet.getOwnerKey(), dataSet);
        }
        subjectMongoDao.removeSubjectByKey(testUser);
    }

    @Test
    public void testGetDataSetById() throws Exception {
        try {
            User persistedTestUser = createTestUser();
            DataSet testDataset = createTestDataset(persistedTestUser, testDataSetIdentifier, "Screen");
            assertNotNull(testDataset.getId());
            DataSet dataSet = dao.getDataSetByIdentifier(testUser, testDataSetIdentifier);
            assertEquals(testDataSetIdentifier, dataSet.getIdentifier());
        } finally {
            removeTestData();
        }
    }

    @Test
    public void testGetUserDataSets() throws Exception {
        try {
            User persistedTestUser = createTestUser();
            DataSet testDataset = createTestDataset(persistedTestUser, testDataSetIdentifier, "Screen");
            List<DataSet> dataSets = dao.getUserDataSets(testUser);
            assertTrue("Test user has no data sets", !dataSets.isEmpty());
            for (DataSet dataSet2 : dataSets) {
                assertEquals(testDataset.getId(), dataSet2.getId());
                assertEquals(testUser, dataSet2.getOwnerKey());
            }
        } finally {
            removeTestData();
        }
    }

    @Test
    public void testGetDataSetByIdentifier() throws Exception {
        try {
            User persistedTestUser = createTestUser();
            DataSet testDataset = createTestDataset(persistedTestUser, testDataSetIdentifier, "Screen");
            for (DataSet dataSet2 : dao.getUserDataSets(testUser)) {
                assertEquals(testDataset.getId(), dataSet2.getId());
                assertEquals(testUser, dataSet2.getOwnerKey());
                assertEquals(testDataSetIdentifier, dataSet2.getIdentifier());
            }
        } finally {
            removeTestData();
        }
    }

    @Test
    public void testQuerySamples() throws Exception {
        try {
            User persistedTestUser = createTestUser();
            DataSet testDataset = createTestDataset(persistedTestUser, testDataSetIdentifier, "Screen");
            Sample testSample = createTestSample(persistedTestUser, testDataset);
            List<Sample> samplesByCrossBarcodes = sampleMongoDao.findMatchingSamples(
                    new SampleQuery().addSampleCrossBarcodes(Arrays.asList(1234))
            );
            assertFalse(samplesByCrossBarcodes.isEmpty());
            assertEquals(testSample.getCrossBarcode(), samplesByCrossBarcodes.get(0).getCrossBarcode());
        } finally {
            removeTestData();
        }
    }

}
