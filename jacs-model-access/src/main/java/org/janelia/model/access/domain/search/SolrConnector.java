package org.janelia.model.access.domain.search;

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.searchable.SearchableDocType;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.util.ReflectionHelper;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A SOLR connector.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
class SolrConnector {
    private static final Logger LOG = LoggerFactory.getLogger(SolrConnector.class);

    private final SolrServer solrServer;
    private final int solrCommitDelayInMillis;

    SolrConnector(@Nullable SolrServer solrServer, int solrCommitDelayInMillis) {
        this.solrServer = solrServer;
        this.solrCommitDelayInMillis = solrCommitDelayInMillis;
    }

    /**
     * Run the given query against the index.
     *
     * @param query
     * @return
     */
    QueryResponse search(SolrQuery query) {
        LOG.trace("search(query={})", query.getQuery());
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return new QueryResponse();
        }
        LOG.debug("Running SOLR query: {}", query);
        try {
            return solrServer.query(query);
        } catch (SolrServerException e) {
            LOG.error("Search error for {}", query, e);
            throw new IllegalStateException(e);
        }
    }

    boolean addDocToIndex(SolrInputDocument solrDoc) {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        try {
            solrServer.add(solrDoc, solrCommitDelayInMillis);
            solrServer.commit(false, false);
            return true;
        } catch (Exception e) {
            LOG.error("Error while adding {} to solr index", solrDoc, e);
            return false;
        }
    }

    boolean addDocsToIndex(Stream<SolrInputDocument> solrDocsStream, int batchSize) {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        List<SolrInputDocument> solrDocsBatch = new ArrayList<>();

        AtomicBoolean result = new AtomicBoolean(true);
        // group documents in batches of given size to send the to solr
        solrDocsStream
                .forEach(solrDoc -> {
                    if (batchSize > 1) {
                        solrDocsBatch.add(solrDoc);
                        if (solrDocsBatch.size() >= batchSize) {
                            try {
                                solrServer.add(solrDocsBatch, solrCommitDelayInMillis);
                                solrServer.commit(false, false);
                            } catch (Exception e) {
                                LOG.error("Error while updating solr index for {}", solrDocsBatch, e);
                                result.set(false);
                            } finally {
                                solrDocsBatch.clear();
                            }
                        }
                    } else {
                        try {
                            solrServer.add(solrDoc, solrCommitDelayInMillis);
                            solrServer.commit(false, false);
                        } catch (Exception e) {
                            LOG.error("Error while updating solr index for {}", solrDoc, e);
                            result.set(false);
                        }
                    }
                })
                ;
        if (solrDocsBatch.size() > 0) {
            try {
                solrServer.add(solrDocsBatch, solrCommitDelayInMillis);
                solrServer.commit(false, false);
            } catch (Exception e) {
                LOG.error("Error while updating solr index for {}", solrDocsBatch, e);
                result.set(false);
            } finally {
                solrDocsBatch.clear();
            }
        }
        return result.get();
    }

    void clearIndex() {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return;
        }
        try {
            solrServer.deleteByQuery("*:*", solrCommitDelayInMillis);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    boolean removeDocIdFromIndex(Long id) {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        try {
            solrServer.deleteById(id.toString(), solrCommitDelayInMillis);
            solrServer.commit(false, false);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    boolean removeDocIdsFromIndex(Stream<Long> docIdsStream, int batchSize) {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        AtomicBoolean result = new AtomicBoolean(true);
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
            if (!removeDocIds(remainingIds)) {
                result.set(false);
            }
        }
        return result.get();
    }

    private void removeHandler(List<Long> ids, Long id, int batchSize, AtomicBoolean result) {
        if (batchSize > 1) {
            ids.add(id);
            if (ids.size() >= batchSize) {
                if (!removeDocIds(ids)) {
                    result.set(false);
                }
                ids.clear();
            }
        } else {
            removeDocIdFromIndex(id);
        }
    }

    private boolean removeDocIds(List<Long> ids) {
        String q = idsToSolrQuery(ids);
        try {
            solrServer.deleteByQuery(q, solrCommitDelayInMillis);
            solrServer.commit(false, false);
            return true;
        } catch (Exception e) {
            LOG.error("Error trying to delete using query: {}", q, e);
            return false;
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
        if (solrServer != null) {
            Stream<SolrInputDocument> solrDocsStream = searchByDocIds(docIds, batchSize)
                    .map(solrDoc -> ClientUtils.toSolrInputDocument(solrDoc))
                    .map(solrInputDoc -> {
                        Collection<Long> ancestorIds;
                        SolrInputField field = solrInputDoc.getField("ancestor_ids");
                        if (field == null || field.getValue() == null) {
                            ancestorIds = new ArrayList<>();
                        } else {
                            ancestorIds = (Collection<Long>) field.getValue();
                        }
                        ancestorIds.add(ancestorId);
                        solrInputDoc.setField("ancestor_ids", ancestorIds, 0.2f);
                        return solrInputDoc;
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
