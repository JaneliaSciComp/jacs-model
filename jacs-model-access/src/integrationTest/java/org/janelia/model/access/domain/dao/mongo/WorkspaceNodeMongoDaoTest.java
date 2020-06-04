package org.janelia.model.access.domain.dao.mongo;

import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.domain.workspace.Workspace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class WorkspaceNodeMongoDaoTest extends AbstractMongoDaoTest {

    private static final String TEST_OWNER = "testOwner";

    private WorkspaceNodeMongoDao workspaceNodeMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        SubjectMongoDao subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
    }

    @Test
    public void defaultWorkspace() {
        Workspace firstDefaultWorkspace = workspaceNodeMongoDao.createDefaultWorkspace(TEST_OWNER);
        Workspace secondDefaultWorkspace = workspaceNodeMongoDao.createDefaultWorkspace(TEST_OWNER);
        assertNotSame(firstDefaultWorkspace, secondDefaultWorkspace);
        assertEquals(firstDefaultWorkspace.getId(), secondDefaultWorkspace.getId());

        Workspace retrievedDefaultWorkspace = workspaceNodeMongoDao.getDefaultWorkspace(TEST_OWNER);
        assertNotSame(firstDefaultWorkspace, retrievedDefaultWorkspace);
        assertEquals(firstDefaultWorkspace.getId(), retrievedDefaultWorkspace.getId());
    }

}
