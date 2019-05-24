package org.janelia.model.access.domain.dao;

import java.util.List;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.domain.sample.DataSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleDAOTest {

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;
    private static final String testDataSetIdentifier = testUser+"_screen";
    private static final DomainDAO dao = DomainDAOManager.getInstance().getDao();

    @BeforeClass
    public static void beforeClass() throws Exception {
        cleanup();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        cleanup();
    }

    /**
     * Clean up any test data that was created but failed to be deleted.
     * @throws Exception
     */
    private static void cleanup() throws Exception {
        DomainDAOManager.getInstance().dropTestDatabase();
    }

    private DataSet createTestData() throws Exception {
        dao.createUser(testName, "Tester", null);
        DataSet dataSet = new DataSet();
        dataSet.setName("Screen");
        dataSet.setIdentifier(testDataSetIdentifier);
        return dao.createDataSet(testUser, dataSet);
    }

    private void removeTestData() throws Exception {
        DataSet dataSet = dao.getDataSetByIdentifier(testUser,testDataSetIdentifier);
        if (dataSet!=null) {
            dao.remove(dataSet.getOwnerKey(), dataSet);
        }
        dao.removeUser(testUser);
    }

    @Test
    public void testGetDataSetById() throws Exception {
        try {
            createTestData();
            DataSet dataSet = dao.getDataSetByIdentifier(testUser, testDataSetIdentifier);
            assertEquals(testDataSetIdentifier, dataSet.getIdentifier());
        }
        finally {
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
        }
        finally {
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
        }
        finally {
            removeTestData();
        }
    }

//    @Test
//    public void testGetActiveSamplesForDataSet() throws Exception {
//        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
//            List<Sample> activeSamples = dao.getActiveSamplesForDataSet(testUser, dataSet.getIdentifier());
//            for(Sample sample : activeSamples) {
//                assertTrue(sample.getSageSynced());
//            }
//            break; // testing one data set is enough
//        }
//    }
//
//    @Test
//    public void testGetSamplesForDataSet() throws Exception {
//        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
//            List<Sample> activeSamples = dao.getActiveSamplesForDataSet(testUser, dataSet.getIdentifier());
//            List<Sample> allSamples = dao.getSamplesForDataSet(testUser, dataSet.getIdentifier());
//            assertTrue(activeSamples.size() <= allSamples.size());
//        }
//    }
//
//    @Test
//    public void testGetActiveLsmsForDataSet() throws Exception {
//        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
//            List<LSMImage> activeLsms = dao.getActiveLsmsForDataSet(testUser, dataSet.getIdentifier());
//            for(LSMImage lsm : activeLsms) {
//                assertTrue(lsm.getSageSynced());
//            }
//            break; // testing one data set is enough
//        }
//    }
//
//    @Test
//    public void testGetLsmsForDataSet() throws Exception {
//        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
//            List<LSMImage> activeLsms = dao.getActiveLsmsForDataSet(testUser, dataSet.getIdentifier());
//            List<LSMImage> allLsms = dao.getLsmsForDataSet(testUser, dataSet.getIdentifier());
//            assertTrue(activeLsms.size() <= allLsms.size());
//        }
//    }
//
//    @Test
//    public void testGetSamplesBySlideCode() throws Exception {
//        boolean checked = false;
//        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
//            List<Sample> activeSamples = dao.getActiveSamplesForDataSet(testUser, dataSet.getIdentifier());
//            for(Sample sample : activeSamples) {
//                List<Sample> matchingSamples = dao.getSamplesBySlideCode(testUser, sample.getDataSet(), sample.getSlideCode());
//                assertFalse(matchingSamples.isEmpty());
//                Sample matchingActiveSample = dao.getActiveSampleBySlideCode(testUser, sample.getDataSet(), sample.getSlideCode());
//                assertEquals(sample.getId(), matchingActiveSample.getId());
//                checked = true;
//            }
//        }
//        assertTrue("Test user has no data set with samples", checked);
//    }


}
