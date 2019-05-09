package org.janelia.model.access.domain;

import com.fasterxml.jackson.databind.MapperFeature;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.janelia.model.domain.*;
import org.janelia.model.domain.compartments.CompartmentSet;
import org.janelia.model.domain.enums.OrderStatus;
import org.janelia.model.domain.enums.PipelineStatus;
import org.janelia.model.domain.gui.alignment_board.AlignmentBoard;
import org.janelia.model.domain.gui.alignment_board.AlignmentContext;
import org.janelia.model.domain.gui.colordepth.ColorDepthMask;
import org.janelia.model.domain.gui.colordepth.ColorDepthResult;
import org.janelia.model.domain.gui.colordepth.ColorDepthSearch;
import org.janelia.model.domain.gui.search.Filter;
import org.janelia.model.domain.gui.search.criteria.FacetCriteria;
import org.janelia.model.domain.ontology.*;
import org.janelia.model.domain.orders.IntakeOrder;
import org.janelia.model.domain.sample.*;
import org.janelia.model.domain.screen.FlyLine;
import org.janelia.model.domain.tiledMicroscope.TmReviewTask;
import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.domain.workspace.Workspace;
import org.janelia.model.security.*;
import org.janelia.model.security.util.SubjectUtils;
import org.jongo.*;
import org.jongo.marshall.jackson.JacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.janelia.model.domain.DomainUtils.abbr;

/**
 * Data access object for the domain object model.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainDAO {

    private static final Logger log = LoggerFactory.getLogger(DomainDAO.class);

    protected MongoClient m;
    protected Jongo jongo;

    protected String databaseName;

    protected MongoCollection preferenceCollection;
    protected MongoCollection alignmentBoardCollection;
    protected MongoCollection alignmentContextCollection;
    protected MongoCollection annotationCollection;
    protected MongoCollection compartmentSetCollection;
    protected MongoCollection dataSetCollection;
    protected MongoCollection releaseCollection;
    protected MongoCollection flyLineCollection;
    protected MongoCollection fragmentCollection;
    protected MongoCollection imageCollection;
    protected MongoCollection ontologyCollection;
    protected MongoCollection pipelineStatusCollection;
    protected MongoCollection intakeOrdersCollection;
    protected MongoCollection sampleCollection;
    protected MongoCollection sampleLockCollection;
    protected MongoCollection subjectCollection;
    protected MongoCollection treeNodeCollection;
    protected MongoCollection tmSampleCollection;
    protected MongoCollection tmWorkspaceCollection;
    protected MongoCollection tmNeuronCollection;
    protected MongoCollection tmReviewTaskCollection;
    protected MongoCollection colorDepthMaskCollection;
    protected MongoCollection colorDepthSearchCollection;
    protected MongoCollection colorDepthResultCollection;

    public DomainDAO(String serverUrl, String databaseName) {
        this(serverUrl, databaseName, null, null);
    }

    public DomainDAO(String serverUrl, String databaseName, String username, String password) {
        List<ServerAddress> members = new ArrayList<>();
        for (String serverMember : serverUrl.split(",")) {
            members.add(new ServerAddress(serverMember));
        }

        MongoClientOptions options = MongoClientOptions.builder().writeConcern(WriteConcern.JOURNALED).build();

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            MongoCredential credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
            this.m = new MongoClient(members, credential, options);
            log.info("Connected to MongoDB (" + databaseName + "@" + serverUrl + ") as user " + username);
        } else {
            this.m = new MongoClient(members, options);
            log.info("Connected to MongoDB (" + databaseName + "@" + serverUrl + ")");
        }

        init(m, databaseName);
    }

    public DomainDAO(MongoClient m, String databaseName) {
        init(m, databaseName);
    }

    @SuppressWarnings("deprecation")
    private void init(MongoClient m, String databaseName) {
        this.m = m;
        this.databaseName = databaseName;
        this.jongo = new Jongo(m.getDB(databaseName),
                new JacksonMapper.Builder()
                        .enable(MapperFeature.AUTO_DETECT_GETTERS)
                        .enable(MapperFeature.AUTO_DETECT_SETTERS)
                        .build());
        this.alignmentBoardCollection = getCollectionByClass(AlignmentBoard.class);
        this.alignmentContextCollection = getCollectionByClass(AlignmentContext.class);
        this.annotationCollection = getCollectionByClass(Annotation.class);
        this.compartmentSetCollection = getCollectionByClass(CompartmentSet.class);
        this.dataSetCollection = getCollectionByClass(DataSet.class);
        this.releaseCollection = getCollectionByClass(LineRelease.class);
        this.flyLineCollection = getCollectionByClass(FlyLine.class);
        this.fragmentCollection = getCollectionByClass(NeuronFragment.class);
        this.imageCollection = getCollectionByClass(Image.class);
        this.ontologyCollection = getCollectionByClass(Ontology.class);
        this.sampleCollection = getCollectionByClass(Sample.class);
        this.sampleLockCollection = getCollectionByClass(SampleLock.class);
        this.pipelineStatusCollection = getCollectionByClass(StatusTransition.class);
        this.intakeOrdersCollection = getCollectionByClass(IntakeOrder.class);
        this.subjectCollection = getCollectionByClass(Subject.class);
        this.treeNodeCollection = getCollectionByClass(TreeNode.class);
        this.preferenceCollection = getCollectionByClass(Preference.class);
        this.tmSampleCollection = getCollectionByClass(TmSample.class);
        this.tmWorkspaceCollection = getCollectionByClass(TmWorkspace.class);
        this.tmNeuronCollection = getCollectionByClass(TmNeuronMetadata.class);
        this.tmReviewTaskCollection = getCollectionByClass(TmReviewTask.class);
        this.colorDepthMaskCollection = getCollectionByClass(ColorDepthMask.class);
        this.colorDepthSearchCollection = getCollectionByClass(ColorDepthSearch.class);
        this.colorDepthResultCollection = getCollectionByClass(ColorDepthResult.class);

    }

    public com.mongodb.client.MongoCollection<Document> getNativeCollection(String collectionName) {
        MongoDatabase db = m.getDatabase(databaseName);
        return db.getCollection(collectionName);
    }

    public final MongoCollection getCollectionByClass(Class<?> domainClass) {
        String collectionName = DomainUtils.getCollectionName(domainClass);
        return jongo.getCollection(collectionName);
    }

    public MongoCollection getCollectionByName(String collectionName) {
        if (collectionName == null) {
            throw new IllegalArgumentException("collectionName argument may not be null");
        }
        return jongo.getCollection(collectionName);
    }

    public MongoClient getMongo() {
        return m;
    }

    public Jongo getJongo() {
        return jongo;
    }

    // Subjects

    /**
     * Save the given subject.
     */
    public Subject save(Subject subject) {
        log.debug("save({})", subject);
        if (subject.getId() == null) {
            subject.setId(getNewId());
            subjectCollection.insert(subject);
        } else {
            subjectCollection.update("{_id:#}", subject.getId()).with(subject);
        }

        log.trace("Saved " + subject.getClass().getName() + "#" + subject.getId());
        return subject;
    }

    /**
     * Return all the subjects.
     */
    public List<Subject> getSubjects() {
        return toList(subjectCollection.find().as(Subject.class));
    }

    public List<Subject> getUsers() throws Exception {
        return toList(subjectCollection.find("{key:{$regex:#}}", "^user:").as(Subject.class));
    }

    public List<Subject> getGroups() throws Exception {
        return toList(subjectCollection.find("{key:{$regex:#}}", "^group:").as(Subject.class));
    }

    /**
     * Return the set of subjectKeys which are readable by the given subject.
     * This includes the subject itself, and all of the groups it has read access for.
     */
    public Set<String> getReaderSet(String subjectKey) {
        if (subjectKey == null) return null;
        Subject subject = subjectCollection.findOne("{key:#}", subjectKey).as(Subject.class);
        if (subject == null) {
            throw new IllegalArgumentException("No such subject: " + subjectKey);
        }
        return SubjectUtils.getReaderSet(subject);
    }

    /**
     * Return the set of subjectKeys which are writable by the given subject.
     * This includes the subject itself, and all of the groups it has write access for.
     */
    public Set<String> getWriterSet(String subjectKey) {
        if (subjectKey == null) return null;
        Subject subject = subjectCollection.findOne("{key:#}", subjectKey).as(Subject.class);
        if (subject == null) {
            throw new IllegalArgumentException("No such subject: " + subjectKey);
        }
        return SubjectUtils.getWriterSet(subject);
    }

    public Subject getSubjectByKey(String subjectKey) {
        return subjectCollection.findOne("{key:#}", subjectKey).as(Subject.class);
    }

    public Subject getSubjectByName(String subjectName) {
        return subjectCollection.findOne("{name:#}", subjectName).as(Subject.class);
    }

    /**
     * Return subject by name or key.
     */
    public Subject getSubjectByNameOrKey(String subjectNameOrKey) {
        return subjectCollection.findOne("{$or:[{name:#},{key:#}]}", subjectNameOrKey, subjectNameOrKey).as(Subject.class);
    }

    /**
     * Return user by name or key.
     */
    public User getUserByNameOrKey(String subjectNameOrKey) {
        log.debug("getUserByNameOrKey({})", subjectNameOrKey);
        return subjectCollection.findOne("{$or:[{name:#},{key:#}], class:#}", subjectNameOrKey, subjectNameOrKey, User.class.getName()).as(User.class);
    }

    /**
     * Return group by name or key.
     */
    public Group getGroupByNameOrKey(String subjectNameOrKey) {
        return subjectCollection.findOne("{$or:[{name:#},{key:#}], class:#}", subjectNameOrKey, subjectNameOrKey, Group.class.getName()).as(Group.class);
    }

    public User createUser(String name, String fullName, String email) throws Exception {
        log.debug("createUser(name={}, fullName={}, email={})", name, fullName, email);
        User newSubject = new User();
        newSubject.setId(getNewId());
        newSubject.setName(name);
        newSubject.setKey("user:" + name);
        newSubject.setFullName(fullName);
        newSubject.setEmail(email);
        // Add user to the "everyone" group
        newSubject.setUserGroupRole(Subject.USERS_KEY, GroupRole.Reader);
        subjectCollection.insert(newSubject);

        User user = getUserByNameOrKey(name);
        if (user != null) {
            log.debug("Created user " + user.getKey());
            // If the user was created, make sure they have a workspace
            createWorkspace(user.getKey());
        } else {
            throw new Exception("Problem creating user " + name);
        }

        return user;
    }

    public Group createGroup(String name, String fullName) throws Exception {
        log.debug("createGroup(name={}, fullName={})", name, fullName);
        Group newSubject = new Group();
        newSubject.setId(getNewId());
        newSubject.setName(name);
        newSubject.setKey("group:" + name);
        newSubject.setFullName(fullName);
        subjectCollection.insert(newSubject);

        Group group = getGroupByNameOrKey(name);
        if (group != null) {
            log.info("Created group " + group.getKey());
            createWorkspace(group.getKey());
        } else {
            throw new Exception("Problem creating group " + name);
        }

        return group;
    }

    public void remove(Subject subject) throws Exception {
        log.debug("remove({})", subject);
        WriteResult result = subjectCollection.remove("{_id:#}", subject.getId());
        if (result.getN() != 1) {
            throw new IllegalStateException("Deleted " + result.getN() + " records instead of one: Subject#" + subject.getId());
        }
    }

    public void removeUser(String userNameOrKey) throws Exception {
        log.debug("removeUser(subjectNameOrKey)", userNameOrKey);
        Subject user = getUserByNameOrKey(userNameOrKey);
        if (user == null) throw new IllegalArgumentException("User not found: " + userNameOrKey);
        remove(user);
    }

    public void removeGroup(String groupNameOrKey) throws Exception {
        log.debug("removeGroup(subjectNameOrKey)", groupNameOrKey);
        Subject group = getGroupByNameOrKey(groupNameOrKey);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupNameOrKey);
        remove(group);
    }

    public void addUserToGroup(String userNameOrKey, String groupNameOrKey, GroupRole role) throws Exception {
        log.debug("addUserToGroup(user={}, group={})", userNameOrKey, groupNameOrKey);
        User user = getUserByNameOrKey(userNameOrKey);
        if (user == null) throw new IllegalArgumentException("User not found: " + userNameOrKey);
        Group group = getGroupByNameOrKey(groupNameOrKey);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupNameOrKey);

        UserGroupRole ugr = user.getRole(group.getKey());
        if (ugr != null) {
            if (ugr.getRole().equals(role)) {
                log.info("User " + userNameOrKey + " already has role " + role + " in group " + groupNameOrKey + ". Skipping add...");
            } else {
                ugr.setRole(role);
                subjectCollection.save(user);
                log.info("Set role for " + userNameOrKey + " to " + role + " in group " + groupNameOrKey);
            }
        } else {
            user.setUserGroupRole(group.getKey(), role);
            subjectCollection.save(user);
            log.info("Set role for " + userNameOrKey + " to " + role + " in group " + groupNameOrKey);
        }
    }

    public void removeUserFromGroup(String userNameOrKey, String groupNameOrKey) throws Exception {
        log.debug("removeUserFromGroup(user={}, group={})", userNameOrKey, groupNameOrKey);
        User user = getUserByNameOrKey(userNameOrKey);
        if (user == null) throw new IllegalArgumentException("User not found: " + userNameOrKey);
        Group group = getGroupByNameOrKey(groupNameOrKey);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupNameOrKey);

        boolean dirty = false;
        // Purge all roles for this group
        UserGroupRole ugr = null;
        while ((ugr = user.getRole(group.getKey())) != null) {
            user.getUserGroupRoles().remove(ugr);
            dirty = true;
        }

        if (dirty) {
            subjectCollection.save(user);
            log.info("Removed user " + userNameOrKey + " from group " + groupNameOrKey);
        } else {
            log.debug("User " + userNameOrKey + " does not belong to group " + groupNameOrKey + ". Skipping removal...");
        }
    }

    // Workspaces

    public void createWorkspace(String ownerKey) throws Exception {
        log.debug("createWorkspace({})", ownerKey);
        if (getDefaultWorkspace(ownerKey) != null) {
            log.info("User " + ownerKey + " already has at least one workspace, skipping creation step.");
            return;
        }
        Workspace workspace = new Workspace();
        workspace.setName(DomainConstants.NAME_DEFAULT_WORKSPACE);
        save(ownerKey, workspace);
        log.info("Created workspace (id=" + workspace.getId() + ") for " + ownerKey);
    }

    public List<Workspace> getWorkspaces(String subjectKey) {
        log.debug("getWorkspaces({})", subjectKey);
        Set<String> subjects = getReaderSet(subjectKey);
        List<Workspace> workspaces;
        if (subjects == null) {
            workspaces = toList(treeNodeCollection.find("{class:#}", Workspace.class.getName()).as(Workspace.class));
        } else {
            workspaces = toList(treeNodeCollection.find("{class:#,readers:{$in:#}}", Workspace.class.getName(), subjects).as(Workspace.class));
        }
        Collections.sort(workspaces, new DomainObjectComparator(subjectKey));
        return workspaces;
    }

    public Workspace getDefaultWorkspace(String subjectKey) {
        log.debug("getDefaultWorkspace({})", subjectKey);
        return treeNodeCollection.findOne("{class:#,ownerKey:#}", Workspace.class.getName(), subjectKey).as(Workspace.class);
    }

    // Preferences

    /**
     * Return all the preferences for a given subject.
     */
    public List<Preference> getPreferences(String subjectKey) {
        log.debug("getPreferences({})", subjectKey);
        return toList(preferenceCollection.find("{subjectKey:#}", subjectKey).as(Preference.class));
    }

    public List<Preference> getPreferences(String subjectKey, String category) throws Exception {
        log.debug("getPreferences({}, category={})", subjectKey, category);
        return toList(preferenceCollection.find("{subjectKey:#,category:#}", subjectKey, category).as(Preference.class));
    }

    public Preference getPreference(String subjectKey, String category, String key) {
        log.debug("getPreference({}, category={}, key={})", subjectKey, category, key);
        return preferenceCollection.findOne("{subjectKey:#,category:#,key:#}", subjectKey, category, key).as(Preference.class);
    }

    public Object getPreferenceValue(String subjectKey, String category, String key) {
        Preference preference = getPreference(subjectKey, category, key);
        if (preference == null) {
            return null;
        } else {
            return preference.getValue();
        }
    }

    public void setPreferenceValue(String subjectKey, String category, String key, Object value) throws Exception {
        Preference preference = getPreference(subjectKey, category, key);
        if (value == null) {
            if (preference == null) {
                log.warn("Cannot remove {}'s preference for {}:{} because it cannot be found", subjectKey, category, key);
            } else {
                // Null value means that the preference should be deleted
                preferenceCollection.remove("{_id:#,subjectKey:#}", preference.getId(), subjectKey);
            }
            return;
        } else {
            if (preference == null) {
                // Create a new preference
                preference = new Preference(subjectKey, category, key, value);
            } else {
                // Update existing preference
                preference.setValue(value);
            }
            save(subjectKey, preference);
        }
    }

    /**
     * Saves the given subject preference.
     *
     * @param subjectKey
     * @param preference
     * @return
     * @throws Exception
     */
    public Preference save(String subjectKey, Preference preference) throws Exception {

        log.debug("save({}, {})", subjectKey, preference);

        if (preference.getId() == null) {
            preference.setId(getNewId());
            preferenceCollection.insert(preference);
        } else {
            // The placeholder is important here. Without it, nulls would not be set (see https://github.com/bguerout/jongo/issues/231)
            WriteResult result = preferenceCollection.update("{_id:#,subjectKey:#}", preference.getId(), subjectKey).with("#", preference);
            if (result.getN() != 1) {
                throw new IllegalStateException("Updated " + result.getN() + " records instead of one: preference#" + preference.getId());
            }
        }

        log.trace("Saved " + preference.getClass().getName() + "#" + preference.getId());
        return preference;
    }

    /**
     * Returns any TreeNodes which reference the given object.
     *
     * @param domainObject
     * @return boolean
     * @throws Exception
     */
    public List<Reference> getContainerReferences(DomainObject domainObject) throws Exception {

        log.trace("Checking to see whether  " + domainObject.getId() + " has any parent references");
        if (domainObject == null || domainObject.getId() == null) {
            return null;
        }

        String refStr = Reference.createFor(domainObject).toString();
        List<Reference> refList = new ArrayList<>();

        // TODO: the node implementations should be decoupled from this method

        for (TreeNode item : treeNodeCollection.find("{children:#}", refStr).as(TreeNode.class)) {
            refList.add(Reference.createFor(item));
        }

        for (ColorDepthMask item : colorDepthMaskCollection.find("{children:#}", refStr).as(ColorDepthMask.class)) {
            refList.add(Reference.createFor(item));
        }

        return refList;
    }

    /**
     * Returns the count of TreeNodes which reference the given object.
     *
     * @param domainObject
     * @return
     * @throws Exception
     */
    public long getContainerReferenceCount(DomainObject domainObject) throws Exception {

        log.trace("Checking to see whether  " + domainObject.getId() + " has any parent references");
        if (domainObject == null || domainObject.getId() == null) {
            throw new IllegalArgumentException("DomainObject and its id must be not-null");
        }

        String refStr = Reference.createFor(domainObject).toString();
        return treeNodeCollection.count("{children:#}", refStr);
    }

    /**
     * Returns the count of TreeNodes which reference the given object.
     *
     * @param references
     * @return
     * @throws Exception
     */
    public long getContainerReferenceCount(Collection<Reference> references) throws Exception {

        List<String> refStrings = new ArrayList<>();
        for (Reference reference : references) {
            refStrings.add(reference.toString());
        }

        return treeNodeCollection.count("{children:{$in:#}}", refStrings);
    }

    public <T extends DomainObject> List<TreeNode> getContainers(String subjectKey, Collection<Reference> references) throws Exception {

        long start = System.currentTimeMillis();

        Set<String> subjects = getReaderSet(subjectKey);

        List<String> refStrings = new ArrayList<>();
        for (Reference reference : references) {
            refStrings.add(reference.toString());
        }

        MongoCursor<TreeNode> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = treeNodeCollection.find("{children:{$in:#}}", refStrings).as(TreeNode.class);
        } else {
            cursor = treeNodeCollection.find("{children:{$in:#},readers:{$in:#}}", refStrings, subjects).as(TreeNode.class);
        }

        List<TreeNode> list = toList(cursor);
        log.trace("Getting " + list.size() + " TreeNode objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    /**
     * Create a list of the result set in iteration order.
     */
    public <T> List<T> toList(MongoCursor<? extends T> cursor) {
        List<T> list = new ArrayList<>();
        for (T item : cursor) {
            list.add(item);
        }
        return list;
    }

    /**
     * Create a list of the result set in the order of the given id list. If ids is null then
     * return the result set in the order it comes back.
     */
    public <T extends DomainObject> List<T> toList(MongoCursor<T> cursor, Collection<Long> ids) {
        if (ids == null) {
            List<T> list = new ArrayList<>();
            for (T item : cursor) {
                list.add(item);
            }
            return list;
        }
        List<T> list = new ArrayList<>(ids.size());
        Map<Long, T> map = new HashMap<>(ids.size());
        for (T item : cursor) {
            map.put(item.getId(), item);
        }
        for (Long id : ids) {
            T item = map.get(id);
            if (item != null) {
                list.add(item);
            }
        }
        return list;
    }

    public <T extends DomainObject> void deleteDomainObject(String subjectKey, Class<T> domainClass, Long id) {
        deleteDomainObjects(subjectKey, domainClass, Arrays.asList(id));
    }

    public <T extends DomainObject> boolean deleteDomainObjects(String subjectKey, Class<T> domainClass, List<Long> ids) {

        Set<String> subjects = getWriterSet(subjectKey);
        String collectionName = DomainUtils.getCollectionName(domainClass);

        WriteResult wr = getCollectionByName(collectionName).remove("{_id:{$in:#},writers:{$in:#}}", ids, subjects);
        if (wr.getN() != ids.size()) {
            log.warn("Only deleted " + wr.getN() + " objects from " + ids);
            return false;
        }

        return true;
    }

    /**
     * Retrieve a refresh copy of the given domain object from the database.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> T getDomainObject(String subjectKey, T domainObject) {
        return (T) getDomainObject(subjectKey, domainObject.getClass(), domainObject.getId());
    }

    /**
     * Get the domain object referenced by the collection name and id.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> T getDomainObject(String subjectKey, Class<T> domainClass, Long id) {
        Reference reference = Reference.createFor(domainClass, id);
        return (T) getDomainObject(subjectKey, reference);
    }

    /**
     * Get the domain object referenced by the given Reference.
     */
    public DomainObject getDomainObject(String subjectKey, Reference reference) {
        List<DomainObject> objs = getDomainObjects(subjectKey, reference.getTargetClassName(), Arrays.asList(reference.getTargetId()));
        return objs.isEmpty() ? null : objs.get(0);
    }

    private DomainObject getDomainObject(String subjectKey, String className, Long id) {
        List<DomainObject> objs = getDomainObjects(subjectKey, className, Arrays.asList(id));
        return objs.isEmpty() ? null : objs.get(0);
    }

    public <T extends DomainObject> List<T> getDomainObjectsAs(List<Reference> references, Class<T> clazz) {
        return getDomainObjectsAs(null, references, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainObject> List<T> getDomainObjectsAs(String subjectKey, List<Reference> references, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (DomainObject object : getDomainObjects(subjectKey, references)) {
            if (clazz.isAssignableFrom(object.getClass())) {
                list.add((T) object);
            } else {
                log.warn("Referenced object is " + object.getClass().getSimpleName() + " not " + clazz.getSimpleName());
            }
        }
        return list;
    }

    public List<DomainObject> getDomainObjects(String subjectKey, List<Reference> references) {
        return getDomainObjects(subjectKey, references, false);
    }

    private Collection<DomainObject> getUserDomainObjects(String subjectKey, List<Reference> references) {
        return getDomainObjects(subjectKey, references, true);
    }

    /**
     * Get the domain objects referenced by the given list of References.
     */
    public List<DomainObject> getDomainObjects(String subjectKey, List<Reference> references, boolean ownedOnly) {

        List<DomainObject> domainObjects = new ArrayList<>();
        if (references == null || references.isEmpty()) {
            return domainObjects;
        }

        Multimap<String, Long> referenceMap = ArrayListMultimap.create();
        for (Reference reference : references) {
            if (reference == null) {
                log.warn("{} requested null reference", subjectKey);
                continue;
            }
            referenceMap.put(reference.getTargetClassName(), reference.getTargetId());
        }

        for (String className : referenceMap.keySet()) {
            List<DomainObject> objs = ownedOnly ?
                    getUserDomainObjects(subjectKey, className, referenceMap.get(className)) :
                    getDomainObjects(subjectKey, className, referenceMap.get(className));
            domainObjects.addAll(objs);
        }

        return domainObjects;
    }

    /**
     * Get the domain objects of a single class with the specified ids.
     *
     * @param subjectKey
     * @param className
     * @param ids
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> List<T> getDomainObjects(String subjectKey, String className, Collection<Long> ids) {
        Class<T> clazz;
        try {
            clazz = (Class<T>) DomainUtils.getObjectClassByName(className);
        } catch (IllegalArgumentException e) {
            log.error("Unknown domain object class: " + className);
            return new ArrayList<T>();
        }
        return getDomainObjects(subjectKey, clazz, ids);
    }

    public <T extends DomainObject> List<T> getDomainObjects(String subjectKey, Class<T> domainClass) {
        return getDomainObjects(subjectKey, domainClass, null);
    }

    /**
     * Get the domain objects in the given collection name with the specified ids.
     */
    public <T extends DomainObject> List<T> getDomainObjects(String subjectKey, Class<T> domainClass, Collection<Long> ids) {

        if (domainClass == null) return new ArrayList<>();

        log.debug("getDomainObjects({}, className={}, ids={})", subjectKey, domainClass.getSimpleName(), abbr(ids));

        long start = System.currentTimeMillis();

        Set<String> subjects = getReaderSet(subjectKey);
        String collectionName = DomainUtils.getCollectionName(domainClass);
        Set<String> classes = DomainUtils.getObjectClassNames(domainClass);

        MongoCursor<T> cursor;
        if (ids == null) {
            if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
                cursor = getCollectionByName(collectionName).find("{class:{$in:#}}", classes).as(domainClass);
            } else {
                cursor = getCollectionByName(collectionName).find("{class:{$in:#},readers:{$in:#}}", classes, subjects).as(domainClass);
            }
        } else {
            if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
                cursor = getCollectionByName(collectionName).find("{class:{$in:#},_id:{$in:#}}", classes, ids).as(domainClass);
            } else {
                cursor = getCollectionByName(collectionName).find("{class:{$in:#},_id:{$in:#},readers:{$in:#}}", classes, ids, subjects).as(domainClass);
            }
        }

        List<T> list = toList(cursor, ids);
        log.trace("Getting " + list.size() + " " + collectionName + " objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    public <T extends DomainObject> List<T> getUserDomainObjects(String subjectKey, Class<T> domainClass) {
        return getUserDomainObjects(subjectKey, domainClass, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainObject> List<T> getUserDomainObjects(String subjectKey, String className, Collection<Long> ids) {
        return (List<T>) getUserDomainObjects(subjectKey, DomainUtils.getObjectClassByName(className), ids);
    }

    /**
     * Get the domain objects owned by the given user, in the given collection name, with the specified ids.
     */
    public <T extends DomainObject> List<T> getUserDomainObjects(String subjectKey, Class<T> domainClass, Collection<Long> ids) {

        if (domainClass == null) return new ArrayList<>();

        log.debug("getUserDomainObjects({}, className={}, ids={})", subjectKey, domainClass.getSimpleName(), abbr(ids));

        long start = System.currentTimeMillis();

        String collectionName = DomainUtils.getCollectionName(domainClass);
        Set<String> classes = DomainUtils.getObjectClassNames(domainClass);
        MongoCursor<T> cursor;
        if (ids == null) {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#},ownerKey:#}", classes, subjectKey).as(domainClass);
        } else {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#},_id:{$in:#},ownerKey:#}", classes, ids, subjectKey).as(domainClass);
        }

        List<T> list = toList(cursor, ids);
        log.trace("Getting " + list.size() + " " + collectionName + " objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    /**
     * Get the domain objects referenced by the given reverse reference.
     *
     * @param subjectKey
     * @param reverseRef
     * @return
     */
    public List<DomainObject> getDomainObjects(String subjectKey, ReverseReference reverseRef) {

        log.debug("getDomainObjects({}, reverseRef={})", subjectKey, reverseRef);

        Set<String> subjects = getReaderSet(subjectKey);
        Class<? extends DomainObject> domainClass = DomainUtils.getObjectClassByName(reverseRef.getReferringClassName());
        String collectionName = DomainUtils.getCollectionName(reverseRef.getReferringClassName());
        Class<? extends DomainObject> baseClass = DomainUtils.getBaseClass(collectionName);
        Set<String> classes = DomainUtils.getObjectClassNames(baseClass);

        MongoCursor<? extends DomainObject> cursor = null;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#},'" + reverseRef.getReferenceAttr() + "':#}", classes, reverseRef.getReferenceId()).sort("{_id: 1}").as(domainClass);
        } else {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#},'" + reverseRef.getReferenceAttr() + "':#,readers:{$in:#}}", classes, reverseRef.getReferenceId(), subjects).sort("{_id: 1}").as(domainClass);
        }

        List<DomainObject> list = toList(cursor);
        if (list.size() != reverseRef.getCount()) {
            log.warn("Reverse reference (" + reverseRef.getReferringClassName() + ":" + reverseRef.getReferenceAttr() + ":" + reverseRef.getReferenceId()
                    + ") denormalized count (" + reverseRef.getCount() + ") does not match actual count (" + list.size() + ")");
        }
        return list;
    }

    /**
     * Get the domain object by name.
     */
    public <T extends DomainObject> List<T> getDomainObjectsByName(String subjectKey, Class<T> domainClass, String name) {

        if (domainClass == null) return null;

        log.debug("getDomainObjectsByName({}, className={}, name={})", subjectKey, domainClass.getSimpleName(), name);

        long start = System.currentTimeMillis();

        Set<String> subjects = getReaderSet(subjectKey);

        String collectionName = DomainUtils.getCollectionName(domainClass);
        Set<String> classes = DomainUtils.getObjectClassNames(domainClass);
        MongoCursor<T> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#},name:#}", classes, name).as(domainClass);
        } else {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#},name:#,readers:{$in:#}}", classes, name, subjects).as(domainClass);
        }

        List<T> list = toList(cursor);
        log.trace("Getting " + list.size() + " " + collectionName + " objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    /**
     * Get the domain object by name.
     */
    public <T extends DomainObject> List<T> getUserDomainObjectsByName(String subjectKey, Class<T> domainClass, String name) {

        if (domainClass == null || name == null) {
            return null;
        }

        long start = System.currentTimeMillis();
        log.debug("getUserDomainObjectsByName({}, className={}, name={})", subjectKey, domainClass.getSimpleName(), name);

        String collectionName = DomainUtils.getCollectionName(domainClass);
        Set<String> classes = DomainUtils.getObjectClassNames(domainClass);

        MongoCursor<T> cursor = getCollectionByName(collectionName).find("{class:{$in:#},ownerKey:#,name:#}", classes, subjectKey, name).as(domainClass);

        List<T> list = toList(cursor);
        log.trace("Getting " + list.size() + " " + collectionName + " objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    /**
     * Get domain objects of a given type with a given specified property value.
     */
    public <T extends DomainObject> List<T> getDomainObjectsWithProperty(String subjectKey, Class<T> domainClass, String propName, String propValue) {

        if (domainClass == null) {
            return null;
        }

        long start = System.currentTimeMillis();
        log.debug("getDomainObjectsWithProperty({}, className={}, name={}, value={})", subjectKey, domainClass.getSimpleName(), propName, propValue);

        Set<String> subjects = getReaderSet(subjectKey);

        String collectionName = DomainUtils.getCollectionName(domainClass);
        Set<String> classes = DomainUtils.getObjectClassNames(domainClass);

        MongoCursor<T> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#}," + propName + ":#}", classes, propValue).as(domainClass);
        } else {
            cursor = getCollectionByName(collectionName).find("{class:{$in:#}," + propName + ":#,readers:{$in:#}}", classes, propValue, subjects).as(domainClass);
        }

        List<T> list = toList(cursor);
        log.trace("Getting " + list.size() + " " + collectionName + " objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    public List<Annotation> getAnnotations(String subjectKey, Reference reference) {
        return getAnnotations(subjectKey, Arrays.asList(reference));
    }

    public List<Annotation> getAnnotations(String subjectKey, Collection<Reference> references) {
        log.debug("getAnnotations({}, references={})", subjectKey, abbr(references));
        Set<String> subjects = getReaderSet(subjectKey);

        List<String> targetRefs = new ArrayList<>();
        for (Reference reference : references) {
            targetRefs.add(reference.toString());
        }

        MongoCursor<Annotation> cursor = null;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = annotationCollection.find("{target:{$in:#}}", targetRefs).as(Annotation.class);
        } else {
            cursor = annotationCollection.find("{target:{$in:#},readers:{$in:#}}", targetRefs, subjects).as(Annotation.class);
        }

        return toList(cursor);
    }

    public List<Ontology> getOntologies(String subjectKey) {
        log.debug("getOntologies({})", subjectKey);
        Set<String> subjects = getReaderSet(subjectKey);
        List<Ontology> ontologies = toList(ontologyCollection.find("{readers:{$in:#}}", subjects).as(Ontology.class));
        Collections.sort(ontologies, new DomainObjectComparator(subjectKey));
        return ontologies;
    }

    public OntologyTerm getErrorOntologyCategory() {
        // TODO: this needs to be exposed to the client
        log.debug("getErrorOntologyCategory()");
        List<Ontology> ontologies = getDomainObjectsByName(DomainConstants.GENERAL_USER_GROUP_KEY, Ontology.class, DomainConstants.ERROR_ONTOLOGY_NAME);
        if (ontologies.size() > 1) {
            log.warn("Multiple error ontologies detected. Please ensure that " + DomainConstants.GENERAL_USER_GROUP_KEY + " only owns a single ontology with name " + DomainConstants.ERROR_ONTOLOGY_NAME);
        }
        for (Ontology ontology : ontologies) {
            OntologyTerm term = ontology.findTerm(DomainConstants.ERROR_ONTOLOGY_CATEGORY);
            if (term instanceof Category) {
                return term;
            }
        }
        throw new IllegalStateException("Error ontology category could not be found");
    }

    public Annotation createAnnotation(String subjectKey, Reference target, OntologyTermReference ontologyTermReference, Object value) throws Exception {

        log.debug("createAnnotation({}, target={}, ontologyTerm={}, value={})", subjectKey, target, ontologyTermReference, value);

        Ontology ontology = getDomainObject(subjectKey, Ontology.class, ontologyTermReference.getOntologyId());
        OntologyTerm ontologyTerm = ontology.findTerm(ontologyTermReference.getOntologyTermId());

        OntologyTerm keyTerm = ontologyTerm;
        OntologyTerm valueTerm = null;
        String keyString = keyTerm.getName();
        String valueString = value == null ? null : value.toString();

        if (keyTerm instanceof EnumItem) {
            keyTerm = ontologyTerm.getParent();
            valueTerm = ontologyTerm;
            keyString = keyTerm.getName();
            valueString = valueTerm.getName();
        }

        final Annotation annotation = new Annotation();
        annotation.setKey(keyString);
        annotation.setValue(valueString);
        annotation.setTarget(target);

        annotation.setKeyTerm(new OntologyTermReference(ontology, keyTerm));
        if (valueTerm != null) {
            annotation.setValueTerm(new OntologyTermReference(ontology, valueTerm));
        }

        String tag = (annotation.getValue() == null ? annotation.getKey()
                : annotation.getKey() + " = " + annotation.getValue());
        annotation.setName(tag);

        Annotation savedAnnotation = save(subjectKey, annotation);
        log.trace("Saved annotation as " + savedAnnotation.getId());

        // TODO: auto-share annotation based on auto-share template (this logic is currently in the client)
        return savedAnnotation;
    }

    // Data sets

    public List<DataSet> getDataSets() {
        return getDataSets(null);
    }

    public List<DataSet> getDataSets(String subjectKey) {
        log.debug("getDataSets({})", subjectKey);
        Set<String> subjects = getReaderSet(subjectKey);
        List<DataSet> dataSets;
        if (subjects == null) {
            dataSets = toList(dataSetCollection.find().as(DataSet.class));
        } else {
            dataSets = toList(dataSetCollection.find("{readers:{$in:#}}", subjects).as(DataSet.class));
        }
        Collections.sort(dataSets, new DomainObjectComparator(subjectKey));
        return dataSets;
    }

    public List<DataSet> getUserDataSets(String subjectKey) {
        log.debug("getUserDataSets({})", subjectKey);
        if (subjectKey == null) {
            return toList(dataSetCollection.find().as(DataSet.class));
        } else {
            return toList(dataSetCollection.find("{ownerKey:#}", subjectKey).as(DataSet.class));
        }
    }

    public DataSet getDataSetByIdentifier(String subjectKey, String dataSetIdentifier) {

        log.debug("getDataSetByIdentifier({}, dataSetIdentifier={})", subjectKey, dataSetIdentifier);

        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return dataSetCollection.findOne("{identifier:#}", dataSetIdentifier).as(DataSet.class);
        } else {
            return dataSetCollection.findOne("{readers:{$in:#},identifier:#}", subjects, dataSetIdentifier).as(DataSet.class);
        }
    }

    public List<DataSet> getDataSetsWithColorDepthImages(String subjectKey, String alignmentSpace) {
        // subjectKey is ignored, because all users can known about the existence of all data sets
        List<DataSet> dataSets = toList(dataSetCollection.find("{'colorDepthCounts." + alignmentSpace + "':{$exists:1}}").as(DataSet.class));
        Collections.sort(dataSets, new DomainObjectComparator(subjectKey));
        return dataSets;
    }

    public DataSet createDataSet(String subjectKey, DataSet dataSet) throws Exception {

        DataSet saved = save(subjectKey, dataSet);

        String filterName = dataSet.getName();
        log.info("Creating data set filter for " + filterName + ", shared with " + subjectKey);
        Filter filter = createDataSetFilter(subjectKey, dataSet, filterName);

        // Now add it to the owner's Data Sets folder

        TreeNode sharedDataFolder = getOrCreateDefaultFolder(subjectKey, DomainConstants.NAME_DATA_SETS);
        addChildren(subjectKey, sharedDataFolder, Arrays.asList(Reference.createFor(filter)));

        return saved;
    }

    // Sample locks

    /**
     * Attempts to lock a sample for the given task id an owner. The caller must check the return value of this method. If null is returned,
     * then the sample could not be locked. Only if a non-null SampleLock is returned can the sample be considered locked.
     *
     * @param subjectKey
     * @param sampleId
     * @param taskId
     * @param description
     * @return
     */
    public SampleLock lockSample(String subjectKey, Long sampleId, Long taskId, String description) {

        Reference ref = Reference.createFor(Sample.class, sampleId);

        // First attempt to refresh an existing lock (reentrant lock)
        WriteResult result = sampleLockCollection
                .update("{ownerKey:#, taskId:#, sampleRef:#}", subjectKey, taskId, ref.toString())
                .with("{$currentDate:{'creationDate':true}}");

        if (result.getN() < 1) {
            // Nothing was updated

            // If there's no existing lock, then create a new one
            SampleLock lock = new SampleLock();
            lock.setCreationDate(new Date());
            lock.setOwnerKey(subjectKey);
            lock.setTaskId(taskId);
            lock.setSampleRef(ref);
            lock.setDescription(description);

            try {
                sampleLockCollection.insert(lock);
                log.debug("Task {} ({}) has locked sample {}", taskId, subjectKey, sampleId);
                return lock;
            } catch (DuplicateKeyException e) {
                log.error("Task {} ({}) tried to lock {} and failed", taskId, subjectKey, sampleId);
                return null;
            }
        } else {
            // Lock was updated, now let's fetch it and return it
            SampleLock lock = sampleLockCollection
                    .findOne("{ownerKey:#, taskId:#, sampleRef:#}", subjectKey, taskId, ref.toString())
                    .as(SampleLock.class);
            if (lock == null) {
                log.error("Task {} ({}) reconfirmed lock on sample {}, but it cannot be found.", taskId, subjectKey, sampleId);
            } else {
                log.debug("Task {} ({}) reconfirmed lock on sample {}", taskId, subjectKey, sampleId);

            }
            return lock;
        }
    }

    /**
     * Attempts to unlock a sample, given the lock holder's task id and owner.
     *
     * @param subjectKey
     * @param sampleId
     * @param taskId
     * @return
     */
    public boolean unlockSample(String subjectKey, Long sampleId, Long taskId) {

        Reference ref = Reference.createFor(Sample.class, sampleId);
        WriteResult result = sampleLockCollection
                .remove("{ownerKey:#, taskId:#, sampleRef:#}", subjectKey, taskId, ref.toString());

        if (result.getN() != 1) {

            SampleLock lock = sampleLockCollection
                    .findOne("{sampleRef:#}", ref.toString()).as(SampleLock.class);
            if (lock == null) {
                log.error("Task {} ({}) tried to remove lock on {} and failed. "
                        + "It looks like the lock may have expired.", taskId, subjectKey, sampleId);
            } else {
                log.error("Task {} ({}) tried to remove lock on {} and failed. "
                        + "It looks like the lock is owned by someone else: {}.", taskId, subjectKey, sampleId, lock);
            }

            return false;
        }

        log.debug("Task {} ({}) removed lock on {}", taskId, subjectKey, sampleId);
        return true;
    }

    // Samples by data set

    public List<Sample> getActiveSamplesByDataSet(String subjectKey, String dataSetIdentifier) {
        return getSamplesByDataSet(subjectKey, dataSetIdentifier, true);
    }

    public List<Sample> getSamplesByDataSet(String subjectKey, String dataSetIdentifier) {
        return getSamplesByDataSet(subjectKey, dataSetIdentifier, false);
    }

    private List<Sample> getSamplesByDataSet(String subjectKey, String dataSetIdentifier, boolean activeOnly) {

        log.debug("getActiveSamplesByDataSet({}, dataSetIdentifier={})", subjectKey, dataSetIdentifier);

        long start = System.currentTimeMillis();
        Set<String> subjects = getReaderSet(subjectKey);

        MongoCursor<Sample> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = sampleCollection.find("{dataSet:#" + (activeOnly ? ",sageSynced:true" : "") + "}", dataSetIdentifier).as(Sample.class);
        } else {
            cursor = sampleCollection.find("{dataSet:#" + (activeOnly ? ",sageSynced:true" : "") + ",readers:{$in:#}}", dataSetIdentifier, subjects).as(Sample.class);
        }

        List<Sample> list = toList(cursor);
        log.trace("Getting " + list.size() + " Sample objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    // LSMSs by data set

    public List<LSMImage> getActiveLsmsByDataSet(String subjectKey, String dataSetIdentifier) {
        return getLsmsByDataSet(subjectKey, dataSetIdentifier, true);
    }

    public List<LSMImage> getLsmsByDataSet(String subjectKey, String dataSetIdentifier) {
        return getLsmsByDataSet(subjectKey, dataSetIdentifier, false);
    }

    private List<LSMImage> getLsmsByDataSet(String subjectKey, String dataSetIdentifier, boolean activeOnly) {

        log.debug("getActiveLsmsByDataSet({}, dataSetIdentifier={})", subjectKey, dataSetIdentifier);

        long start = System.currentTimeMillis();
        Set<String> subjects = getReaderSet(subjectKey);

        MongoCursor<LSMImage> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = imageCollection.find("{dataSet:#" + (activeOnly ? ",sageSynced:true" : "") + "}", dataSetIdentifier).as(LSMImage.class);
        } else {
            cursor = imageCollection.find("{dataSet:#" + (activeOnly ? ",sageSynced:true" : "") + ",readers:{$in:#}}", dataSetIdentifier, subjects).as(LSMImage.class);
        }

        List<LSMImage> list = toList(cursor);
        log.trace("Getting " + list.size() + " LSMImage objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    // LSMSs by slide code

    public List<LSMImage> getActiveLsmsBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode) {
        return getLsmsBySlideCode(subjectKey, dataSetIdentifier, slideCode, true);
    }

    public List<LSMImage> getLsmsBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode) {
        return getLsmsBySlideCode(subjectKey, dataSetIdentifier, slideCode, false);
    }

    /**
     * Returns the LSMs associated with a given slide code.
     *
     * @param subjectKey        authorization key; if null system level privileges are assumed
     * @param dataSetIdentifier data set filter, if null returns all LSMs across multiple data sets
     * @param slideCode         required parameter
     * @param activeOnly        if true, only those LSMs with sageSynced=true are returned
     * @return list of matching LSM images
     */
    private List<LSMImage> getLsmsBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode, boolean activeOnly) {

        log.debug("getActiveLsmsBySlideCode({}, dataSetIdentifier={}, slideCode={})", subjectKey, dataSetIdentifier, slideCode);

        long start = System.currentTimeMillis();
        Set<String> subjects = getReaderSet(subjectKey);
        String dataSetFilter = dataSetIdentifier == null ? "" : "dataSet:'" + dataSetIdentifier + "', ";

        MongoCursor<LSMImage> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = imageCollection.find("{" + dataSetFilter + "slideCode:#" + (activeOnly ? ",sageSynced:true" : "") + "}", slideCode).as(LSMImage.class);
        } else {
            cursor = imageCollection.find("{" + dataSetFilter + "slideCode:#" + (activeOnly ? ",sageSynced:true" : "") + ",readers:{$in:#}}", slideCode, subjects).as(LSMImage.class);
        }

        List<LSMImage> list = toList(cursor);
        log.trace("Getting " + list.size() + " LSMImage objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    // Samples by slide code

    public List<Sample> getSamplesBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode) {
        return getSamplesBySlideCode(subjectKey, dataSetIdentifier, slideCode, false);
    }

    public Sample getActiveSampleBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode) {
        List<Sample> list = getSamplesBySlideCode(subjectKey, dataSetIdentifier, slideCode, true);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            log.warn("More than one active sample found for " + dataSetIdentifier + "/" + slideCode);
        }
        return list.get(0);
    }

    /**
     * Same as getLsmsBySlideCode, but for Samples.
     */
    private List<Sample> getSamplesBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode, boolean activeOnly) {

        log.debug("getActiveSampleBySlideCode({}, dataSetIdentifier={}, slideCode={})", subjectKey, dataSetIdentifier, slideCode);

        long start = System.currentTimeMillis();
        Set<String> subjects = getReaderSet(subjectKey);
        String dataSetFilter = dataSetIdentifier == null ? "" : "dataSet:'" + dataSetIdentifier + "', ";

        MongoCursor<Sample> cursor;
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            cursor = sampleCollection.find("{" + dataSetFilter + "slideCode:#" + (activeOnly ? ",sageSynced:true" : "") + "}", slideCode).as(Sample.class);
        } else {
            cursor = sampleCollection.find("{" + dataSetFilter + "slideCode:#" + (activeOnly ? ",sageSynced:true" : "") + ",readers:{$in:#}}", slideCode, subjects).as(Sample.class);
        }

        List<Sample> list = toList(cursor);
        log.trace("Getting " + list.size() + " Sample objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    // User samples by slide code

    public List<Sample> getUserSamplesBySlideCode(String subjectKey, String dataSetIdentifier, String slideCode) {

        log.debug("getUserSamplesBySlideCode({}, dataSetIdentifier={}, slideCode={})", subjectKey, dataSetIdentifier, slideCode);

        long start = System.currentTimeMillis();

        MongoCursor<Sample> cursor;
        if (subjectKey == null) {
            cursor = sampleCollection.find("{dataSet:#,slideCode:#}", dataSetIdentifier, slideCode).as(Sample.class);
        } else {
            cursor = sampleCollection.find("{dataSet:#,slideCode:#,ownerKey:#}", dataSetIdentifier, slideCode, subjectKey).as(Sample.class);
        }

        List<Sample> list = toList(cursor);
        log.trace("Getting " + list.size() + " Sample objects took " + (System.currentTimeMillis() - start) + " ms");
        return list;
    }

    public List<LSMImage> getActiveLsmsBySampleId(String subjectKey, Long sampleId) {
        log.debug("getActiveLsmsBySampleId({}, {})", subjectKey, sampleId);
        String refStr = "Sample#" + sampleId;
        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return toList(imageCollection.find("{sampleRef:#,sageSynced:true}", refStr).as(LSMImage.class));
        } else {
            return toList(imageCollection.find("{sampleRef:#,sageSynced:true,readers:{$in:#}}", refStr, subjects).as(LSMImage.class));
        }
    }

    public List<LSMImage> getInactiveLsmsBySampleId(String subjectKey, Long sampleId) {
        log.debug("getInactiveLsmsBySampleId({}, {})", subjectKey, sampleId);
        String refStr = "Sample#" + sampleId;
        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return toList(imageCollection.find("{sampleRef:#,sageSynced:false}", refStr).as(LSMImage.class));
        } else {
            return toList(imageCollection.find("{sampleRef:#,sageSynced:false,readers:{$in:#}}", refStr, subjects).as(LSMImage.class));
        }
    }

    public List<LSMImage> getAllLsmsBySampleId(String subjectKey, Long sampleId) {
        log.debug("getAllLsmsBySampleId({}, {})", subjectKey, sampleId);
        String refStr = "Sample#" + sampleId;
        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return toList(imageCollection.find("{sampleRef:#}", refStr).as(LSMImage.class));
        } else {
            return toList(imageCollection.find("{sampleRef:#,readers:{$in:#}}", refStr, subjects).as(LSMImage.class));
        }
    }

    public LSMImage getActiveLsmBySageId(String subjectKey, Integer sageId) {
        log.debug("getActiveLsmBySageId({}, {})", subjectKey, sageId);
        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return imageCollection.findOne("{sageId:#,sageSynced:true}", sageId).as(LSMImage.class);
        } else {
            return imageCollection.findOne("{sageId:#,sageSynced:true,readers:{$in:#}}", sageId, subjects).as(LSMImage.class);
        }
    }

    public List<LSMImage> getUserLsmsBySageId(String subjectKey, Integer sageId) {
        log.debug("getUserLsmsBySageId({}, {})", subjectKey, sageId);
        if (subjectKey == null) {
            return toList(imageCollection.find("{sageId:#}", sageId).as(LSMImage.class));
        } else {
            return toList(imageCollection.find("{sageId:#, ownerKey:#}", sageId, subjectKey).as(LSMImage.class));
        }
    }

    public List<NeuronFragment> getNeuronFragmentsBySampleId(String subjectKey, Long sampleId) {
        log.debug("getNeuronFragmentsBySampleId({}, {})", subjectKey, sampleId);
        String refStr = "Sample#" + sampleId;
        if (subjectKey == null) {
            return toList(fragmentCollection.find("{sampleRef:#}", refStr).as(NeuronFragment.class));
        } else {
            Set<String> subjects = getReaderSet(subjectKey);
            return toList(fragmentCollection.find("{sampleRef:#, readers:{$in:#}}", refStr, subjects).as(NeuronFragment.class));
        }
    }

    public List<NeuronFragment> getNeuronFragmentsBySeparationId(String subjectKey, Long separationId) {
        log.debug("getNeuronFragmentsBySeparationId({}, {})", subjectKey, separationId);
        if (subjectKey == null) {
            return toList(fragmentCollection.find("{separationId:#}", separationId).as(NeuronFragment.class));
        } else {
            Set<String> subjects = getReaderSet(subjectKey);
            return toList(fragmentCollection.find("{separationId:#, readers:{$in:#}}", separationId, subjects).as(NeuronFragment.class));
        }
    }

    public Sample getSampleBySeparationId(String subjectKey, Long separationId) {
        log.debug("getSampleBySeparationId({}, {})", subjectKey, separationId);
        if (subjectKey == null) {
            return sampleCollection.findOne("{objectiveSamples.pipelineRuns.results.results.id:#}", separationId).as(Sample.class);
        } else {
            Set<String> subjects = getReaderSet(subjectKey);
            return sampleCollection.findOne("{objectiveSamples.pipelineRuns.results.results.id:#, readers:{$in:#}}", separationId, subjects).as(Sample.class);
        }
    }

    public NeuronSeparation getNeuronSeparation(String subjectKey, Long separationId) throws Exception {
        log.debug("getNeuronSeparation({}, {})", subjectKey, separationId);
        Set<String> subjects = getReaderSet(subjectKey);
        // TODO: match subject set to ensure user has read permission
        Aggregate.ResultsIterator<NeuronSeparation> results = sampleCollection.aggregate("{$match: {\"objectiveSamples.pipelineRuns.results.results.id\": " + separationId + "}}")
                .and("{$unwind: \"$objectiveSamples\"}")
                .and("{$unwind: \"$objectiveSamples.pipelineRuns\"}")
                .and("{$unwind: \"$objectiveSamples.pipelineRuns.results\"}")
                .and("{$unwind: \"$objectiveSamples.pipelineRuns.results.results\"}")
                .and("{$match: {\"objectiveSamples.pipelineRuns.results.results.id\": " + separationId + "}}")
                .and("{$project: {class : \"$objectiveSamples.pipelineRuns.results.results.class\" ," +
                        "id : \"$objectiveSamples.pipelineRuns.results.results.id\"," +
                        "name : \"$objectiveSamples.pipelineRuns.results.results.name\"," +
                        "filepath : \"$objectiveSamples.pipelineRuns.results.results.filepath\"," +
                        "creationDate : \"$objectiveSamples.pipelineRuns.results.results.creationDate\"," +
                        "fragments : \"$objectiveSamples.pipelineRuns.results.results.fragments\"," +
                        "hasWeights : \"$objectiveSamples.pipelineRuns.results.results.hasWeights\"}}")
                .as(NeuronSeparation.class);
        if (results.hasNext()) {
            return results.next();
        }
        return null;
    }

    public List<Subject> getMembersByGroupKey(String groupKey) {
        log.debug("getMembersByGroupId({})", groupKey);
        String refstr = "group:" + groupKey;
        return toList(subjectCollection.find("{userGroupRoles.groupKey:#}", refstr).as(Subject.class));
    }

    public List<Sample> getSamplesByDataSet(String dataset, int pageNumber, int pageSize, String sortBy) {
        log.debug("getSamplesByDataSet({})", dataset);
        List<Sample> samples = toList(sampleCollection.find("{dataSet:#}", dataset).sort("{" + sortBy + ":1}").skip(pageSize * (pageNumber - 1)).limit(pageSize).as(Sample.class));
        return samples;
    }

    public boolean isAdmin(String subjectKey) {
        log.debug("isAdmin({})", subjectKey);
        return subjectCollection.count("{userGroupRoles.groupKey:'group:admin', key:#}", subjectKey) != 0;
    }

    public List<Sample> getRecentSamples(String subjectKey) {
        log.debug("getRecentSamples({})");
        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return toList(sampleCollection.find().sort("{creationDate: -1}").limit(100).as(Sample.class));
        } else {
            return toList(sampleCollection.find("{readers:{$in:#}}", subjects).sort("{creationDate: -1}").limit(100).as(Sample.class));
        }
    }

    public Map<Subject, Integer> getGroupNames() {
        log.debug("getGroupNames");
        Map<Subject, Integer> hmap = new HashMap<>();
        List<Subject> c = subjectCollection.distinct("userGroupRoles.groupKey").as(Subject.class);
        for (int i = 0; i < c.size(); i++) {
            Integer count = subjectCollection.find("{userGroupRoles.groupKey:#}", c.get(i)).as(Subject.class).count();
            hmap.put(c.get(i), count);
        }
        return hmap;
    }

    public Map<String, String> getDataSetsByGroupName(String groupName) {
        log.debug("getDatasets({})", groupName);
        String refstr = "group:" + groupName;
        Map<String, String> hmap = new HashMap<>();

        for (DataSet dataSet : getDomainObjects(refstr, DataSet.class)) {
            if (groupName.equals("admin")) {
                hmap.put(dataSet.getName(), "admin");
            } else {
                String owner = dataSet.getOwnerKey();
                Set<String> writers = dataSet.getWriters();
                Set<String> readers = dataSet.getReaders();

                if (owner.contains(refstr)) {
                    hmap.put(dataSet.getName(), "Owner");
                } else if (writers.contains(refstr)) {
                    hmap.put(dataSet.getName(), "Writer");
                } else if (readers.contains(refstr)) {
                    hmap.put(dataSet.getName(), "Reader");
                }
            }
        }
        return hmap;
    }

    /**
     * Create the given object, with the given id. Dangerous to use if you don't know what you're doing! Use save() instead.
     *
     * @param subjectKey
     * @param domainObject
     * @return
     * @throws Exception
     */
    public <T extends DomainObject> T createWithPrepopulatedId(String subjectKey, T domainObject) throws Exception {
        String collectionName = DomainUtils.getCollectionName(domainObject);
        MongoCollection collection = getCollectionByName(collectionName);
        try {
            Date now = new Date();
            domainObject.setOwnerKey(subjectKey);
            domainObject.getReaders().add(subjectKey);
            domainObject.getWriters().add(subjectKey);
            domainObject.setCreationDate(now);
            domainObject.setUpdatedDate(now);
            collection.save(domainObject);
            log.trace("Created new object " + domainObject);
            return domainObject;
        } catch (MongoException e) {
            throw new Exception(e);
        }
    }

    private <T extends DomainObject> T saveImpl(String subjectKey, T domainObject) throws Exception {
        String collectionName = DomainUtils.getCollectionName(domainObject);
        MongoCollection collection = getCollectionByName(collectionName);
        try {
            Date now = new Date();
            if (domainObject.getId() == null) {
                domainObject.setId(getNewId());
                domainObject.setOwnerKey(subjectKey);
                domainObject.getReaders().add(subjectKey);
                domainObject.getWriters().add(subjectKey);
                domainObject.setCreationDate(now);
                domainObject.setUpdatedDate(now);
                collection.save(domainObject);
                log.trace("Created new object " + domainObject);
            } else {
                Set<String> subjects = getWriterSet(subjectKey);
                domainObject.setUpdatedDate(now);

                // The placeholder is important here. Without it, nulls would not be set (see https://github.com/bguerout/jongo/issues/231)
                WriteResult result = collection.update("{_id:#,writers:{$in:#}}", domainObject.getId(), subjects).with("#", domainObject);

                if (result.getN() != 1) {
                    throw new IllegalStateException("Updated " + result.getN() + " records instead of one: " + collectionName + "#" + domainObject.getId());
                }
                log.trace("Updated " + result.getN() + " rows for " + domainObject);
            }
            log.trace("Saved " + domainObject);
            return domainObject;
        } catch (MongoException e) {
            throw new Exception(e);
        }
    }

    /**
     * Saves the given object and returns a saved copy.
     *
     * @param subjectKey   The subject saving the object. If this is a new object, then this subject becomes the owner of the new object.
     * @param domainObject The object to be saved. If the id is not set, then a new object is created.
     * @return a copy of the saved object
     * @throws Exception
     */
    public <T extends DomainObject> T save(String subjectKey, T domainObject) throws Exception {

        log.debug("save({}, {})", subjectKey, Reference.createFor(domainObject));
        return saveImpl(subjectKey, domainObject);
    }

    public boolean remove(String subjectKey, DomainObject domainObject) throws Exception {

        String collectionName = DomainUtils.getCollectionName(domainObject);
        MongoCollection collection = getCollectionByName(collectionName);

        log.debug("remove({}, {})", subjectKey, Reference.createFor(domainObject));

        Set<String> subjects = getWriterSet(subjectKey);

        WriteResult result;
        if (subjects == null) {
            result = collection.remove("{_id:#}", domainObject.getId());
        } else {
            result = collection.remove("{_id:#,writers:{$in:#}}", domainObject.getId(), subjects);
        }

        if (result.getN() != 1) {
            throw new IllegalStateException("Deleted " + result.getN() + " records instead of one: " + collectionName + "#" + domainObject.getId());
        }

        // TODO: remove dependent objects?
        return result.getN() > 0;
    }

    public Ontology reorderTerms(String subjectKey, Long ontologyId, Long parentTermId, int[] order) throws Exception {

        Ontology ontology = getDomainObject(subjectKey, Ontology.class, ontologyId);
        if (ontology == null) {
            throw new IllegalArgumentException("Ontology not found: " + ontologyId);
        }
        OntologyTerm parent = ontology.findTerm(parentTermId);
        if (parent == null) {
            throw new IllegalArgumentException("Term not found: " + parentTermId);
        }

        log.debug("reorderTerms({}, ontologyId={}, parentTermId={}, order={})", subjectKey, ontologyId, parentTermId, order);

        List<OntologyTerm> childTerms = new ArrayList<>(parent.getTerms());

        if (log.isTraceEnabled()) {
            log.trace("{} has the following terms: ", parent.getName());
            for (OntologyTerm term : childTerms) {
                log.trace("  {}", term.getId());
            }
            log.trace("They should be put in this ordering: ");
            for (int i = 0; i < order.length; i++) {
                log.trace("  {} -> {}", i, order[i]);
            }
        }

        int originalSize = childTerms.size();
        OntologyTerm[] reordered = new OntologyTerm[childTerms.size()];
        for (int i = 0; i < order.length; i++) {
            int j = order[i];
            reordered[j] = childTerms.get(i);
            childTerms.set(i, null);
        }

        parent.getTerms().clear();
        for (OntologyTerm ref : reordered) {
            parent.getTerms().add(ref);
        }
        for (OntologyTerm term : childTerms) {
            if (term != null) {
                log.warn("Adding broken term " + term.getId() + " at the end");
                parent.getTerms().add(term);
            }
        }

        if (childTerms.size() != originalSize) {
            throw new IllegalStateException("Reordered children have new size " + childTerms.size() + " (was " + originalSize + ")");
        }

        log.trace("Reordering children of ontology term '{}'", parent.getName());
        saveImpl(subjectKey, ontology);
        return getDomainObject(subjectKey, ontology);
    }

    public Ontology addTerms(String subjectKey, Long ontologyId, Long parentTermId, Collection<OntologyTerm> terms, Integer index) throws Exception {

        if (terms == null) {
            throw new IllegalArgumentException("Cannot add null children");
        }
        Ontology ontology = getDomainObject(subjectKey, Ontology.class, ontologyId);
        if (ontology == null) {
            throw new IllegalArgumentException("Ontology not found: " + ontologyId);
        }
        OntologyTerm parent = ontology.findTerm(parentTermId);
        if (parent == null) {
            throw new IllegalArgumentException("Term not found: " + parentTermId);
        }

        log.debug("addTerms({}, ontologyId={}, parentTermId={}, terms={}, index={})", subjectKey, ontologyId, parentTermId, abbr(terms), index);

        int i = 0;
        for (OntologyTerm childTerm : terms) {
            if (childTerm.getId() == null) {
                childTerm.setId(getNewId());
            }
            if (index != null) {
                parent.insertChild(index + i, childTerm);
            } else {
                parent.addChild(childTerm);
            }
            i++;
        }

        log.trace("Adding " + terms.size() + " terms to " + parent.getName());
        saveImpl(subjectKey, ontology);
        return getDomainObject(subjectKey, ontology);
    }

    public Ontology removeTerm(String subjectKey, Long ontologyId, Long parentTermId, Long termId) throws Exception {

        Ontology ontology = getDomainObject(subjectKey, Ontology.class, ontologyId);
        if (ontology == null) {
            throw new IllegalArgumentException("Ontology not found: " + ontologyId);
        }
        OntologyTerm parent = ontology.findTerm(parentTermId);
        if (parent.getTerms() == null) {
            throw new Exception("Term has no children: " + parentTermId);
        }

        log.debug("removeTerm({}, ontologyId={}, parentTermId={}, termId={})", subjectKey, ontologyId, parentTermId, termId);

        OntologyTerm removed = null;
        for (Iterator<OntologyTerm> iterator = parent.getTerms().iterator(); iterator.hasNext(); ) {
            OntologyTerm child = iterator.next();
            if (child != null && child.getId() != null && child.getId().equals(termId)) {
                removed = child;
                iterator.remove();
                break;
            }
        }
        if (removed == null) {
            throw new Exception("Could not find term to remove: " + termId);
        }

        log.trace("Removing term '{}' from '{}'", removed.getName(), parent.getName());
        saveImpl(subjectKey, ontology);
        return getDomainObject(subjectKey, ontology);
    }

    public TreeNode getTreeNodeById(String subjectKey, Long id) {
        log.debug("getTreeNodeById({}, {})", subjectKey, id);
        return getDomainObject(subjectKey, TreeNode.class, id);
    }

    public TreeNode getParentTreeNodes(String subjectKey, Reference ref) {
        log.debug("getParentTreeNodes({}, {})", subjectKey, ref);
        String refStr = ref.toString();
        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null) {
            return treeNodeCollection.findOne("{'children':#}", refStr).as(TreeNode.class);
        } else {
            return treeNodeCollection.findOne("{'children':#,readers:{$in:#}}", refStr, subjects).as(TreeNode.class);
        }
    }

    public TreeNode getOrCreateDefaultFolder(String subjectKey, String folderName) throws Exception {
        Workspace defaultWorkspace = getDefaultWorkspace(subjectKey);
        if (defaultWorkspace == null) {
            throw new IllegalStateException("Subject does not have a default workspace: " + subjectKey);
        }
        TreeNode folder = DomainUtils.findObjectByTypeAndName(getUserDomainObjects(subjectKey, defaultWorkspace.getChildren()), TreeNode.class, folderName);
        if (folder == null) {
            log.debug("Existing folder named {} and owned by {} was not found in the default workspace. Creating one now.", folderName, subjectKey);
            folder = new TreeNode();
            folder.setName(folderName);
            folder = save(subjectKey, folder);
            addChildren(subjectKey, defaultWorkspace, Arrays.asList(Reference.createFor(folder)));
        }
        return folder;
    }

    public <T extends Node> T reorderChildren(String subjectKey, T nodeArg, int[] order) throws Exception {

        T node = getDomainObject(subjectKey, nodeArg);
        if (node == null) {
            throw new IllegalArgumentException("Tree node not found: " + nodeArg.getId());
        }

        log.debug("reorderChildren({}, {}, order={})", subjectKey, node, order);

        if (!node.hasChildren()) {
            log.warn("Tree node has no children to reorder: " + node.getId());
            return node;
        }

        List<Reference> references = new ArrayList<>(node.getChildren());

        if (references.size() != order.length) {
            throw new IllegalArgumentException("Order array must be the same size as the child array (" + order.length + "!=" + references.size() + ")");
        }

        if (log.isTraceEnabled()) {
            log.trace("{} has the following references: ", node.getName());
            for (Reference reference : references) {
                log.trace("  {}#{}", reference.getTargetClassName(), reference.getTargetId());
            }
            log.trace("They should be put in this ordering: ");
            for (int i = 0; i < order.length; i++) {
                log.trace("  {} -> {}", i, order[i]);
            }
        }

        int originalSize = references.size();
        Reference[] reordered = new Reference[references.size()];
        for (int i = 0; i < order.length; i++) {
            int j = order[i];
            reordered[j] = references.get(i);
            references.set(i, null);
        }

        node.getChildren().clear();
        for (Reference ref : reordered) {
            node.getChildren().add(ref);
        }
        for (Reference ref : references) {
            if (ref != null) {
                log.warn("Adding broken ref to collection " + ref.getTargetClassName() + " at the end");
                node.getChildren().add(ref);
            }
        }

        if (references.size() != originalSize) {
            throw new IllegalStateException("Reordered children have new size " + references.size() + " (was " + originalSize + ")");
        }

        saveImpl(subjectKey, node);
        return getDomainObject(subjectKey, node);
    }

    public List<DomainObject> getChildren(String subjectKey, Node node) {
        return getDomainObjects(subjectKey, node.getChildren());
    }

    public <T extends Node> T addChildren(String subjectKey, T nodeArg, Collection<Reference> references) throws Exception {
        return addChildren(subjectKey, nodeArg, references, null);
    }

    public <T extends Node> T addChildren(String subjectKey, T nodeArg, Collection<Reference> references, Integer index) throws Exception {
        if (references == null) {
            throw new IllegalArgumentException("Cannot add null children");
        }
        T node = getDomainObject(subjectKey, nodeArg);
        if (node == null) {
            throw new IllegalArgumentException("Tree node not found: " + nodeArg.getId());
        }
        log.debug("addChildren({}, {}, references={}, index={})", subjectKey, node, abbr(references), index);
        // Keep track of children in a set for faster 'contains' lookups
        Set<Reference> childRefs = new HashSet<>();
        for (Reference ref : node.getChildren()) {
            childRefs.add(ref);
        }
        int i = 0;
        List<Reference> added = new ArrayList<>();
        for (Reference ref : references) {
            if (ref.getTargetId() == null) {
                throw new IllegalArgumentException("Cannot add child without an id");
            }
            if (ref.getTargetClassName() == null) {
                throw new IllegalArgumentException("Cannot add child without a target class name");
            }
            if (childRefs.contains(ref)) {
                log.trace("{} already contains {}, skipping add.", node, ref);
                continue;
            }
            if (index != null) {
                node.insertChild(index + i, ref);
            } else {
                node.addChild(ref);
            }
            added.add(ref);
            childRefs.add(ref);
            i++;
        }
        saveImpl(subjectKey, node);

        for (Reference ref : added) {
            addPermissions(node.getOwnerKey(), ref.getTargetClassName(), ref.getTargetId(), node, false, false);
        }

        return getDomainObject(subjectKey, node);
    }

    public <T extends Node> T removeChildren(String subjectKey, T nodeArg, Collection<Reference> references) throws Exception {
        if (references == null) {
            throw new IllegalArgumentException("Cannot remove null children");
        }
        T node = getDomainObject(subjectKey, nodeArg);
        if (node == null) {
            throw new IllegalArgumentException("Tree node not found: " + nodeArg.getId());
        }
        log.debug("removeChildren({}, {}, references={})", subjectKey, node, abbr(references));
        for (Reference ref : references) {
            if (ref.getTargetId() == null) {
                throw new IllegalArgumentException("Cannot add child without an id");
            }
            if (ref.getTargetClassName() == null) {
                throw new IllegalArgumentException("Cannot add child without a target class name");
            }
            node.removeChild(ref);
        }
        saveImpl(subjectKey, node);
        return getDomainObject(subjectKey, node);
    }

    public <T extends Node> T removeReference(String subjectKey, T nodeArg, Reference reference) throws Exception {
        T node = getDomainObject(subjectKey, nodeArg);
        if (node == null) {
            throw new IllegalArgumentException("Tree node not found: " + nodeArg.getId());
        }
        log.debug("removeReference({}, {}, {})", subjectKey, node, reference);
        if (node.hasChildren()) {
            node.getChildren().removeIf(iref -> iref.equals(reference));
            saveImpl(subjectKey, node);
        }
        return getDomainObject(subjectKey, node);
    }

    public <T extends DomainObject> T updateProperty(String subjectKey, Class<T> clazz, Long id, String propName, Object propValue) throws Exception {
        return updateProperty(subjectKey, clazz.getName(), id, propName, propValue);
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainObject> T updateProperty(String subjectKey, String className, Long id, String propName, Object propValue) throws Exception {
        Class<T> clazz = (Class<T>) DomainUtils.getObjectClassByName(className);
        T domainObject = getDomainObject(subjectKey, clazz, id);
        try {
            set(domainObject, propName, propValue);
        } catch (Exception e) {
            throw new IllegalStateException("Could not update object attribute " + propName, e);
        }

        Set<String> subjects = getWriterSet(subjectKey);

        log.debug("updateProperty({}, {}, name={}, value={})", subjectKey, Reference.createFor(domainObject), propName, propValue);
        String collectionName = DomainUtils.getCollectionName(className);
        MongoCollection collection = getCollectionByName(collectionName);
        WriteResult wr = collection.update("{_id:#,writers:{$in:#}}", domainObject.getId(), subjects).with("{$set: {'" + propName + "':#, updatedDate:#}}", propValue, new Date());
        if (wr.getN() != 1) {
            throw new Exception("Could not update " + collectionName + "#" + domainObject.getId() + "." + propName);
        }
        return domainObject;
    }

    public <T extends DomainObject> void deleteProperty(String ownerKey, Class<T> clazz, String propName) {
        String collectionName = DomainUtils.getCollectionName(clazz);
        log.debug("deleteProperty({}, collection={}, name={})", ownerKey, collectionName, propName);
        MongoCollection collection = getCollectionByName(collectionName);
        WriteResult wr = collection.update("{ownerKey:#}", ownerKey).with("{$unset: {" + propName + ":\"\"}}");
        if (wr.getN() != 1) {
            log.warn("Could not delete property " + collectionName + "." + propName);
        }
    }

    public void addPermissions(String ownerKey, String className, Long id, DomainObject permissionTemplate, boolean forceChildUpdates) throws Exception {
        addPermissions(ownerKey, className, id, permissionTemplate, true, forceChildUpdates);
    }

    public void addPermissions(String ownerKey, String className, Long id, DomainObject permissionTemplate, boolean allowWriters, boolean forceChildUpdates) throws Exception {
        log.debug("addPermissions({}, className={}, id={}, permissionTemplate={}, forceChildUpdates={})", ownerKey, className, id, permissionTemplate, forceChildUpdates);
        changePermissions(ownerKey, className, Arrays.asList(id), permissionTemplate.getReaders(), permissionTemplate.getWriters(), true, allowWriters, forceChildUpdates, false);
    }

    public void setPermissions(String ownerKey, String className, Long id, String grantee, boolean read, boolean write, boolean forceChildUpdates) throws Exception {
        setPermissions(ownerKey, className, id, grantee, read, write, true, forceChildUpdates);
    }

    public void setPermissions(String ownerKey, String className, Long id, String grantee, boolean read, boolean write, boolean allowWriters, boolean forceChildUpdates) throws Exception {

        DomainObject targetObject = getDomainObject(ownerKey, className, id);

        Set<String> readAdd = new HashSet<>();
        Set<String> readRemove = new HashSet<>();
        Set<String> writeAdd = new HashSet<>();
        Set<String> writeRemove = new HashSet<>();

        if (DomainUtils.hasReadAccess(targetObject, grantee)) {
            if (!read) {
                if (DomainUtils.isOwner(targetObject, grantee)) {
                    log.warn("Cannot remove owner's read permission for {}", targetObject);
                } else {
                    readRemove.add(grantee);
                }
            }
        } else {
            if (read) {
                readAdd.add(grantee);
            }
        }

        if (DomainUtils.hasWriteAccess(targetObject, grantee)) {
            if (!write) {
                if (DomainUtils.isOwner(targetObject, grantee)) {
                    log.warn("Cannot remove owner's write permission for {}", targetObject);
                } else {
                    writeRemove.add(grantee);
                }
            }
        } else {
            if (write) {
                writeAdd.add(grantee);
            }
        }

        if (!readAdd.isEmpty() || !writeAdd.isEmpty()) {
            changePermissions(ownerKey, className, Arrays.asList(id), readAdd, writeAdd, true, allowWriters, forceChildUpdates, true);
        }

        if (!readRemove.isEmpty() || !writeRemove.isEmpty()) {
            changePermissions(ownerKey, className, Arrays.asList(id), readRemove, writeRemove, false, allowWriters, forceChildUpdates, true);
        }
    }


    /**
     * Change the permissions on the specified objects and their "children", to the given reader/writer sets.
     *
     * @param subjectKey            Current user
     * @param className             name of the class of parent objects
     * @param ids                   ids of the parent objects
     * @param readers               subject keys to add or remove from reader sets
     * @param writers               subject keys to add or remove from writer sets
     * @param grant                 if true, grant permissions, else, revoke them
     * @param allowWriters          Update objects where the current user has write permission. If this is false, only update objects where the current user is the owner.
     * @param forceChildUpdates     Update all child objects even if no updates were required to the parent objects
     * @param createSharedDataLinks Create links to the parent objects in the readers' Shared Data directory
     * @throws Exception
     */
    private void changePermissions(String subjectKey, String className, Collection<Long> ids, Collection<String> readers, Collection<String> writers,
                                   boolean grant, boolean allowWriters, boolean forceChildUpdates, boolean createSharedDataLinks) throws Exception {
        changePermissions(subjectKey, className, ids, readers, writers, grant, allowWriters, forceChildUpdates, createSharedDataLinks, new HashSet<>());
    }

    private void changePermissions(String subjectKey, String className, Collection<Long> ids, Collection<String> readers, Collection<String> writers,
                                   boolean grant, boolean allowWriters, boolean forceChildUpdates, boolean createSharedDataLinks, Set<Long> visited) throws Exception {

        String logIds = DomainUtils.abbr(ids);
        String updateQueryClause = allowWriters ? "writers:{$in:#}" : "ownerKey:#";
        String collectionName = DomainUtils.getCollectionName(className);
        String op = grant ? "$addToSet" : "$pull";
        String iter = grant ? "$each" : "$in";
        String withClause = "{" + op + ":{readers:{" + iter + ":#},writers:{" + iter + ":#}}}";

        Object updateQueryParam = allowWriters ? getWriterSet(subjectKey) : subjectKey;

        Class<? extends DomainObject> clazz = DomainUtils.getObjectClassByName(className);
        List<Reference> objectRefs = new ArrayList<>();
        for (Long id : ids) {
            objectRefs.add(Reference.createFor(clazz, id));
        }

        if (grant) {
            log.debug("grantPermissions({}, {}, ids={}, readers={}, writers={})", subjectKey, collectionName, logIds, readers, writers);
        } else {
            log.debug("revokePermissions({}, {}, ids={}, readers={}, writers={})", subjectKey, collectionName, logIds, readers, writers);
        }

        if (readers.isEmpty() && writers.isEmpty()) return;

        MongoCollection collection = getCollectionByName(collectionName);
        WriteResult wr = collection.update("{_id:{$in:#}," + updateQueryClause + "}", ids, updateQueryParam).multi().with(withClause, readers, writers);

        log.debug("Changing permissions on {} documents", wr.getN());

        if (forceChildUpdates || (wr.getN() > 0)) {
            // Update related objects
            // TODO: this class shouldn't know about these domain object classes, it should delegate somewhere else.

            if (Node.class.isAssignableFrom(clazz)) {
                log.trace("Changing permissions on all members of the nodes: {}", logIds);
                for (Long nodeId : ids) {

                    if (visited.contains(nodeId)) {
                        log.trace("Already visited folder with id=" + nodeId);
                        continue;
                    }
                    visited.add(nodeId);

                    Node node = (Node) collection.findOne("{_id:#," + updateQueryClause + "}", nodeId, updateQueryParam).as(clazz);
                    if (node == null) {
                        log.warn("Could not find node with id=" + nodeId);
                    } else if (node.hasChildren()) {
                        Multimap<String, Long> groupedIds = HashMultimap.create();
                        for (Reference ref : node.getChildren()) {
                            groupedIds.put(ref.getTargetClassName(), ref.getTargetId());
                        }

                        for (String refClassName : groupedIds.keySet()) {
                            Collection<Long> refIds = groupedIds.get(refClassName);
                            changePermissions(subjectKey, refClassName, refIds, readers, writers, grant, forceChildUpdates, allowWriters, false, visited);
                        }
                    }
                }
            }

            if (ColorDepthSearch.class.isAssignableFrom(clazz)) {
                for (ColorDepthSearch search : getDomainObjectsAs(objectRefs, ColorDepthSearch.class)) {

                    log.trace("Changing permissions on all masks and results associated with {}", search);

                    WriteResult wr1 = colorDepthMaskCollection.update("{_id:{$in:#}," + updateQueryClause + "}", search.getMasks(), updateQueryParam).multi().with(withClause, readers, writers);
                    log.trace("Updated permissions on {} masks", wr1.getN());

                    WriteResult wr2 = colorDepthResultCollection.update("{_id:{$in:#}," + updateQueryClause + "}", search.getResults(), updateQueryParam).multi().with(withClause, readers, writers);
                    log.trace("Updated permissions on {} results", wr2.getN());
                }
            } else if ("sample".equals(collectionName)) {

                log.trace("Changing permissions on all fragments and lsms associated with samples: {}", logIds);

                List<String> sampleRefs = DomainUtils.getRefStrings(objectRefs);

                WriteResult wr1 = fragmentCollection.update("{sampleRef:{$in:#}," + updateQueryClause + "}", sampleRefs, updateQueryParam).multi().with(withClause, readers, writers);
                log.trace("Updated permissions on {} fragments", wr1.getN());

                WriteResult wr2 = imageCollection.update("{sampleRef:{$in:#}," + updateQueryClause + "}", sampleRefs, updateQueryParam).multi().with(withClause, readers, writers);
                log.trace("Updated permissions on {} lsms", wr2.getN());

            } else if ("fragment".equals(collectionName)) {

                Set<Long> sampleIds = new HashSet<>();
                for (NeuronFragment fragment : getDomainObjectsAs(subjectKey, objectRefs, NeuronFragment.class)) {
                    sampleIds.add(fragment.getSample().getTargetId());
                }

                log.trace("Changing permissions on {} samples associated with fragments: {}", sampleIds.size(), logIds);
                changePermissions(subjectKey, Sample.class.getName(), sampleIds, readers, writers, grant, allowWriters, forceChildUpdates, false);
            } else if ("dataSet".equals(collectionName)) {
                log.trace("Changing permissions on all samples and LSMs of the data sets: {}", logIds);
                for (Long id : ids) {

                    // Retrieve the data set in order to find its identifier
                    DataSet dataSet = collection.findOne("{_id:#," + updateQueryClause + "}", id, updateQueryParam).as(DataSet.class);
                    if (dataSet == null) {
                        throw new IllegalArgumentException("Could not find an writeable data set with id=" + id);
                    }

                    // Get all sample ids for a given data set
                    List<String> sampleRefs = new ArrayList<>();
                    for (Sample sample : sampleCollection.find("{dataSet:#}", dataSet.getIdentifier()).projection("{class:1,_id:1}").as(Sample.class)) {
                        sampleRefs.add("Sample#" + sample.getId());
                    }

                    // This could just call changePermissions recursively, but batching is far more efficient.
                    WriteResult wr1 = sampleCollection.update("{dataSet:#," + updateQueryClause + "}", dataSet.getIdentifier(), updateQueryParam).multi().with(withClause, readers, writers);
                    log.trace("Changed permissions on {} samples", wr1.getN());

                    WriteResult wr2 = fragmentCollection.update("{sampleRef:{$in:#}," + updateQueryClause + "}", sampleRefs, updateQueryParam).multi().with(withClause, readers, writers);
                    log.trace("Updated permissions on {} fragments", wr2.getN());

                    WriteResult wr3 = imageCollection.update("{sampleRef:{$in:#}," + updateQueryClause + "}", sampleRefs, updateQueryParam).multi().with(withClause, readers, writers);
                    log.trace("Updated permissions on {} lsms", wr3.getN());

                    // JW-25275: Automatically create data set filters when sharing data sets

                    if (grant) {
                        for (String granteeKey : readers) {

                            if (granteeKey.equals(dataSet.getOwnerKey())) continue;

                            String filterName = dataSet.getName() + " (" + dataSet.getOwnerName() + ")";

                            if (getUserDomainObjectsByName(granteeKey, Filter.class, filterName).isEmpty()) {
                                // Grantee has no filters for this data set, so let's create one

                                log.info("Creating data set filter for " + filterName + ", shared with " + granteeKey);
                                Filter filter = createDataSetFilter(granteeKey, dataSet, filterName);

                                // Now add it to their Saved Data folder

                                TreeNode sharedDataFolder = getOrCreateDefaultFolder(granteeKey, DomainConstants.NAME_SHARED_DATA);
                                addChildren(granteeKey, sharedDataFolder, Arrays.asList(Reference.createFor(filter)));
                            }
                        }
                    }
                }
            } else if ("tmWorkspace".equals(collectionName)) {

                log.trace("Changing permissions on the TmSamples associated with the TmWorkspaces: {}", logIds);

                List<Long> sampleIds = new ArrayList<>();
                for (TmWorkspace workspace : tmWorkspaceCollection.find("{_id:{$in:#}}", ids).projection("{class:1,sampleRef:1}").as(TmWorkspace.class)) {
                    sampleIds.add(workspace.getSampleId());
                }

                WriteResult wr1 = tmSampleCollection.update("{_id:{$in:#}," + updateQueryClause + "}", sampleIds, updateQueryParam).multi().with(withClause, readers, writers);
                log.trace("Updated permissions on {} TmSamples", wr1.getN());

                List<String> workspaceRefs = DomainUtils.getRefStrings(objectRefs);
                log.trace("Changing permissions on the TmNeurons associated with the TmWorkspaces: {}", workspaceRefs);
                WriteResult wr2 = tmNeuronCollection.update("{workspaceRef:{$in:#}," + updateQueryClause + "}", workspaceRefs, updateQueryParam).multi().with(withClause, readers, writers);
                log.trace("Updated permissions on {} TmNeurons", wr2.getN());
            }
        }

        if (createSharedDataLinks) {
            // Ensure shared items are in the grantee's Shared Data folder
            Set<String> grantees = new HashSet<String>();
            grantees.addAll(readers);
            grantees.addAll(writers);

            // TODO: need a better way to specify the object classes that can be added to Shared Data
            if (clazz.isAssignableFrom(TreeNode.class)
                    || clazz.isAssignableFrom(Filter.class)
                    || clazz.isAssignableFrom(Sample.class)
                    || clazz.isAssignableFrom(NeuronFragment.class)
                    || clazz.isAssignableFrom(TmSample.class)
                    || clazz.isAssignableFrom(TmWorkspace.class)) {

                for (String grantee : grantees) {
                    if (!grantee.equals(subjectKey)) {
                        TreeNode sharedDataFolder = getOrCreateDefaultFolder(grantee, DomainConstants.NAME_SHARED_DATA);
                        if (grant) {
                            addChildren(grantee, sharedDataFolder, objectRefs);
                        } else {
                            removeChildren(grantee, sharedDataFolder, objectRefs);
                        }
                    }
                }
            }
        }
    }

    private Filter createDataSetFilter(String subjectKey, DataSet dataSet, String filterName) throws Exception {

        Filter filter = new Filter();
        filter.setName(filterName);
        filter.setSearchClass(Sample.class.getSimpleName());

        FacetCriteria dataSetCriteria = new FacetCriteria();
        dataSetCriteria.setAttributeName("dataSet");
        dataSetCriteria.setValues(Sets.newHashSet(dataSet.getIdentifier()));
        filter.addCriteria(dataSetCriteria);

        FacetCriteria syncFacet = new FacetCriteria();
        syncFacet.setAttributeName("sageSynced");
        syncFacet.setValues(Sets.newHashSet("true"));
        filter.addCriteria(syncFacet);

        save(subjectKey, filter);

        return filter;
    }

    public void addPipelineStatusTransition(Long sampleId, PipelineStatus source, PipelineStatus target, String orderNo,
                                            String process, Map<String, Object> parameters) throws Exception {
        log.info("adding StateTransition (source={}, target={}, sampleId={}, orderNo={}, process={})", source, target, sampleId, orderNo,
                process);
        StatusTransition newStatusTransition = new StatusTransition();
        newStatusTransition.setSampleId(sampleId);
        newStatusTransition.setSource(source);
        newStatusTransition.setTarget(target);
        newStatusTransition.setTransitionDate(new Date());
        newStatusTransition.setProcess(process);
        newStatusTransition.setParameters(parameters);
        newStatusTransition.setOrderNo(orderNo);
        pipelineStatusCollection.insert(newStatusTransition);
    }

    public List<StatusTransition> getPipelineStatusTransitionsBySampleId(Long sampleId) throws Exception {
        return toList(pipelineStatusCollection.find("{sampleId: #}", sampleId).as(StatusTransition.class));
    }

    public void addIntakeOrder(String orderNo, String owner) throws Exception {
        log.info("adding IntakeOrder (orderNo={}, owner={})", orderNo, owner);
        IntakeOrder newOrder = new IntakeOrder();
        newOrder.setOrderNo(orderNo);
        newOrder.setOwner(owner);
        newOrder.setStartDate(Calendar.getInstance().getTime());
        newOrder.setStatus(OrderStatus.Intake);
        intakeOrdersCollection.insert(newOrder);
    }

    public void addOrUpdateIntakeOrder(IntakeOrder order) throws Exception {
        log.info("adding/updating IntakeOrder (orderNo={}, owner={})", order.getOrderNo(), order.getOwner());
        IntakeOrder prevOrder = getIntakeOrder(order.getOrderNo());
        if (prevOrder == null) {
            intakeOrdersCollection.insert(order);
        } else {
            intakeOrdersCollection.update("{orderNo: #}}", order.getOrderNo()).with(order);
        }
    }

    // returns order information (including Sample Ids) given a number of hours time window
    public List<IntakeOrder> getIntakeOrders(Calendar cutoffDate) throws Exception {
        return toList(intakeOrdersCollection.find("{startDate: {$gte: #}}", cutoffDate).as(IntakeOrder.class));
    }

    // returns specific order information
    public IntakeOrder getIntakeOrder(String orderNo) throws Exception {
        List<IntakeOrder> orderList = toList(intakeOrdersCollection.find("{orderNo: #}}", orderNo).as(IntakeOrder.class));
        if (orderList == null || orderList.size() == 0)
            return null;
        return orderList.get(0);
    }

    public void addSampleToOrder(String orderNo, Long sampleId) throws Exception {
        intakeOrdersCollection.update("{orderNo: #}", orderNo).with("{$addToSet: { sampleIds: # } }", sampleId);
    }

    // add SampleIds to order as they get processed
    public void addSampleIdsToOrder(String orderNo, List<Long> sampleIds) throws Exception {
        intakeOrdersCollection.update("{orderNo: #}", orderNo).with("{$push: { sampleIds: { $each: # } } }", sampleIds);
    }

    // Copy and pasted from ReflectionUtils in shared module
    private void set(Object obj, String attributeName, Object value) throws Exception {
        Class<?>[] argTypes = {value.getClass()};
        Object[] argValues = {value};
        String methodName = getAccessor("set", attributeName);
        obj.getClass().getMethod(methodName, argTypes).invoke(obj, argValues);
    }

    // Copy and pasted from ReflectionUtils in shared module
    private static String getAccessor(String prefix, String attributeName) {
        String firstChar = attributeName.substring(0, 1).toUpperCase();
        return prefix + firstChar + attributeName.substring(1);
    }

    public Long getNewId() {
        return TimebasedIdentifierGenerator.generateIdList(1).get(0);
    }

    public List<LineRelease> getLineReleases(String subjectKey) {
        log.debug("getLineReleases({})", subjectKey);
        Set<String> subjects = getReaderSet(subjectKey);
        List<LineRelease> releases;
        if (subjects == null) {
            releases = toList(releaseCollection.find().as(LineRelease.class));
        } else {
            releases = toList(releaseCollection.find("{readers:{$in:#}}", subjects).as(LineRelease.class));
        }
        Collections.sort(releases, new DomainObjectComparator(subjectKey));
        return releases;
    }

    public List<LineRelease> getLineReleasesByName(String subjectKey) {
        log.debug("getLineReleasesByName({})", subjectKey);
        List<LineRelease> releases;
        if (subjectKey == null) {
            releases = toList(releaseCollection.find().as(LineRelease.class));
        } else {
            releases = toList(releaseCollection.find("{name: #}", subjectKey).as(LineRelease.class));
        }
        Collections.sort(releases, new DomainObjectComparator(subjectKey));
        return releases;
    }

    public LineRelease createLineRelease(String subjectKey, String name, Date releaseDate, Integer lagTimeMonths, List<String> dataSets) throws Exception {
        log.debug("createLineRelease({}, name={}, releaseDate={}, lagTimeMonths={}, dataSets={})", subjectKey, name, dataSets);
        LineRelease release = new LineRelease();
        release.setName(name);
        release.setReleaseDate(releaseDate);
        release.setLagTimeMonths(lagTimeMonths);
        release.setDataSets(dataSets);
        return save(subjectKey, release);
    }

    /**
     * Sum the disk space usage of all the samples in the given data set, and cache it within the corresponding DataDet object.
     *
     * @param dataSetIdentifier unique identifier of the data set to update
     * @throws Exception
     */
    public void updateDataSetDiskspaceUsage(String dataSetIdentifier) throws Exception {

        log.debug("updateDataSetDiskspaceUsage({})", dataSetIdentifier);

        Aggregate.ResultsIterator<Long> results = sampleCollection
                .aggregate("{$match: {\"dataSet\": \"" + dataSetIdentifier + "\"}}")
                .and("{$group: {_id:\"sum\",count:{$sum:\"$diskSpaceUsage\"}}}").map(new ResultHandler<Long>() {
                    @Override
                    public Long map(DBObject result) {
                        log.trace("Got result: {}", result);
                        // Count is usually a Long, but can be an Integer when it's zero. Let's play it safe.
                        Number count = (Number) result.get("count");
                        if (count == null) {
                            return 0L;
                        }
                        return count.longValue();
                    }
                });

        Long usage = 0L;
        if (results.hasNext()) {
            usage = results.next();
        }

        log.info("Calculated usage for {} = {} bytes", dataSetIdentifier, usage);

        WriteResult wr = dataSetCollection.update("{identifier:#}", dataSetIdentifier).with("{$set: {\"diskSpaceUsage\":#}}", usage);
        if (wr.getN() != 1) {
            throw new Exception("Could not update disk space usage for DataSet " + dataSetIdentifier);
        }
    }

    public void addColorDepthSearchMask(String subjectKey, Long searchId, ColorDepthMask mask) {
        Set<String> subjects = getWriterSet(subjectKey);
        Reference ref = Reference.createFor(mask);
        colorDepthSearchCollection.update("{_id:#, writers:{$in:#}}", searchId, subjects).with("{$push: { masks: # } }", ref);
    }

    public void addColorDepthSearchResult(String subjectKey, Long searchId, ColorDepthResult result) {
        Set<String> subjects = getWriterSet(subjectKey);
        Reference ref = Reference.createFor(result);
        colorDepthSearchCollection.update("{_id:#, writers:{$in:#}}", searchId, subjects).with("{$push: { results: # } }", ref);
    }

    public <T extends DomainObject> List<T> fullTextSearch(String subjectKey, Class<T> domainClass, String text) {

        String collectionName = DomainUtils.getCollectionName(domainClass);

        Set<String> subjects = getReaderSet(subjectKey);
        if (subjects == null || subjects.contains(Subject.ADMIN_KEY)) {
            return toList(getCollectionByName(collectionName).find("{$text:{$search:#}}", text).as(domainClass));
        } else {
            return toList(getCollectionByName(collectionName).find("{$text:{$search:#}, readers:{$in:#}} ", text, subjects).as(domainClass));
        }
    }

}
