package org.janelia.model.access.domain.dao.mongo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.janelia.model.domain.ontology.Category;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.ontology.OntologyTerm;
import org.janelia.model.domain.ontology.Tag;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class OntologyMongoDaoTest extends AbstractMongoDaoTest {

    private SubjectMongoDao subjectMongoDao;
    private OntologyMongoDao ontologyMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        ontologyMongoDao = new OntologyMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
    }

    @Test
    public void addNewTerms() {
        class TestData {
            private final String subjectKey;
            private final Ontology ontology;
            private final String insertAfter;
            private final String[] newTerms;
            private final String[] expectedNewTerms;

            TestData(String subjectKey,
                     Ontology ontology,
                     String insertAfter,
                     String[] newTerms,
                     String[] expectedNewTerms) {
                this.subjectKey = subjectKey;
                this.ontology = ontology;
                this.insertAfter = insertAfter;
                this.newTerms = newTerms;
                this.expectedNewTerms = expectedNewTerms;
            }
        }
        Ontology testOntology = persistData(createTestOntology(
                "o1",
                "u1",
                Arrays.asList(
                        createCategoryTerm("o1.c1"),
                        createCategoryTerm("o1.c2"),
                        createTagTerm("o1.t1"),
                        createTagTerm("o1.t2")
                )));
        TestData[] testData = new TestData[] {
                new TestData(
                        "u1",
                        testOntology,
                        "o1.c2",
                        new String[] {"o1.c3", "o1.c4"},
                        new String[] {"o1.c1", "o1.c2", "o1.c3", "o1.c4", "o1.t1", "o1.t2"}
                ),
                new TestData(
                        "u2",
                        testOntology,
                        "o1.c2",
                        new String[] {"o1.c3", "o1.c4"},
                        null
                )
        };
        for (TestData td : testData) {
            Long parentTermId = td.ontology.getId();
            int insertPos = 0;
            for (OntologyTerm t : td.ontology.getTerms()) {
                insertPos++;
                if (td.insertAfter.equals(t.getName())) {
                    break;
                }
            }
            Ontology updatedOntology = ontologyMongoDao.addTerms(
                    td.subjectKey,
                    td.ontology.getId(),
                    parentTermId,
                    Arrays.stream(td.newTerms)
                            .map(tn -> createCategoryTerm(tn)).collect(Collectors.toList()),
                    insertPos
            );
            if (td.expectedNewTerms == null) {
                assertNull(updatedOntology);
            } else {
                assertArrayEquals(td.expectedNewTerms,
                        updatedOntology.getTerms().stream().map(t -> t.getName()).toArray());
            }
        }
    }

    @Test
    public void reorderTerms() {
        class TestData {
            private final String subjectKey;
            private final Ontology ontology;
            private final int[] newOrder;
            private final String[] expectedTermsOrder;
            private final Class<? extends Throwable> expectedExceptionClass;
            private final String expectedExceptionMessage;

            TestData(String subjectKey,
                     Ontology ontology,
                     int[] newOrder,
                     String[] expectedTermsOrder,
                     Class<? extends Throwable> expectedExceptionClass,
                     String expectedExceptionMessage) {
                this.subjectKey = subjectKey;
                this.ontology = ontology;
                this.newOrder = newOrder;
                this.expectedTermsOrder = expectedTermsOrder;
                this.expectedExceptionClass = expectedExceptionClass;
                this.expectedExceptionMessage = expectedExceptionMessage;
            }
        }
        Ontology testOntology = persistData(createTestOntology(
                "o1",
                "u1",
                Arrays.asList(
                        createCategoryTerm("o1.c1"),
                        createCategoryTerm("o1.c2"),
                        createTagTerm("o1.t1"),
                        createTagTerm("o1.t2")
                )));
        TestData[] testData = new TestData[] {
                new TestData(
                        "u1",
                        testOntology,
                        new int[] {1, 0, 3, 2},
                        new String[] {"o1.c2", "o1.c1", "o1.t2", "o1.t1"},
                        null,
                        null
                ),
                new TestData(
                        "u1",
                        testOntology,
                        new int[] {1, 0},
                        new String[] {"o1.c1", "o1.c2", "o1.t2", "o1.t1"},
                        null,
                        null
                ),
                new TestData(
                        "u1",
                        testOntology,
                        new int[] {2, 3}, // if we need to rearrange only the end we need to pass all terms order
                        new String[] {"o1.c1", "o1.c2", "o1.t2", "o1.t1"},
                        IllegalArgumentException.class,
                        "Index value 2 greater than array length 2 in term order array [2, 3]"
                ),
                new TestData(
                        "u2",
                        testOntology,
                        new int[] {1, 0, 3, 2},
                        null,
                        null,
                        null
                )
        };
        for (TestData td : testData) {
            Long parentTermId = td.ontology.getId();
            if (td.expectedExceptionClass != null) {
                Assertions.assertThatThrownBy(() -> ontologyMongoDao.reorderTerms(
                        td.subjectKey,
                        td.ontology.getId(),
                        parentTermId,
                        td.newOrder))
                        .isInstanceOf(td.expectedExceptionClass)
                        .hasMessage(td.expectedExceptionMessage);
            } else {
                Ontology updatedOntology = ontologyMongoDao.reorderTerms(
                        td.subjectKey,
                        td.ontology.getId(),
                        parentTermId,
                        td.newOrder
                );
                if (td.expectedTermsOrder == null) {
                    assertNull(updatedOntology);
                } else {
                    assertArrayEquals(td.expectedTermsOrder,
                            updatedOntology.getTerms().stream().map(t -> t.getName()).toArray());
                }
            }

        }
    }

    @Test
    public void removeTerm() {
        class TestData {
            private final String subjectKey;
            private final Ontology ontology;
            private final String toRemove;
            private final String[] expectedNewTerms;

            TestData(String subjectKey,
                     Ontology ontology,
                     String toRemove,
                     String[] expectedNewTerms) {
                this.subjectKey = subjectKey;
                this.ontology = ontology;
                this.toRemove = toRemove;
                this.expectedNewTerms = expectedNewTerms;
            }
        }
        Ontology testOntology = persistData(createTestOntology(
                "o1",
                "u1",
                Arrays.asList(
                        createCategoryTerm("o1.c1"),
                        createCategoryTerm("o1.c2"),
                        createTagTerm("o1.t1"),
                        createTagTerm("o1.t2")
                )));
        TestData[] testData = new TestData[] {
                new TestData(
                        "u1",
                        testOntology,
                        "o1.c2",
                        new String[] {"o1.c1", "o1.t1", "o1.t2"}
                ),
                new TestData(
                        "u2",
                        testOntology,
                        "o1.c2",
                        null
                )
        };
        for (TestData td : testData) {
            Long parentTermId = td.ontology.getId();
            Long termId = null;
            for (OntologyTerm t : td.ontology.getTerms()) {
                if (td.toRemove.equals(t.getName())) {
                    termId = t.getId();
                }
            }
            Ontology updatedOntology = ontologyMongoDao.removeTerm(
                    td.subjectKey,
                    td.ontology.getId(),
                    parentTermId,
                    termId
            );
            if (td.expectedNewTerms == null) {
                assertNull(updatedOntology);
            } else {
                assertArrayEquals(td.expectedNewTerms,
                        updatedOntology.getTerms().stream().map(t -> t.getName()).toArray());
            }
        }
    }

    private Ontology persistData(Ontology o) {
        ontologyMongoDao.save(o);
        return o;
    }

    private Ontology createTestOntology(String name, String ownerKey, List<OntologyTerm> terms) {
        Ontology ontology = new Ontology();
        ontology.setName(name);
        ontology.setOwnerKey(ownerKey);
        terms.forEach(t -> ontology.addChild(t));
        return ontology;
    }

    private Category createCategoryTerm(String termName) {
        Category term = new Category();
        term.setId(ontologyMongoDao.createNewId());
        term.setName(termName);
        return term;
    }

    private Tag createTagTerm(String termName) {
        Tag term = new Tag();
        term.setId(ontologyMongoDao.createNewId());
        term.setName(termName);
        return term;
    }

}
