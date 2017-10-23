package org.janelia.it.jacs.model.domain;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.janelia.it.jacs.model.domain.sample.Sample;
import org.janelia.it.jacs.model.domain.sample.SampleLock;
import org.janelia.it.jacs.model.domain.support.DomainDAO;
import org.junit.Test;

/**
 * Tests for sample locking.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleLockTests {

    private static final DomainDAO dao = DomainDAOManager.getInstance().getDao();

    private static final String OWNER = "user:wolfft";
    private static final String DATA_SET = "wolfft_central_tile_mcfo_case_1";
    private static final String SLIDE_CODE = "20130605_1_A2";
    private static final String DESCRIPTION = "SampleLockTests";
    private static final Long TASK_ID = 123456789L;
    
    @Test
    public void testGetSubjects() throws Exception {

        // Lock the samples
        List<SampleLock> locks = new ArrayList<>();
        for(Sample sample : dao.getSamplesBySlideCode(OWNER, DATA_SET, SLIDE_CODE)) {
            SampleLock lock = dao.lockSample(OWNER, sample.getId(), TASK_ID, DESCRIPTION);
            assertNotNull(lock);
            assertEquals(OWNER, lock.getOwnerKey());
            assertEquals(TASK_ID, lock.getTaskId());
            assertEquals(sample.getId(), lock.getSampleRef().getTargetId());
            assertEquals(DESCRIPTION, lock.getDescription());
            assertNotNull(lock.getCreationDate());
            locks.add(lock);
        }
        
        assertFalse(locks.isEmpty());

        // Try to reenter the locks (this should work)
        for(SampleLock lock : locks) {
            SampleLock newLock = dao.lockSample(OWNER, lock.getSampleRef().getTargetId(), TASK_ID, DESCRIPTION);
            assertNotNull(newLock);
        }

        // Try to get the locks as another user (this should fail)
        for(SampleLock lock : locks) {
            SampleLock newLock = dao.lockSample("UnknownActor", lock.getSampleRef().getTargetId(), TASK_ID, DESCRIPTION);
            assertNull(newLock);
        }

        // Try to get the locks as another task (this should fail)
        for(SampleLock lock : locks) {
            SampleLock newLock = dao.lockSample(OWNER, lock.getSampleRef().getTargetId(), 1L, DESCRIPTION);
            assertNull(newLock);
        }

        // Try to unlock as another user (this should fail)
        for(SampleLock lock : locks) {
            boolean unlocked = dao.unlockSample("UnknownActor", lock.getSampleRef().getTargetId(), TASK_ID);
            assertFalse(unlocked);
        }
        
        // Unlock
        for(SampleLock lock : locks) {
            boolean unlocked = dao.unlockSample(OWNER, lock.getSampleRef().getTargetId(), TASK_ID);
            assertTrue(unlocked);
        }
        
        // Try to unlock again (this should fail)
        for(SampleLock lock : locks) {
            boolean unlocked = dao.unlockSample(OWNER, lock.getSampleRef().getTargetId(), TASK_ID);
            assertFalse(unlocked);
        }
    }
}
