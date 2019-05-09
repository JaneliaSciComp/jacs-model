package org.janelia.model.access.domain.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.janelia.model.security.GroupRole;
import org.janelia.model.security.Subject;
import org.janelia.model.security.User;
import org.janelia.model.access.domain.DomainDAO;
import org.janelia.model.domain.workspace.Workspace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SubjectDAOTest {

    private static final String testUser = "unittester";
    private static final String testUserKey = "user:unittester";
    private static final String testUserFullName = "Unit Tester";
    private static final String testGroup = "unittesters";
    private static final String testGroupFullName = "Unit Tester Group";
    
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
        try {
            Workspace workspace = dao.getDefaultWorkspace(testUserKey);
            if (workspace!=null) {
                dao.remove(testUserKey, workspace);
            }
        }
        catch (Exception e) {
            System.out.println("Could not clean test workspace. This is probably a good thing.");
        }
        try {
            dao.removeUser(testUser);
        }
        catch (Exception e) {
            System.out.println("Could not clean test user. This is probably a good thing.");
        }
        try {
            dao.removeGroup(testGroup);
        }
        catch (Exception e) {
            System.out.println("Could not clean test group. This is probably a good thing.");
        }
    }
    
    @Test
    public void testGetSubjects() throws Exception {
        dao.createUser(testUser, testUserFullName, null);
        dao.createGroup(testGroup, testGroupFullName);
        boolean hasUsers = false;
        boolean hasGroups = false;
        for(Subject subject : dao.getSubjects()) {
            if (subject.getKey().startsWith("user")) hasUsers = true;
            if (subject.getKey().startsWith("group")) hasGroups = true;
        }
        assertTrue(hasUsers);
        assertTrue(hasGroups);
        dao.removeUser(testUser);
        dao.removeGroup(testGroup);
    }

    @Test
    public void testGetUsers() throws Exception {
        dao.createUser(testUser, testUserFullName, null);
        dao.createGroup(testGroup, testGroupFullName);
        boolean hasUsers = false;
        boolean hasGroups = false;
        for(Subject subject : dao.getUsers()) {
            if (subject.getKey().startsWith("user")) hasUsers = true;
            if (subject.getKey().startsWith("group")) hasGroups = true;
        }
        assertTrue(hasUsers);
        assertFalse(hasGroups);
        dao.removeUser(testUser);
        dao.removeGroup(testGroup);
    }

    @Test
    public void testGetGroups() throws Exception {
        dao.createUser(testUser, testUserFullName, null);
        dao.createGroup(testGroup, testGroupFullName);
        boolean hasUsers = false;
        boolean hasGroups = false;
        for(Subject subject : dao.getGroups()) {
            if (subject.getKey().startsWith("user")) hasUsers = true;
            if (subject.getKey().startsWith("group")) hasGroups = true;
        }
        assertFalse(hasUsers);
        assertTrue(hasGroups);
        dao.removeUser(testUser);
        dao.removeGroup(testGroup);
    }

    @Test
    public void testCreateDeleteUser() throws Exception {
        Subject subject = dao.createUser(testUser, testUserFullName, null);
        assertEquals(testUser, subject.getName());
        assertEquals(testUserFullName, subject.getFullName());
        Workspace workspace = dao.getDefaultWorkspace(subject.getKey());
        assertNotNull(workspace);
        assertEquals(subject.getKey(), workspace.getOwnerKey());
        
        dao.removeUser(testUser);
        subject = dao.getSubjectByNameOrKey(testUser);
        assertNull(subject);
    }

    @Test
    public void testUserGetters() throws Exception {
        Subject subject = dao.createUser(testUser, testUserFullName, null);
        subject = dao.getSubjectByNameOrKey(testUser);
        assertNotNull(subject);
        
        subject = dao.getSubjectByName(subject.getName());
        assertNotNull(subject);

        subject = dao.getSubjectByKey(subject.getKey());
        assertNotNull(subject);

        subject = dao.getUserByNameOrKey(subject.getName());
        assertNotNull(subject);

        subject = dao.getUserByNameOrKey(subject.getKey());
        assertNotNull(subject);
        
        dao.removeUser(testUser);
    }
    
    @Test
    public void testCreateDeleteGroup() throws Exception {
        Subject subject = dao.createGroup(testGroup, testGroupFullName);
        assertEquals(testGroup, subject.getName());
        assertEquals(testGroupFullName, subject.getFullName());
        
        dao.removeGroup(testGroup);
        subject = dao.getSubjectByNameOrKey(testGroup);
        assertNull(subject);
    }

    @Test
    public void testGroupGetters() throws Exception {
        Subject subject = dao.createGroup(testGroup, testGroupFullName);
        subject = dao.getSubjectByNameOrKey(testGroup);
        assertNotNull(subject);
        
        subject = dao.getSubjectByName(subject.getName());
        assertNotNull(subject);

        subject = dao.getSubjectByKey(subject.getKey());
        assertNotNull(subject);

        subject = dao.getGroupByNameOrKey(subject.getName());
        assertNotNull(subject);

        subject = dao.getGroupByNameOrKey(subject.getKey());
        assertNotNull(subject);
        
        dao.removeGroup(testGroup);
    }
    
    @Test
    public void testAddRemoveFromGroup() throws Exception {
        User user = dao.createUser(testUser, testUserFullName, null);
        Subject group = dao.createGroup(testGroup, testGroupFullName);
        dao.addUserToGroup(testUser, testGroup, GroupRole.Owner);
        user = dao.getUserByNameOrKey(testUser);
        assertTrue(user.hasGroupWrite(group.getKey()));
        dao.removeUser(testUser);
        dao.removeGroup(testGroup);
    }

    @Test
    public void testGroupRead() throws Exception {
        User user = dao.createUser(testUser, testUserFullName, null);
        Subject group = dao.createGroup(testGroup, testGroupFullName);
        dao.addUserToGroup(testUser, testGroup, GroupRole.Reader);
        user = dao.getUserByNameOrKey(testUser);
        assertTrue(user.hasGroupRead(group.getKey()));
        assertFalse(user.hasGroupWrite(group.getKey()));
        dao.removeUser(testUser);
        dao.removeGroup(testGroup);
    }
    
    @Test
    public void testGroupWrite() throws Exception {
        User user = dao.createUser(testUser, testUserFullName, null);
        Subject group = dao.createGroup(testGroup, testGroupFullName);
        dao.addUserToGroup(testUser, testGroup, GroupRole.Writer);
        user = dao.getUserByNameOrKey(testUser);
        assertTrue(user.hasGroupRead(group.getKey()));
        assertTrue(user.hasGroupWrite(group.getKey()));
        dao.removeUser(testUser);
        dao.removeGroup(testGroup);
    }
}
