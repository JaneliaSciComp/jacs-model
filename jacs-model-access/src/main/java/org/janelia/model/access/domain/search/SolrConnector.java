package org.janelia.model.access.domain.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

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
import org.slf4j.MDC;

/**
 * A SOLR connector.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
class SolrConnector {
    private static final Logger LOG = LoggerFactory.getLogger(SolrConnector.class);

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
    private final int solrAutoCommitMillis;

    SolrConnector(@Nullable SolrClient solrClient, int solrAutoCommitMillis) {
        this.solrClient = solrClient;
        this.solrAutoCommitMillis = solrAutoCommitMillis;
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
            solrClient.add(solrDoc, solrAutoCommitMillis);
            return true;
        } catch (Throwable e) {
            LOG.error("Error while adding {} to solr index", solrDoc, e);
            return false;
        }
    }

    int addDocsToIndex(Stream<SolrInputDocument> solrDocsStream, int batchSize, Map<String, String> mdcContext) {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return 0;
        }
        long startTime = System.currentTimeMillis();
        List<SolrInputDocument> solrDocsBatch = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger result = new AtomicInteger(0);
        int nDocs = solrDocsStream.map((solrDoc) -> {
                if (batchSize > 1) {
                    solrDocsBatch.add(solrDoc);
                    if (solrDocsBatch.size() >= batchSize) {
                        List<SolrInputDocument> toAdd;
                        synchronized (solrDocsBatch) {
                            toAdd = new ArrayList<>(solrDocsBatch);
                            solrDocsBatch.clear();
                        }
                        result.addAndGet(indexDocs(toAdd, result.get(), startTime, mdcContext));
                    }
                } else {
                    try {
                        solrClient.add(solrDoc, solrAutoCommitMillis);
                        result.incrementAndGet();
                    } catch (Throwable e) {
                        LOG.error("Error while updating solr index for {}", solrDoc, e);
                    }
                }
                return 1;
        }).reduce(0, Integer::sum);

        int nResults = result.addAndGet(indexDocs(solrDocsBatch, result.get(), startTime, mdcContext));
        if (nResults != nDocs) {
            LOG.warn("Number of processed documents {} does not match indexed documnents {}", nDocs, nResults);
        } else {
            LOG.info("Indexed {} streamed documents", nResults);
        }
        return nResults;
    }

    private int indexDocs(Collection<SolrInputDocument> docs, int currentDocs, long startTime, Map<String, String> mdcContext) {
        if (CollectionUtils.isNotEmpty(docs)) {
            MDC.setContextMap(mdcContext);
            try {
                long opStartTime = System.currentTimeMillis();
                LOG.debug("    Adding {} docs (+ {}, elapsed time: {}s)", docs.size(), currentDocs, (System.currentTimeMillis() - startTime) / 1000.);
                solrClient.add(docs, solrAutoCommitMillis);
                LOG.debug("    Added {} docs (+ {} in {}s)", docs.size(), currentDocs, (System.currentTimeMillis() - opStartTime) / 1000.);
                return docs.size();
            } catch (Throwable e) {
                LOG.error("Error while updating solr index with {} documents", docs.size(), e);
            }
            return 0;
        } else {
            return 0;
        }
    }

    void clearIndex() {
        if (solrClient == null) {
            LOG.debug("SOLR is not configured");
            return;
        }
        try {
            LOG.info("Clear index");
            solrClient.deleteByQuery("*:*", solrAutoCommitMillis);
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
            solrClient.deleteById(id.toString(), solrAutoCommitMillis);
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
            solrClient.deleteByQuery(q, solrAutoCommitMillis);
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
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
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
            addDocsToIndex(solrDocsStream, batchSize, mdcContext);
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
                    .map(this::idsToSolrQuery)
                    .filter(Objects::nonNull)
                    .map(SolrQuery::new)
                    .map(this::search)
                    .flatMap(queryResponse -> queryResponse.getResults().stream())
                    ;
        }
    }

    void commitChanges() {
        if (solrClient != null) {
            try {
                solrClient.commit();
            } catch (Exception e) {
                LOG.warn("Failed to commit the latest changes", e);
            }
        }
    }
}
