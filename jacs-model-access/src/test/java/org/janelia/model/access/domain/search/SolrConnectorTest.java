package org.janelia.model.access.domain.search;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

public class SolrConnectorTest {

    @Test
    public void indexDocumentStream() throws Exception {
        class TestData {
            private final int batchSize;
            private final int commitSize;
            private final int expectedCommits;

            private TestData(int batchSize, int commitSize, int expectedCommits) {
                this.batchSize = batchSize;
                this.commitSize = commitSize;
                this.expectedCommits = expectedCommits;
            }
        }
        List<SolrInputDocument> testSolrDocs = Arrays.asList(
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
        TestData[] testData = new TestData[] {
                new TestData(-2, 0, testSolrDocs.size()),
                new TestData(0, 10, 2),
                new TestData(1, testSolrDocs.size() + 1, 1),
                new TestData(4, 0, 3),
                new TestData(5, 5, 3),
                new TestData(testSolrDocs.size(), 1, 1),
                new TestData(testSolrDocs.size() + 1, 100, 1)
        };
        SolrServer testSolrServer = Mockito.mock(SolrServer.class);
        int commitDelay = 100;
        SolrConnector solrConnector = createSolrConnector(testSolrServer, commitDelay);
        for (TestData td : testData) {
            solrConnector.addDocsToIndex(testSolrDocs.stream(), td.batchSize, td.commitSize);
            int batchSize = Math.max(1, td.batchSize);
            int nInvocations;
            if (batchSize == 1 || testSolrDocs.size() % batchSize == 0) {
                nInvocations = testSolrDocs.size() / batchSize;
            } else {
                nInvocations = testSolrDocs.size() / batchSize + 1;
            }
            if (batchSize == 1) {
                Mockito.verify(testSolrServer, times(nInvocations)).add(any(SolrInputDocument.class));
            } else {
                Mockito.verify(testSolrServer, times(nInvocations)).add(anyList());
            }
            Mockito.verify(testSolrServer, times(td.expectedCommits)).commit(true, true);
            Mockito.reset(testSolrServer);
        }
    }

    @Test
    public void updateDocAncestors() throws Exception {
        class TestData {
            private final Set<Long> descendantIds;
            private final Long ancestorDocId;
            private final int batchSize;
            private final int commitSize;
            private final List<String> expectedQueries;

            private TestData(Set<Long> descendantIds, Long ancestorDocId, int batchSize, int commitSize, List<String> expectedQueries) {
                this.descendantIds = descendantIds;
                this.ancestorDocId = ancestorDocId;
                this.batchSize = batchSize;
                this.commitSize = commitSize;
                this.expectedQueries = expectedQueries;
            }
        }

        TestData[] testData = new TestData[] {
                new TestData(null, 10L, 0, 0, Collections.emptyList()),
                new TestData(ImmutableSet.of(1L, 2L, 3L, 4L), 10L, 0, 1, Arrays.asList("id:1 OR id:2 OR id:3 OR id:4")),
                new TestData(ImmutableSet.of(1L, 2L, 3L, 4L), 10L, 3, 1, Arrays.asList("id:1 OR id:2 OR id:3", "id:4")),
                new TestData(ImmutableSet.of(1L, 2L, 3L, 4L), 10L, 2, 100, Arrays.asList("id:1 OR id:2", "id:3 OR id:4"))
        };
        SolrServer testSolrServer = Mockito.mock(SolrServer.class);
        int commitDelay = 100;
        SolrConnector solrConnector = createSolrConnector(testSolrServer, commitDelay);
        for (TestData td : testData) {
            try {
                Mockito.when(testSolrServer.query(any(SolrQuery.class))).then(invocation -> {
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
            solrConnector.updateDocsAncestors(td.descendantIds, td.ancestorDocId, td.batchSize, td.commitSize);
            td.expectedQueries.forEach(q -> {
                try {
                    Mockito.verify(testSolrServer).query(argThat((ArgumentMatcher<SolrQuery>) solrQuery -> solrQuery.getQuery().equals(q)));
                } catch (SolrServerException e) {
                    fail(e.toString());
                }
            });
            if (CollectionUtils.isEmpty(td.descendantIds)) {
                Mockito.verify(testSolrServer, never()).add(anyList(), eq(commitDelay));
            } else {
                int batchSize = Math.max(1, td.batchSize);
                int nInvocations;
                if (batchSize == 1 || td.descendantIds.size() % batchSize == 0) {
                    nInvocations = td.descendantIds.size() / batchSize;
                } else {
                    nInvocations = td.descendantIds.size() / batchSize + 1;
                }
                if (batchSize == 1) {
                    Mockito.verify(testSolrServer, times(nInvocations)).add(any(SolrInputDocument.class));
                } else {
                    Mockito.verify(testSolrServer, times(nInvocations)).add(anyList());
                }
            }
            Mockito.reset(testSolrServer);
        }
    }

    private SolrConnector createSolrConnector(SolrServer testSolrServer, int commitDelay) {
        return new SolrConnector(testSolrServer, commitDelay);
    }
    private SolrInputDocument createTestSolrDoc(String id) {
        SolrInputDocument solrDoc = new SolrInputDocument();
        solrDoc.setField("id", id);
        return solrDoc;
    }
}
