package org.janelia.model.access.domain.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SOLR connector.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
class SolrConnector {
    private static final Logger LOG = LoggerFactory.getLogger(SolrConnector.class);
    private static final int SOLR_COMMIT_WITHIN_MS = 5000;

    private static SolrInputDocument toSolrInputDocument(SolrDocument solrDocument) {
        SolrInputDocument solrInputDocument = new SolrInputDocument();

        for (String name : solrDocument.getFieldNames()) {
            solrInputDocument.addField(name, solrDocument.getFieldValue(name));
        }

        //Don't forget children documents
        if (solrDocument.getChildDocuments() != null) {
            for (SolrDocument childDocument : solrDocument.getChildDocuments()) {
                //You can add paranoic check against infinite loop childDocument == solrDocument
                solrInputDocument.addChildDocument(toSolrInputDocument(childDocument));
            }
        }
        return solrInputDocument;
    }

    private final SolrClient solrClient;

    SolrConnector(@Nullable SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    /**
     * Run the given query against the index.
     *
     * @param query
     * @return
     */
    QueryResponse search(SolrQuery query) {
        LOG.trace("search(query={})", query.getQuery());
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return new QueryResponse();
        }
        LOG.debug("Running SOLR query: {}", query);
        try {
            return solrClient.query(query);
        } catch (IOException e) {
            LOG.error("Search IO error for {}", query, e);
            throw new IllegalStateException(e);
        } catch (SolrServerException e) {
            LOG.error("Search server error for {}", query, e);
            throw new IllegalStateException(e);
        }
    }

    boolean addDocToIndex(SolrInputDocument solrDoc) {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        try {
            solrClient.add(solrDoc, SOLR_COMMIT_WITHIN_MS);
            return true;
        } catch (Throwable e) {
            LOG.error("Error while adding {} to solr index", solrDoc, e);
            return false;
        }
    }

    int addDocsToIndex(Stream<SolrInputDocument> solrDocsStream, int batchSize) {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return 0;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<SolrInputDocument> solrDocsBatch = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger result = new AtomicInteger(0);
        // group documents in batches of given size to send the to solr
        solrDocsStream
                .forEach(solrDoc -> {
                    int nAdded;
                    if (batchSize > 1) {
                        solrDocsBatch.add(solrDoc);
                        int nDocs = solrDocsBatch.size();
                        if (nDocs >= batchSize) {
                            try {
                                LOG.debug("    Adding {} docs (+ {} in {}s)", solrDocsBatch.size(), result.get(), stopwatch.elapsed(TimeUnit.SECONDS));
                                solrClient.add(solrDocsBatch, SOLR_COMMIT_WITHIN_MS);
                                nAdded = nDocs;
                            } catch (Throwable e) {
                                LOG.error("Error while updating solr index with {} documents", nDocs, e);
                                nAdded = 0;
                            } finally {
                                solrDocsBatch.clear();
                            }
                        } else {
                            nAdded = 0;
                        }
                    } else {
                        try {
                            solrClient.add(solrDoc, SOLR_COMMIT_WITHIN_MS);
                            nAdded = 1;
                        } catch (Throwable e) {
                            LOG.error("Error while updating solr index for {}", solrDoc, e);
                            nAdded = 0;
                        }
                    }
                    if (nAdded > 0) {
                        result.addAndGet(nAdded);
                    }
                })
                ;
        if (solrDocsBatch.size() > 0) {
            try {
                LOG.debug("    Adding {} docs (+ {} in {}s)", solrDocsBatch.size(), result.get(), stopwatch.elapsed(TimeUnit.SECONDS));
                solrClient.add(solrDocsBatch, SOLR_COMMIT_WITHIN_MS);
                result.addAndGet(solrDocsBatch.size());
            } catch (Throwable e) {
                LOG.error("Error while updating solr index with {} docs", solrDocsBatch.size(), e);
            } finally {
                solrDocsBatch.clear();
            }
        }
        return result.get();
    }

    void clearIndex() {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return;
        }
        try {
            solrClient.deleteByQuery("*:*", SOLR_COMMIT_WITHIN_MS);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    boolean removeDocIdFromIndex(Long id) {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        try {
            solrClient.deleteById(id.toString(), SOLR_COMMIT_WITHIN_MS);
            return true;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    int removeDocIdsFromIndex(Stream<Long> docIdsStream, int batchSize) {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return 0;
        }
        AtomicInteger result = new AtomicInteger(0);
        List<Long> remainingIds = docIdsStream
            .reduce(new ArrayList<>(),
                    (ids, id) -> {
                        removeHandler(ids, id, batchSize, result);
                        return ids;
                    },
                    (ids1, ids2) -> {
                        for (Long id : ids2) {
                            removeHandler(ids1, id, batchSize, result);
                        }
                        return ids1;
                    });
        if (!remainingIds.isEmpty()) {
            int nRemoved = removeDocIds(remainingIds);
            result.addAndGet(nRemoved);
        }
        return result.get();
    }

    private void removeHandler(List<Long> ids, Long id, int batchSize, AtomicInteger itemsRemoved) {
        int nRemoved;
        if (batchSize > 1) {
            ids.add(id);
            if (ids.size() >= batchSize) {
                nRemoved = removeDocIds(ids);
                ids.clear();
            } else {
                nRemoved = 0;
            }
        } else {
            nRemoved = removeDocIds(Arrays.asList(id));
        }
        if (nRemoved > 0) {
            itemsRemoved.addAndGet(nRemoved);
        }
    }

    private int removeDocIds(List<Long> ids) {
        String q = idsToSolrQuery(ids);
        try {
            solrClient.deleteByQuery(q, SOLR_COMMIT_WITHIN_MS);
            return ids.size();
        } catch (Throwable e) {
            LOG.error("Error trying to delete using query: {}", q, e);
            return 0;
        }
    }

    private String idsToSolrQuery(List<Long> ids) {
        return ids
                .stream()
                .map(id -> "id:" + id.toString())
                .reduce((id1, id2) -> id1 + " OR " + id2)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    void updateDocsAncestors(Set<Long> docIds, Long ancestorId, int batchSize) {
        if (solrClient != null) {
            Stream<SolrInputDocument> solrDocsStream = searchByDocIds(docIds, batchSize)
                    .map(SolrConnector::toSolrInputDocument)
                    .peek(solrInputDoc -> {
                        Collection<Long> ancestorIds;
                        SolrInputField field = solrInputDoc.getField("ancestor_ids");
                        if (field == null || field.getValue() == null) {
                            ancestorIds = new ArrayList<>();
                        } else {
                            ancestorIds = (Collection<Long>) field.getValue();
                        }
                        ancestorIds.add(ancestorId);
                        solrInputDoc.addField("ancestor_ids", ancestorIds);
                    });
            addDocsToIndex(solrDocsStream, batchSize);
        }
    }

    private Stream<SolrDocument> searchByDocIds(Set<Long> solrDocIds, int batchSize) {
        if (CollectionUtils.isEmpty(solrDocIds)) {
            return Stream.of();
        } else {
            final AtomicInteger counter = new AtomicInteger();
            Collection<List<Long>> solrDocIdsPartitions = batchSize > 0
                    ? solrDocIds.stream()
                        .collect(Collectors.groupingBy(docId -> counter.getAndIncrement() / batchSize)).values()
                    : Collections.singleton(ImmutableList.copyOf(solrDocIds));
            return solrDocIdsPartitions.stream()
                    .map(partition -> idsToSolrQuery(partition))
                    .filter(queryStr -> queryStr != null)
                    .map(SolrQuery::new)
                    .map(this::search)
                    .flatMap(queryResponse -> queryResponse.getResults().stream())
                    ;
        }
    }

}
