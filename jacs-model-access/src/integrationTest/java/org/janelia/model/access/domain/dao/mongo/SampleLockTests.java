package org.janelia.model.access.domain.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.dao.DomainDAOManager;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.sample.SampleLock;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for sample locking.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleLockTests extends AbstractMongoDaoTest {

    private static final DomainDAO dao = DomainDAOManager.getInstance().getDao();

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;
    private static final String testDataSetIdentifier = testUser+"_screen";
    private static final String testSlideCodePrefix = "20130104_23_A";
    private static final String lockDescription = "Test Lock";
    private static final Long testTaskId = 123456789L;

    private SubjectMongoDao subjectMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
    }

    private void createTestData() throws Exception {
        subjectMongoDao.createUser(testName, "Tester", null);

        for (int i = 1; i < 10; i++) {
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
