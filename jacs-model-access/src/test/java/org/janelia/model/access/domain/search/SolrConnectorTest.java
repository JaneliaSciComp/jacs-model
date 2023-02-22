package org.janelia.model.access.domain.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

public class SolrConnectorTest {

    static class IndexingTestData {
        final int batchSize;
        final int expectedInvocations;

        private IndexingTestData(int batchSize, int expectedInvocations) {
            this.batchSize = batchSize;
            this.expectedInvocations = expectedInvocations;
        }
    }

    @Test
    public void indexDocumentStream() throws Exception {
        List<SolrInputDocument> testSolrDocs = createTestDocsForIndexing();
        IndexingTestData[] testData = createIndexingChecks(testSolrDocs.size());
        SolrClient testSolrClient = Mockito.mock(SolrClient.class);
        SolrConnector solrConnector = createSolrConnector(testSolrClient);
        for (IndexingTestData td : testData) {
            solrConnector.addDocsToIndex(testSolrDocs.stream(), td.batchSize);
            int batchSize = Math.max(1, td.batchSize);
            int nInvocations;
            if (batchSize == 1 || testSolrDocs.size() % batchSize == 0) {
                nInvocations = testSolrDocs.size() / batchSize;
            } else {
                nInvocations = testSolrDocs.size() / batchSize + 1;
            }
            if (batchSize == 1) {
                Mockito.verify(testSolrClient, times(nInvocations)).add(any(SolrInputDocument.class), anyInt());
            } else {
                Mockito.verify(testSolrClient, times(nInvocations)).add(anyList(), anyInt());
            }
            Mockito.verify(testSolrClient, times(0)).commit(true, true);
            Mockito.reset(testSolrClient);
        }
    }

    @Test
    public void indexParallelDocumentStream() throws Exception {
        List<SolrInputDocument> testSolrDocs = createTestDocsForIndexing();
        IndexingTestData[] testData = createIndexingChecks(testSolrDocs.size());
        SolrClient testSolrClient = Mockito.mock(SolrClient.class);
        SolrConnector solrConnector = createSolrConnector(testSolrClient);
        for (IndexingTestData td : testData) {
            solrConnector.addDocsToIndex(testSolrDocs.parallelStream(), td.batchSize);
            int batchSize = Math.max(1, td.batchSize);
            int nInvocations;
            if (batchSize == 1 || testSolrDocs.size() % batchSize == 0) {
                nInvocations = testSolrDocs.size() / batchSize;
            } else {
                nInvocations = testSolrDocs.size() / batchSize + 1;
            }
            if (batchSize == 1) {
                Mockito.verify(testSolrClient, times(nInvocations)).add(any(SolrInputDocument.class), anyInt());
            } else {
                Mockito.verify(testSolrClient, times(nInvocations)).add(anyList(), anyInt());
            }
            Mockito.verify(testSolrClient, times(0)).commit(true, true);
            Mockito.reset(testSolrClient);
        }
    }

    private List<SolrInputDocument> createTestDocsForIndexing() {
        return Arrays.asList(
                createTestSolrDoc("1"),
                createTestSolrDoc("2"),
                createTestSolrDoc("3"),
                createTestSolrDoc("4"),
                createTestSolrDoc("5"),
                createTestSolrDoc("6"),
                createTestSolrDoc("7"),
                createTestSolrDoc("8"),
                createTestSolrDoc("9"),
                createTestSolrDoc("10"),
                createTestSolrDoc("11"),
                createTestSolrDoc("12")
        );
    }

    private IndexingTestData[] createIndexingChecks(int nTestDocs) {
        return new IndexingTestData[] {
                new IndexingTestData(-2, nTestDocs),
                new IndexingTestData(0, 2),
                new IndexingTestData(1, 1),
                new IndexingTestData(4, 3),
                new IndexingTestData(5, 3),
                new IndexingTestData(nTestDocs, 1),
                new IndexingTestData(nTestDocs + 1, 1)
        };
    }

    @Test
    public void updateDocAncestors() throws Exception {
        class TestData {
            private final Set<Long> descendantIds;
            private final Long ancestorDocId;
            private final int batchSize;
            private final List<String> expectedQueries;

            private TestData(Set<Long> descendantIds, Long ancestorDocId, int batchSize, List<String> expectedQueries) {
                this.descendantIds = descendantIds;
                this.ancestorDocId = ancestorDocId;
                this.batchSize = batchSize;
                this.expectedQueries = expectedQueries;
            }
        }

        TestData[] testData = new TestData[] {
                new TestData(null, 10L, 0, Collections.emptyList()),
                new TestData(ImmutableSet.of(1L, 2L, 3L, 4L), 10L, 0, Arrays.asList("id:1 OR id:2 OR id:3 OR id:4")),
                new TestData(ImmutableSet.of(1L, 2L, 3L, 4L), 10L, 3, Arrays.asList("id:1 OR id:2 OR id:3", "id:4")),
                new TestData(ImmutableSet.of(1L, 2L, 3L, 4L), 10L, 2, Arrays.asList("id:1 OR id:2", "id:3 OR id:4"))
        };
        SolrClient testSolrClient = Mockito.mock(SolrClient.class);
        SolrConnector solrConnector = createSolrConnector(testSolrClient);
        for (TestData td : testData) {
            try {
                Mockito.when(testSolrClient.query(any(SolrQuery.class))).then(invocation -> {
                    QueryResponse testResponse = Mockito.mock(QueryResponse.class);
                    SolrQuery solrQuery = invocation.getArgument(0);
                    String query = solrQuery.getQuery();
                    SolrDocumentList solrDocumentList = new SolrDocumentList();
                    solrDocumentList.addAll(Splitter.on(" OR ").splitToList(query).stream().map(s -> {
                        SolrDocument doc = new SolrDocument();
                        doc.put("id", s);
                        return doc;
                    }).collect(Collectors.toList()));
                    Mockito.when(testResponse.getResults()).thenReturn(solrDocumentList);
                    return testResponse;
                });
            } catch (SolrServerException e) {
                fail(e.toString());
            }
            solrConnector.updateDocsAncestors(td.descendantIds, td.ancestorDocId, td.batchSize);
            td.expectedQueries.forEach(q -> {
                try {
                    Mockito.verify(testSolrClient).query(argThat((ArgumentMatcher<SolrQuery>) solrQuery -> solrQuery.getQuery().equals(q)));
                } catch (IOException e) {
                    fail(e.toString());
                } catch (SolrServerException e) {
                    fail(e.toString());
                }
            });
            if (CollectionUtils.isEmpty(td.descendantIds)) {
                Mockito.verify(testSolrClient, never()).add(anyList());
            } else {
                int batchSize = Math.max(1, td.batchSize);
                int nInvocations;
                if (batchSize == 1 || td.descendantIds.size() % batchSize == 0) {
                    nInvocations = td.descendantIds.size() / batchSize;
                } else {
                    nInvocations = td.descendantIds.size() / batchSize + 1;
                }
                if (batchSize == 1) {
                    Mockito.verify(testSolrClient, times(nInvocations)).add(any(SolrInputDocument.class), anyInt());
                } else {
                    Mockito.verify(testSolrClient, times(nInvocations)).add(anyList(), anyInt());
                }
            }
            Mockito.reset(testSolrClient);
        }
    }

    private SolrConnector createSolrConnector(SolrClient testSolrClient) {
        return new SolrConnector(testSolrClient);
    }
    private SolrInputDocument createTestSolrDoc(String id) {
        SolrInputDocument solrDoc = new SolrInputDocument();
        solrDoc.setField("id", id);
        return solrDoc;
    }
}
