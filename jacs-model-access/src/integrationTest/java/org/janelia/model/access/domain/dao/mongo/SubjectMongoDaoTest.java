package org.janelia.model.access.domain.dao.mongo;

import org.janelia.model.security.Group;
import org.janelia.model.security.GroupRole;
import org.janelia.model.security.Subject;
import org.janelia.model.security.User;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SubjectMongoDaoTest extends AbstractMongoDaoTest {

    private static final String testUser = "unittester";
    private static final String testUserKey = "user:unittester";
    private static final String testUserFullName = "Unit Tester";
    private static final String testGroup = "unittesters";
    private static final String testGroupFullName = "Unit Tester Group";

    private SubjectMongoDao subjectMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
    }


    @Test
    public void testGetSubjects() {
        User u = subjectMongoDao.createUser(testUser, testUserFullName, null);
        Group g = subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        boolean hasUsers = false;
        boolean hasGroups = false;
        for(Subject subject : subjectMongoDao.findAll(0, -1)) {
            if (subject.getKey().startsWith("user")) hasUsers = true;
            if (subject.getKey().startsWith("group")) hasGroups = true;
        }
        assertTrue(hasUsers);
        assertTrue(hasGroups);
        subjectMongoDao.delete(u);
        subjectMongoDao.delete(g);
    }

    @Test
    public void testGetUsers() {
        User u = subjectMongoDao.createUser(testUser, testUserFullName, null);
        Group g = subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        boolean hasUsers = false;
        boolean hasGroups = false;
        for(Subject subject : subjectMongoDao.findAllUsers()) {
            if (subject.getKey().startsWith("user")) hasUsers = true;
            if (subject.getKey().startsWith("group")) hasGroups = true;
        }
        assertTrue(hasUsers);
        assertFalse(hasGroups);
        subjectMongoDao.delete(u);
        subjectMongoDao.delete(g);
    }

    @Test
    public void testGetGroups() {
        User u = subjectMongoDao.createUser(testUser, testUserFullName, null);
        Group g = subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        boolean hasUsers = false;
        boolean hasGroups = false;
        for(Subject subject : subjectMongoDao.findAllGroups()) {
            if (subject.getKey().startsWith("user")) hasUsers = true;
            if (subject.getKey().startsWith("group")) hasGroups = true;
        }
        assertFalse(hasUsers);
        assertTrue(hasGroups);
        subjectMongoDao.delete(u);
        subjectMongoDao.delete(g);
    }

    @Test
    public void testUserGetters() {
        subjectMongoDao.createUser(testUser, testUserFullName, null);
        Subject subject = subjectMongoDao.findSubjectByNameOrKey(testUser);
        assertNotNull(subject);
        
        subject = subjectMongoDao.findSubjectByName(subject.getName());
        assertNotNull(subject);

        subject = subjectMongoDao.findSubjectByKey(subject.getKey());
        assertNotNull(subject);

        subject = subjectMongoDao.findUserByNameOrKey(subject.getName());
        assertNotNull(subject);

        subject = subjectMongoDao.findUserByNameOrKey(subject.getKey());
        assertNotNull(subject);

        assertNull(subjectMongoDao.findGroupByNameOrKey(subject.getKey()));

        subjectMongoDao.delete(subject);
    }

    @Test
    public void caseInsensitiveUserGetter() {
        class TestData {
            final String uname;
            final String fullName;
            final String[] searchStrings;

            TestData(String uname, String fullName, String[] searchStrings) {
                this.uname = uname;
                this.fullName = fullName;
                this.searchStrings = searchStrings;
            }
        }
        TestData[] testData = new TestData[] {
                new TestData("user1", "FullUser1", new String[]{"user1", "USer1"}),
                new TestData("User2", "FullUser2", new String[]{"user2", "uSEr2"})
        };
        for (TestData td : testData) {
            User testUser = subjectMongoDao.createUser(td.uname, td.fullName, null);
            assertEquals(td.uname.toLowerCase(), testUser.getName());
            for (String searchedUsername : td.searchStrings) {
                Subject foundUser = subjectMongoDao.findSubjectByName(searchedUsername);
                assertNotNull("No user found for " + searchedUsername, foundUser);
                assertEquals(foundUser.getId(), testUser.getId());
            }
        }
    }

    @Test
    public void testGroupGetters() throws Exception {
        subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        Subject subject = subjectMongoDao.findSubjectByNameOrKey(testGroup);
        assertNotNull(subject);
        
        subject = subjectMongoDao.findSubjectByName(subject.getName());
        assertNotNull(subject);

        subject = subjectMongoDao.findSubjectByKey(subject.getKey());
        assertNotNull(subject);

        subject = subjectMongoDao.findGroupByNameOrKey(subject.getName());
        assertNotNull(subject);

        subject = subjectMongoDao.findGroupByNameOrKey(subject.getKey());
        assertNotNull(subject);

        assertNull(subjectMongoDao.findUserByNameOrKey(subject.getKey()));
        subjectMongoDao.delete(subject);
    }
    
    @Test
    public void testAddRemoveFromGroup() throws Exception {
        subjectMongoDao.createUser(testUser, testUserFullName, null);
        Group group = subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        subjectMongoDao.addUserToGroup(testUser, testGroup, GroupRole.Owner);
        User user = subjectMongoDao.findUserByNameOrKey(testUser);
        assertTrue(user.hasGroupWrite(group.getKey()));
        subjectMongoDao.delete(user);
        subjectMongoDao.delete(group);
    }

    @Test
    public void testGroupRead() {
        subjectMongoDao.createUser(testUser, testUserFullName, null);
        Subject group = subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        subjectMongoDao.addUserToGroup(testUser, testGroup, GroupRole.Reader);
        User user = subjectMongoDao.findUserByNameOrKey(testUser);
        assertTrue(user.hasGroupRead(group.getKey()));
        assertFalse(user.hasGroupWrite(group.getKey()));
        subjectMongoDao.delete(user);
        subjectMongoDao.delete(group);
    }
    
    @Test
    public void testGroupWrite() {
        subjectMongoDao.createUser(testUser, testUserFullName, null);
        Subject group = subjectMongoDao.createGroup(testGroup, testGroupFullName, null);
        subjectMongoDao.addUserToGroup(testUser, testGroup, GroupRole.Writer);
        User user = subjectMongoDao.findUserByNameOrKey(testUser);
        assertTrue(user.hasGroupRead(group.getKey()));
        assertTrue(user.hasGroupWrite(group.getKey()));
        subjectMongoDao.delete(user);
        subjectMongoDao.delete(group);
    }
}
