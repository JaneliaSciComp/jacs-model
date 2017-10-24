package org.janelia.model.domain;

import java.util.List;

import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.LSMImage;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.access.domain.DomainDAO;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleDAOTest {

    private static final String testUser = "user:dolanm";
    private static final DomainDAO dao = DomainDAOManager.getInstance().getDao();

    @Test
    public void testGetUserDataSets() throws Exception {
        List<DataSet> dataSets = dao.getUserDataSets(testUser);
        assertTrue("Test user has no data sets", dataSets.size()>1);
        for(DataSet dataSet : dataSets) {
            assertEquals(testUser, dataSet.getOwnerKey());
        }
    }

    @Test
    public void testGetDataSetByIdentifier() throws Exception {
        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
            DataSet ds = dao.getDataSetByIdentifier(dataSet.getOwnerKey(), dataSet.getIdentifier());
            assertEquals(dataSet.getId(), ds.getId());
        }
    }

    @Test
    public void testGetActiveSamplesForDataSet() throws Exception {
        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
            List<Sample> activeSamples = dao.getActiveSamplesForDataSet(testUser, dataSet.getIdentifier());
            for(Sample sample : activeSamples) {
                assertTrue(sample.getSageSynced());
            }
            break; // testing one data set is enough
        }
    }

    @Test
    public void testGetSamplesForDataSet() throws Exception {
        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
            List<Sample> activeSamples = dao.getActiveSamplesForDataSet(testUser, dataSet.getIdentifier());
            List<Sample> allSamples = dao.getSamplesForDataSet(testUser, dataSet.getIdentifier());
            assertTrue(activeSamples.size() <= allSamples.size());
        }
    }

    @Test
    public void testGetActiveLsmsForDataSet() throws Exception {
        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
            List<LSMImage> activeLsms = dao.getActiveLsmsForDataSet(testUser, dataSet.getIdentifier());
            for(LSMImage lsm : activeLsms) {
                assertTrue(lsm.getSageSynced());
            }
            break; // testing one data set is enough
        }
    }

    @Test
    public void testGetLsmsForDataSet() throws Exception {
        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
            List<LSMImage> activeLsms = dao.getActiveLsmsForDataSet(testUser, dataSet.getIdentifier());
            List<LSMImage> allLsms = dao.getLsmsForDataSet(testUser, dataSet.getIdentifier());
            assertTrue(activeLsms.size() <= allLsms.size());
        }
    }

    @Test
    public void testGetSamplesBySlideCode() throws Exception {
        boolean checked = false;
        for(DataSet dataSet : dao.getUserDataSets(testUser)) {
            List<Sample> activeSamples = dao.getActiveSamplesForDataSet(testUser, dataSet.getIdentifier());
            for(Sample sample : activeSamples) {
                List<Sample> matchingSamples = dao.getSamplesBySlideCode(testUser, sample.getDataSet(), sample.getSlideCode());
                assertFalse(matchingSamples.isEmpty());
                Sample matchingActiveSample = dao.getActiveSampleBySlideCode(testUser, sample.getDataSet(), sample.getSlideCode());
                assertEquals(sample.getId(), matchingActiveSample.getId());
                checked = true;
            }
        }
        assertTrue("Test user has no data set with samples", checked);
    }


}
