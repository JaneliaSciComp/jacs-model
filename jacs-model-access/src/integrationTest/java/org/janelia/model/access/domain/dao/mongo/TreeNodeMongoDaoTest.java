package org.janelia.model.access.domain.dao.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.nodetools.NodeUtils;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.domain.workspace.TreeNode;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class TreeNodeMongoDaoTest extends AbstractMongoDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeMongoDao.class);
    private static final String TEST_NAME = "unittester";
    private static final String TEST_OWNER = "user:"+ TEST_NAME;

    private TreeNodeMongoDao treeNodeMongoDao;
    private DatasetMongoDao datasetMongoDao;
    private SampleMongoDao sampleMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        SubjectMongoDao subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        subjectMongoDao.createUser(TEST_NAME, null, null);
        this.treeNodeMongoDao = new TreeNodeMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
        this.datasetMongoDao = new DatasetMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
        this.sampleMongoDao = new SampleMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
    }

    @Test
    public void getNodesByParentNameAndOwner() {
        TreeNode[] testNodes = new TreeNode[] {
                createTestNode("n1"),
                createTestNode("n2")
        };
        updateNodeChildren(testNodes[0], ImmutableSet.of(Reference.createFor(testNodes[1])));
        List<TreeNode> result = treeNodeMongoDao.getNodesByParentNameAndOwnerKey(testNodes[0].getId(), "n2", TEST_OWNER);
        assertEquals(ImmutableList.of(testNodes[1]), result);
    }

    @Test
    public void getNodeAncestors() {
        class TestData {
            private final TreeNode startNode;
            private final Set<Reference> expectedResult;

            private TestData(TreeNode startNode, Set<Reference> expectedResult) {
                this.startNode = startNode;
                this.expectedResult = expectedResult;
            }
        }
        TreeNode[] testNodes = new TreeNode[] {
                createTestNode("n1"),
                createTestNode("n2"),
                createTestNode("n3"),
                createTestNode("n4"),
                createTestNode("n5"),
                createTestNode("n6")
        };
        Map<TreeNode, Set<Reference>> nodeChildren = ImmutableMap.<TreeNode, Set<Reference>>builder()
                .put(testNodes[0],
                        ImmutableSet.of(
                                Reference.createFor(testNodes[1]),
                                Reference.createFor(testNodes[2])
                        ))
                .put(testNodes[1],
                        ImmutableSet.of(
                                Reference.createFor(testNodes[2]),
                                Reference.createFor(testNodes[3]),
                                Reference.createFor(testNodes[4])
                        ))
                .put(testNodes[2],
                        ImmutableSet.of())
                .put(testNodes[3],
                        ImmutableSet.of())
                .put(testNodes[4],
                        ImmutableSet.of(
                                Reference.createFor(testNodes[2]),
                                Reference.createFor(testNodes[3]),
                                Reference.createFor(testNodes[5])
                        ))
                .put(testNodes[5],
                        ImmutableSet.of(
                                Reference.createFor(testNodes[2])
                        ))
                .build();
        nodeChildren.forEach(this::updateNodeChildren);
        TestData[] testData = new TestData[] {
                new TestData(
                        testNodes[2],
                        ImmutableSet.of(
                                Reference.createFor(testNodes[0]),
                                Reference.createFor(testNodes[1]),
                                Reference.createFor(testNodes[4]),
                                Reference.createFor(testNodes[5]))
                ),
                new TestData(
                        testNodes[3],
                        ImmutableSet.of(
                                Reference.createFor(testNodes[0]),
                                Reference.createFor(testNodes[1]),
                                Reference.createFor(testNodes[4]))
                )
        };

        for (TestData td : testData) {
            Set<Reference> foundAncestors = new LinkedHashSet<>();
            NodeUtils.traverseAllAncestors(
                    Reference.createFor(td.startNode),
                    nodeReference -> {
                        List<? extends Node> nodeAncestors = treeNodeMongoDao.getNodeDirectAncestors(nodeReference);
                        return nodeAncestors.stream().map(Reference::createFor).collect(Collectors.toSet());
                    },
                    n -> foundAncestors.add(n),
                    -1);
            assertEquals(td.expectedResult, foundAncestors);
        }
    }

    @Test
    public void getNodeChildren() {

        TreeNode testNode = createTestNode("parent node");

        // Add 12 child folders
        List<Reference> childNodeRefs = new ArrayList<>();
        for(int i=0; i<12; i++) {
            childNodeRefs.add(Reference.createFor(createTestNode("child node "+i)));
        }

        updateNodeChildren(testNode, childNodeRefs);

        int pageSize = 5;

        List<DomainObject> page1 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "+id", 0, pageSize);
        assertEquals(pageSize, page1.size());

        List<DomainObject> page2 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "+id", 1, pageSize);
        assertEquals(pageSize, page2.size());

        List<DomainObject> page3 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "+id", 2, pageSize);
        assertEquals(2, page3.size());

        List<Reference> refs = new ArrayList<>();
        refs.addAll(DomainUtils.getReferences(page1));
        refs.addAll(DomainUtils.getReferences(page2));
        refs.addAll(DomainUtils.getReferences(page3));

        assertEquals(childNodeRefs, refs);
    }

    @Test
    public void getDiverseNodeChildren() {

        TreeNode testNode = createTestNode("parent node");

        // Add 15 children of diverse types
        List<Reference> childRefs = new ArrayList<>();
        childRefs.add(Reference.createFor(createTestSample("child 1")));
        childRefs.add(Reference.createFor(createTestSample("child 2")));
        childRefs.add(Reference.createFor(createTestDataSet("child 3")));
        childRefs.add(Reference.createFor(createTestNode("child 4")));
        childRefs.add(Reference.createFor(createTestNode("child 5")));

        childRefs.add(Reference.createFor(createTestSample("child 6")));
        childRefs.add(Reference.createFor(createTestNode("child 7")));
        childRefs.add(Reference.createFor(createTestDataSet("child 8")));
        childRefs.add(Reference.createFor(createTestNode("child 9")));
        childRefs.add(Reference.createFor(createTestSample("child 10")));

        childRefs.add(Reference.createFor(createTestNode("child 11")));
        childRefs.add(Reference.createFor(createTestNode("child 12")));
        childRefs.add(Reference.createFor(createTestDataSet("child 13")));
        childRefs.add(Reference.createFor(createTestSample("child 14")));
        childRefs.add(Reference.createFor(createTestSample("child 15")));

        updateNodeChildren(testNode, childRefs);

        assertEquals(15, testNode.getNumChildren());
        int pageSize = 5;

        List<DomainObject> page1 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "+id", 0, pageSize);
        assertEquals(pageSize, page1.size());

        List<DomainObject> page2 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "+id", 1, pageSize);
        assertEquals(pageSize, page2.size());

        List<DomainObject> page3 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "+id", 2, pageSize);
        assertEquals(pageSize, page3.size());

        List<Reference> refs = new ArrayList<>();
        refs.addAll(DomainUtils.getReferences(page1));
        refs.addAll(DomainUtils.getReferences(page2));
        refs.addAll(DomainUtils.getReferences(page3));

        assertEquals(childRefs, refs);
    }

    @Test
    public void getSortedNodeChildren() {

        TreeNode testNode = createTestNode("parent node");

        // Add 15 children of diverse types
        List<Reference> childRefs = new ArrayList<>();
        childRefs.add(Reference.createFor(createTestSample("child 01")));
        childRefs.add(Reference.createFor(createTestSample("child 02")));
        childRefs.add(Reference.createFor(createTestDataSet("child 03")));
        childRefs.add(Reference.createFor(createTestNode("child 04")));
        childRefs.add(Reference.createFor(createTestNode("child 05")));
        childRefs.add(Reference.createFor(createTestSample("child 06")));
        childRefs.add(Reference.createFor(createTestNode("child 07")));
        childRefs.add(Reference.createFor(createTestDataSet("child 08")));
        childRefs.add(Reference.createFor(createTestNode("child 09")));
        childRefs.add(Reference.createFor(createTestSample("child 10")));
        childRefs.add(Reference.createFor(createTestNode("child 11")));
        childRefs.add(Reference.createFor(createTestNode("child 12")));
        childRefs.add(Reference.createFor(createTestDataSet("child 13")));
        childRefs.add(Reference.createFor(createTestSample("child 14")));
        childRefs.add(Reference.createFor(createTestSample("child 15")));

        updateNodeChildren(testNode, childRefs);

        assertEquals(15, testNode.getNumChildren());
        int pageSize = 10;

        List<DomainObject> page1 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "-name", 0, pageSize);
        assertEquals(pageSize, page1.size());

        List<DomainObject> page2 = treeNodeMongoDao.getChildren(TEST_OWNER, testNode, "-name", 1, pageSize);
        assertEquals(5, page2.size());

        List<Reference> refs = new ArrayList<>();
        refs.addAll(DomainUtils.getReferences(page1));
        refs.addAll(DomainUtils.getReferences(page2));

        // The refs should come back in reverse order due to sort criteria
        assertEquals(Lists.reverse(childRefs), refs);
    }

    private DataSet createTestDataSet(String name) {
        DataSet dataSet = new DataSet();
        dataSet.setName(name);
        datasetMongoDao.saveBySubjectKey(dataSet, TEST_OWNER);
        LOG.trace("Saved {}", dataSet);
        return dataSet;
    }

    private Sample createTestSample(String name) {
        Sample sample = new Sample();
        sample.setName(name);
        sample.setAge("A01");
        sample.setBlocked(false);
        sampleMongoDao.saveBySubjectKey(sample, TEST_OWNER);
        LOG.trace("Saved {}", sample);
        return sample;
    }

    private TreeNode createTestNode(String name) {
        TreeNode node = new TreeNode();
        node.setName(name);
        treeNodeMongoDao.saveBySubjectKey(node, TEST_OWNER);
        LOG.trace("Saved {}", node);
        return node;
    }

    private void updateNodeChildren(TreeNode node, Collection<Reference> children) {
        children.forEach(node::addChild);
        MongoDaoHelper.update(treeNodeMongoDao.mongoCollection, node.getId(), ImmutableMap.of("children",  new SetFieldValueHandler<>(node.getChildren())));
    }


}
