package org.janelia.model.domain.workspace;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.janelia.model.domain.Reference;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class NodeUtilsTest {

    @Test
    public void traverseAncestors() {
        class TestData {
            final Map<Reference, Set<Reference>> inputAncestorsMap;
            final Reference startNode;
            final int levels;
            final Set<Reference> expectedResult;

            TestData(Map<Reference, Set<Reference>> inputAncestorsMap, Reference startNode, int levels, Set<Reference> expectedResult) {
                this.inputAncestorsMap = inputAncestorsMap;
                this.startNode = startNode;
                this.levels = levels;
                this.expectedResult = expectedResult;
            }
        }

        TestData[] testData = new TestData[] {
                new TestData(ImmutableMap.<Reference, Set<Reference>>builder()
                        .put(Reference.createFor("#1"), ImmutableSet.of())
                        .put(Reference.createFor("#2"), ImmutableSet.of(Reference.createFor("#1")))
                        .put(Reference.createFor("#3"), ImmutableSet.of(Reference.createFor("#1")))
                        .put(Reference.createFor("#4"), ImmutableSet.of(Reference.createFor("#2")))
                        .put(Reference.createFor("#5"), ImmutableSet.of(Reference.createFor("#2")))
                        .put(Reference.createFor("#6"), ImmutableSet.of(Reference.createFor("#3")))
                        .put(Reference.createFor("#7"), ImmutableSet.of(Reference.createFor("#3")))
                        .build(),
                        Reference.createFor("#5"),
                        -1,
                        ImmutableSet.of(Reference.createFor("#1"), Reference.createFor("#2"))
                ),
                new TestData(ImmutableMap.<Reference, Set<Reference>>builder()
                        .put(Reference.createFor("#1"), ImmutableSet.of())
                        .put(Reference.createFor("#2"), ImmutableSet.of(Reference.createFor("#1")))
                        .put(Reference.createFor("#3"), ImmutableSet.of(Reference.createFor("#1"), Reference.createFor("#2"), Reference.createFor("#5"), Reference.createFor("#6")))
                        .put(Reference.createFor("#4"), ImmutableSet.of(Reference.createFor("#2"), Reference.createFor("#5")))
                        .put(Reference.createFor("#5"), ImmutableSet.of(Reference.createFor("#2")))
                        .put(Reference.createFor("#6"), ImmutableSet.of(Reference.createFor("#5")))
                        .build(),
                        Reference.createFor("#3"),
                        -1,
                        ImmutableSet.of(Reference.createFor("#1"), Reference.createFor("#2"), Reference.createFor("#5"), Reference.createFor("#6"))
                ),
                new TestData(ImmutableMap.<Reference, Set<Reference>>builder()
                        .put(Reference.createFor("#1"), ImmutableSet.of())
                        .put(Reference.createFor("#2"), ImmutableSet.of(Reference.createFor("#1")))
                        .put(Reference.createFor("#3"), ImmutableSet.of(Reference.createFor("#1"), Reference.createFor("#2"), Reference.createFor("#5"), Reference.createFor("#6")))
                        .put(Reference.createFor("#4"), ImmutableSet.of(Reference.createFor("#2"), Reference.createFor("#5")))
                        .put(Reference.createFor("#5"), ImmutableSet.of(Reference.createFor("#2")))
                        .put(Reference.createFor("#6"), ImmutableSet.of(Reference.createFor("#4")))
                        .build(),
                        Reference.createFor("#6"),
                        2,
                        ImmutableSet.of(Reference.createFor("#4"), Reference.createFor("#2"), Reference.createFor("#5"))
                ),
                new TestData(ImmutableMap.<Reference, Set<Reference>>builder()
                        .put(Reference.createFor("#1"), ImmutableSet.of())
                        .put(Reference.createFor("#2"), ImmutableSet.of(Reference.createFor("#1")))
                        .put(Reference.createFor("#3"), ImmutableSet.of(Reference.createFor("#1"), Reference.createFor("#2"), Reference.createFor("#5"), Reference.createFor("#6")))
                        .put(Reference.createFor("#4"), ImmutableSet.of(Reference.createFor("#2"), Reference.createFor("#5")))
                        .put(Reference.createFor("#5"), ImmutableSet.of(Reference.createFor("#2")))
                        .put(Reference.createFor("#6"), ImmutableSet.of(Reference.createFor("#5")))
                        .build(),
                        Reference.createFor("#4"),
                        -1,
                        ImmutableSet.of(Reference.createFor("#1"), Reference.createFor("#2"), Reference.createFor("#5"))
                )

        };
        for (TestData td : testData) {
            Set<Reference> foundAncestors = new LinkedHashSet<>();
            NodeUtils.traverseAllAncestors(td.startNode, td.inputAncestorsMap::get, n -> foundAncestors.add(n), td.levels);
            assertEquals(td.expectedResult, foundAncestors);
        }
    }
}
