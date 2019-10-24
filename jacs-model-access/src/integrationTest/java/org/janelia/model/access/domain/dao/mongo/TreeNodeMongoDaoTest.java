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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeNodeMongoDaoTest extends AbstractMongoDaoTest {

    private static final String TEST_OWNER = "testOwner";

    private TreeNodeMongoDao treeNodeMongoDao;

    @Before
    public void setUp() {
        SubjectMongoDao subjectMongoDao = new SubjectMongoDao(testMongoDatabase);
        treeNodeMongoDao = new TreeNodeMongoDao(
                testMongoDatabase,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
    }

    @Test
    public void getNodesByParentNameAndOwner() {
        TreeNode[] testNodes = new TreeNode[] {
                persistData(createTestNode("n1", TEST_OWNER)),
                persistData(createTestNode("n2", TEST_OWNER))
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
                persistData(createTestNode("n1")),
                persistData(createTestNode("n2")),
                persistData(createTestNode("n3")),
                persistData(createTestNode("n4")),
                persistData(createTestNode("n5")),
                persistData(createTestNode("n6"))
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
                        List<TreeNode> nodeAncestors = treeNodeMongoDao.getNodeDirectAncestors(nodeReference);
                        return nodeAncestors.stream().map(n -> Reference.createFor(n)).collect(Collectors.toSet());
                    },
                    n -> foundAncestors.add(n),
                    -1);
            assertEquals(td.expectedResult, foundAncestors);
        }
    }

    private TreeNode persistData(TreeNode n) {
        treeNodeMongoDao.save(n);
        return n;
    }

    private TreeNode createTestNode(String name) {
        return createTestNode(name, TEST_OWNER);
    }

    private TreeNode createTestNode(String name, String owner) {
        TreeNode n = new TreeNode();
        n.setName(name);
        n.setOwnerKey(owner);
        return n;
    }

    private void updateNodeChildren(TreeNode node, Set<Reference> children) {
        children.forEach(cr -> node.addChild(cr));
        MongoDaoHelper.update(treeNodeMongoDao.mongoCollection, node.getId(), ImmutableMap.of("children",  new SetFieldValueHandler<>(node.getChildren())));
    }

}
