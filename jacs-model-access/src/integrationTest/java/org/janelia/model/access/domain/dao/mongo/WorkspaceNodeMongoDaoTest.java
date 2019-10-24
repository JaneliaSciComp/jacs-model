package org.janelia.model.access.domain.dao.mongo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.NodeUtils;
import org.janelia.model.domain.workspace.TreeNode;
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
        SubjectMongoDao subjectMongoDao = new SubjectMongoDao(testMongoDatabase);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
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
