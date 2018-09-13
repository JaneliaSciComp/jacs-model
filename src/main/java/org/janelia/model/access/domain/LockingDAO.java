package org.janelia.model.access.domain;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import org.janelia.model.domain.DomainObjectLock;
import org.janelia.model.domain.Reference;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistence for distributed locks
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LockingDAO extends BaseDAO {

    private static final Logger log = LoggerFactory.getLogger(DomainDAO.class);

    protected DomainDAO domainDao;
    protected MongoCollection objectLockCollection;

    public LockingDAO(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
        this.domainDao = new DomainDAO(mongoClient, databaseName);
        this.objectLockCollection = getCollectionByClass(DomainObjectLock.class);
    }

    /**
     * Attempts to lock an object for the given task id and owner. The caller must check the return value of this method.
     * If null is returned, then the object could not be locked. Only if a non-null DomainObjectLock is returned can the
     * object be considered locked.
     * The caller of this method is expected to call unlockObject after their job is done.
     * @param subjectKey
     * @param ref
     * @param taskId
     * @param description
     * @return
     */
    public DomainObjectLock lockObject(String subjectKey, Reference ref, Long taskId, String description, long timeoutSecs) {

        long start = System.currentTimeMillis();

        while(true) {

            try {
                // Attempt to atomically refresh or create an existing lock
                WriteResult result = objectLockCollection
                        .update("{ownerKey:#, taskId:#, objectRef:#}", subjectKey, taskId, ref.toString()).upsert()
                        .with("{$currentDate:{'creationDate':true}, $set:{{description:#}}}", description);

                if (result.getN() < 1) {
                    // Nothing was updated
                    log.error("Task {} ({}) tried to lock {} and failed for unknown reasons", taskId, subjectKey, ref);
                }
                else {
                    // Lock was updated, now let's fetch it and return it
                    DomainObjectLock lock = objectLockCollection
                            .findOne("{ownerKey:#, taskId:#, objectRef:#}", subjectKey, taskId, ref.toString())
                            .as(DomainObjectLock.class);
                    if (lock == null) {
                        log.error("Task {} ({}) locked on {}, but it cannot be found.", taskId, subjectKey, ref);
                    }
                    else {
                        log.debug("Task {} ({}) locked on {}", taskId, subjectKey, ref);
                    }
                    return lock;
                }
            }
            catch (DuplicateKeyException e) {
                log.error("Task {} ({}) tried to lock {} and failed because there is an existing lock", taskId, subjectKey, ref);
            }

            long elapsed = System.currentTimeMillis()-start;
            if (elapsed > timeoutSecs*1000) {
                log.error("Task {} ({}) gave up after {} seconds waiting to lock {}", taskId, subjectKey, elapsed/1000, ref);
                return null;
            }

            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                log.error("Lock sleep interrupted, returning null lock");
                return null;
            }
        }
    }

    /**
     * Attempts to unlock a sample, given the lock holder's task id and owner.
     * @param subjectKey
     * @param ref
     * @param taskId
     * @return
     */
    public boolean unlockObject(String subjectKey, Reference ref, Long taskId) {

        WriteResult result = objectLockCollection
                .remove("{ownerKey:#, taskId:#, objectRef:#}", subjectKey, taskId, ref.toString());

        if (result.getN() != 1) {

            // The following is not atomic, but it may still provide some information for debugging
            DomainObjectLock lock = objectLockCollection
                    .findOne("{objectRef:#}", ref.toString()).as(DomainObjectLock.class);
            if (lock==null) {
                log.error("Task {} ({}) tried to remove lock on {} and failed. "
                        + "It looks like the lock may have expired.", taskId, subjectKey, ref);
            }
            else {
                log.error("Task {} ({}) tried to remove lock on {} and failed. "
                        + "It looks like the lock is owned by someone else: {}.", taskId, subjectKey, ref, lock);
            }

            return false;
        }

        log.debug("Task {} ({}) removed lock on {}", taskId, subjectKey, ref);
        return true;
    }

}
