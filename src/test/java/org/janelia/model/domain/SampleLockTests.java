package org.janelia.model.domain;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.sample.SampleLock;
import org.jongo.MongoCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for sample locking.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleLockTests {

    private static final DomainDAO dao = DomainDAOManager.getInstance().getDao();

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;
    private static final String testDataSetIdentifier = testUser+"_screen";
    private static final String testSlideCodePrefix = "20130104_23_A";
    private static final String lockDescription = "Test Lock";
    private static final Long testTaskId = 123456789L;

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

    private void createTestData() throws Exception {
        dao.createUser(testName, "Tester", null);

        for(int i=1; i<10; i++) {
            Sample sample = new Sample();
            sample.setDataSet(testDataSetIdentifier);
            sample.setSlideCode(testSlideCodePrefix + i);
            dao.save(testUser, sample);
        }

    }

    @Test
    public void testSampleLocking() throws Exception {
        createTestData();

        // TODO: this should call the same code as the MongoDbMaintainer. For now it's copy and pasted:
        MongoCollection sampleLockCollection = dao.getCollectionByClass(SampleLock.class);
        sampleLockCollection.ensureIndex("{creationDate:1}", "{expireAfterSeconds:120}");
        sampleLockCollection.ensureIndex("{sampleRef:1}", "{unique:true}");
        sampleLockCollection.ensureIndex("{ownerKey:1,taskId:1,sampleRef:1}");

        // Lock the samples
        List<SampleLock> locks = new ArrayList<>();
        for(Sample sample : dao.getSamplesByDataSet(testUser, testDataSetIdentifier)) {
            SampleLock lock = dao.lockSample(testUser, sample.getId(), testTaskId, lockDescription);
            assertNotNull(lock);
            assertEquals(testUser, lock.getOwnerKey());
            assertEquals(testTaskId, lock.getTaskId());
            assertEquals(sample.getId(), lock.getSampleRef().getTargetId());
            assertEquals(lockDescription, lock.getDescription());
            assertNotNull(lock.getCreationDate());
            locks.add(lock);
        }
        
        assertFalse(locks.isEmpty());

        // Try to reenter the locks (this should work)
        for(SampleLock lock : locks) {
            SampleLock newLock = dao.lockSample(testUser, lock.getSampleRef().getTargetId(), testTaskId, lockDescription);
            assertNotNull(newLock);
        }

        // Try to get the locks as another user (this should fail)
        for(SampleLock lock : locks) {
            SampleLock newLock = dao.lockSample("UnknownActor", lock.getSampleRef().getTargetId(), testTaskId, lockDescription);
            assertNull(newLock);
        }

        // Try to get the locks as another task (this should fail)
        for(SampleLock lock : locks) {
            SampleLock newLock = dao.lockSample(testUser, lock.getSampleRef().getTargetId(), 1L, lockDescription);
            assertNull(newLock);
        }

        // Try to unlock as another user (this should fail)
        for(SampleLock lock : locks) {
            boolean unlocked = dao.unlockSample("UnknownActor", lock.getSampleRef().getTargetId(), testTaskId);
            assertFalse(unlocked);
        }
        
        // Unlock
        for(SampleLock lock : locks) {
            boolean unlocked = dao.unlockSample(testUser, lock.getSampleRef().getTargetId(), testTaskId);
            assertTrue(unlocked);
        }
        
        // Try to unlock again (this should fail)
        for(SampleLock lock : locks) {
            boolean unlocked = dao.unlockSample(testUser, lock.getSampleRef().getTargetId(), testTaskId);
            assertFalse(unlocked);
        }
    }
}
