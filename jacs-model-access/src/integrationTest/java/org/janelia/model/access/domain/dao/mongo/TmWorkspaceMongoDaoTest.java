package org.janelia.model.access.domain.dao.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ITestDomainDAOManager;
import org.janelia.model.access.domain.dao.TmMappedNeuronDao;
import org.janelia.model.access.domain.dao.TmNeuronMetadataDao;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.tiledMicroscope.BoundingBox3d;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.domain.workspace.Workspace;
import org.janelia.model.security.Group;
import org.janelia.model.security.GroupRole;
import org.janelia.model.security.Subject;
import org.janelia.model.security.User;
import org.janelia.model.security.UserGroupRole;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class TmWorkspaceMongoDaoTest extends AbstractMongoDaoTest {

    private static final String TEST_G1 = "g1";
    private static final String[] TEST_G1_USERS = new String[] {
            "g1_u1",
            "g1_u2"
    };
    private static final String TEST_G2 = "g2";
    private static final String[] TEST_G2_USERS = new String[] {
            "g2_u1",
            "g2_u2"
    };
    private static final DomainDAO dao = ITestDomainDAOManager.getInstance().getDao();

    private SubjectMongoDao subjectMongoDao;
    private WorkspaceNodeMongoDao workspaceNodeMongoDao;
    private TmWorkspaceMongoDao tmWorkspaceMongoDao;
    private TmNeuronMetadataDao tmNeuronMetadataDao;

    private static class TestSubjects {
        Group g1;
        Group g2;
        List<User> g1Users;
        List<User> g2Users;
    }
    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        DomainPermissionsMongoHelper permissionsMongoHelper = new DomainPermissionsMongoHelper(subjectMongoDao);
        DomainUpdateMongoHelper updateMongoHelper = new DomainUpdateMongoHelper(testObjectMapper);
        GridFSMongoDao neuronGridFS = new GridFSMongoDao(testMongoDatabase);
        workspaceNodeMongoDao = new WorkspaceNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                updateMongoHelper);
        tmNeuronMetadataDao = new TmNeuronMetadataMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                updateMongoHelper,
                neuronGridFS
        );
        TmMappedNeuronDao tmMappedNeuronDao = new TmMappedNeuronMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                updateMongoHelper
        );
        tmWorkspaceMongoDao = new TmWorkspaceMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                permissionsMongoHelper,
                updateMongoHelper,
                dao,
                tmNeuronMetadataDao,
                tmMappedNeuronDao,
                neuronGridFS
        );
    }

    @Test
    public void usersCanCreateNeuronsIfTheyHavePermissions() {
        TestSubjects testSubjects = createAllTestSubjects();
        TmWorkspace testWorkspace = createTestWorkspace(
                "testWs",
                testSubjects.g1Users.get(0),
                ImmutableSet.of(testSubjects.g1.getKey()),
                ImmutableSet.of(testSubjects.g1.getKey()));
        try {
            TmNeuronMetadata testNeuron = createTestNeuron("tn", testWorkspace);
            tmNeuronMetadataDao.createTmNeuronInWorkspace(testSubjects.g1Users.get(1).getKey(), testNeuron, testWorkspace);
        } finally {
            remoteTestWorkspace(testWorkspace);
            removeAllTestSubjects(testSubjects);
        }
    }

    @Test
    public void usersCannotCreateNeuronsIfTheyDoNotHavePermissions() {
        TestSubjects testSubjects = createAllTestSubjects();
        TmWorkspace testWorkspace = createTestWorkspace(
                "testWs",
                testSubjects.g1Users.get(0),
                ImmutableSet.of(testSubjects.g1.getKey()),
                ImmutableSet.of(testSubjects.g1.getKey()));
        try {
            TmNeuronMetadata testNeuron = createTestNeuron("tn", testWorkspace);
            tmNeuronMetadataDao.createTmNeuronInWorkspace(testSubjects.g2Users.get(0).getKey(), testNeuron, testWorkspace);
            fail("User " + testSubjects.g2Users.get(0).getKey() + " should not be able to create neurons in " + testWorkspace.getName());
        } catch (SecurityException e) {
            // this is expected
        } finally {
            remoteTestWorkspace(testWorkspace);
            removeAllTestSubjects(testSubjects);
        }
    }

    @Test
    public void getUndefinedBoundingBoxes() {
        List<BoundingBox3d> boundingBox3dList = tmWorkspaceMongoDao.getWorkspaceBoundingBoxes(100L);
        assertTrue(boundingBox3dList.isEmpty());
    }

    private TmWorkspace createTestWorkspace(String name,
                                            User u,
                                            Set<String> readers,
                                            Set<String> writers) {
        TmWorkspace tmWorkspace = new TmWorkspace();
        tmWorkspace.setName(name);
        tmWorkspace.addReaders(readers);
        tmWorkspace.addWriters(writers);
        return tmWorkspaceMongoDao.createTmWorkspace(u.getKey(), tmWorkspace);
    }

    private TmNeuronMetadata createTestNeuron(String name, TmWorkspace ws) {
        return new TmNeuronMetadata(ws, name);
    }

    private void remoteTestWorkspace(TmWorkspace ws) {
        tmWorkspaceMongoDao.delete(ws, ws.getOwnerKey());
    }

    private TestSubjects createAllTestSubjects() {
        TestSubjects testSubjects = new TestSubjects();
        testSubjects.g1 = subjectMongoDao.createGroup(TEST_G1, TEST_G1, null);
        testSubjects.g1Users = createTestUsersInGroup(TEST_G1_USERS, testSubjects.g1);
        testSubjects.g2 = subjectMongoDao.createGroup(TEST_G2, TEST_G2, null);
        testSubjects.g2Users = createTestUsersInGroup(TEST_G2_USERS, testSubjects.g2);
        return testSubjects;
    }

    private List<User> createTestUsersInGroup(String[] unames, Group g) {
        List<User> users = new ArrayList<>();
        for (String uname : unames) {
            User u = subjectMongoDao.createUser(uname, uname, null);
            subjectMongoDao.updateUserGroupRoles(u, ImmutableSet.of(
                    new UserGroupRole(g.getKey(), GroupRole.Reader),
                    new UserGroupRole(g.getKey(), GroupRole.Writer)
            ));
            workspaceNodeMongoDao.createDefaultWorkspace(u.getKey());
            users.add(u);
        }
        return users;
    }

    private void removeAllTestSubjects(TestSubjects testSubjects) {
        removeTestSubject(testSubjects.g1);
        removeTestSubject(testSubjects.g2);
        for (User u : testSubjects.g1Users) removeTestUser(u);
        for (User u : testSubjects.g2Users) removeTestUser(u);
    }

    private void removeTestUser(User u) {
        Workspace uw = workspaceNodeMongoDao.getDefaultWorkspace(u.getKey());
        if (uw != null) workspaceNodeMongoDao.delete(uw);
        removeTestSubject(u);
    }

    private void removeTestSubject(Subject s) {
        if (s != null) subjectMongoDao.removeSubjectByKey(s.getKey());
    }

}
