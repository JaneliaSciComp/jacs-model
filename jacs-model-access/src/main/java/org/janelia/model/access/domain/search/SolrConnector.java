package org.janelia.model.access.domain.search;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A SOLR connector.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SolrConnector {
    private static final Logger LOG = LoggerFactory.getLogger(SolrConnector.class);

    private final SolrServer solrServer;
    private final int solrCommitDelayInMillis;

    public SolrConnector(@Nullable SolrServer solrServer, int solrCommitDelayInMillis) {
        this.solrServer = solrServer;
        this.solrCommitDelayInMillis = solrCommitDelayInMillis;
    }

    /**
     * Run the given query against the index.
     *
     * @param query
     * @return
     */
    public QueryResponse search(SolrQuery query) {
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

    public boolean addDocToIndex(SolrInputDocument solrDoc) {
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

    public boolean addDocsToIndex(Stream<SolrInputDocument> solrDocsStream, int batchSize) {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        final AtomicInteger counter = new AtomicInteger();
        int actualBatchSize = Math.max(batchSize, 1);
        // group documents in batches of given size to send the to solr
        return solrDocsStream
                .collect(Collectors.groupingBy(solrDoc -> counter.getAndIncrement() / actualBatchSize,
                        Collectors.collectingAndThen(Collectors.toList(), solrDocsBatch -> {
                            try {
                                solrServer.add(solrDocsBatch, solrCommitDelayInMillis);
                                solrServer.commit(false, false);
                                return true;
                            } catch (Exception e) {
                                LOG.error("Error while updating solr index for {}", solrDocsBatch, e);
                                return false;
                            }
                        })))
                .entrySet().stream()
                .map(e -> e.getValue())
                .reduce((e1, e2) -> e1 && e2)
                .orElse(false)
                ;
    }

    public void clearIndex() {
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

    public boolean removeFromIndexById(String id) {
        if (solrServer == null) {
            LOG.debug("SOLR is not configured");
            return false;
        }
        try {
            solrServer.deleteById(id, solrCommitDelayInMillis);
            solrServer.commit(false, false);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public SolrInputDocument createSolrDoc(DomainObject domainObject, Set<Long> ancestorIds) {
        SolrInputDocument solrDoc = new SolrInputDocument();
        solrDoc.setField("doc_type", SearchableDocType.DOCUMENT.name(), 1.0f);
        solrDoc.setField("class", domainObject.getClass().getName(), 1.0f);
        solrDoc.setField("collection", DomainUtils.getCollectionName(domainObject), 1.0f);
        solrDoc.setField("ancestor_ids", new ArrayList<>(ancestorIds), 0.2f);

        BiConsumer<SearchAttribute, Object> searchFieldHandler = (searchAttribute, fieldValue) -> {
            if (fieldValue == null || fieldValue instanceof String && StringUtils.isBlank((String) fieldValue)) {
                solrDoc.removeField(searchAttribute.key());
                if (StringUtils.isNotEmpty(searchAttribute.facet())) {
                    solrDoc.removeField(searchAttribute.facet());
                }
            } else {
                solrDoc.addField(searchAttribute.key(), fieldValue, 1.0f);
                if (StringUtils.isNotEmpty(searchAttribute.facet())) {
                    solrDoc.addField(searchAttribute.facet(), fieldValue, 1.0f);
                }
            }
        };

        Set<Field> searchableFields = ReflectionUtils.getAllFields(domainObject.getClass(), ReflectionUtils.withAnnotation(SearchAttribute.class));
        for (Field field : searchableFields) {
            try {
                SearchAttribute searchAttributeAnnot = field.getAnnotation(SearchAttribute.class);
                Object value = ReflectionHelper.getFieldValue(domainObject, field.getName());
                searchFieldHandler.accept(searchAttributeAnnot, value);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such field " + field.getName() + " on object " + domainObject, e);
            }
        }

        Set<Method> searchableProperties = ReflectionUtils.getAllMethods(domainObject.getClass(), ReflectionUtils.withAnnotation(SearchAttribute.class));
        for (Method propertyMethod : searchableProperties) {
            try {
                SearchAttribute searchAttributeAnnot = propertyMethod.getAnnotation(SearchAttribute.class);
                Object value = propertyMethod.invoke(domainObject);
                searchFieldHandler.accept(searchAttributeAnnot, value);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalArgumentException("Problem executing " + propertyMethod.getName() + " on object " + domainObject, e);
            }
        }

        return solrDoc;
    }

    @SuppressWarnings("unchecked")
    public void addAncestorIdToAllDocs(Long ancestorDocId, List<Long> solrDocIds, int batchSize) {
        if (solrServer != null) {
            Stream<SolrInputDocument> solrDocsStream = searchByDocIds(solrDocIds, batchSize)
                    .map(solrDoc -> ClientUtils.toSolrInputDocument(solrDoc))
                    .map(solrInputDoc -> {
                        Collection<Long> ancestorIds;
                        SolrInputField field = solrInputDoc.getField("ancestor_ids");
                        if (field == null || field.getValue() == null) {
                            ancestorIds = new ArrayList<>();
                        } else {
                            ancestorIds = (Collection<Long>) field.getValue();
                        }
                        ancestorIds.add(ancestorDocId);
                        solrInputDoc.setField("ancestor_ids", ancestorIds, 0.2f);
                        return solrInputDoc;
                    });
            addDocsToIndex(solrDocsStream, batchSize);
        }
    }

    private Stream<SolrDocument> searchByDocIds(List<Long> solrDocIds, int batchSize) {
        if (CollectionUtils.isEmpty(solrDocIds)) {
            return Stream.of();
        } else {
            final AtomicInteger counter = new AtomicInteger();
            Collection<List<Long>> solrDocIdsPartitions = batchSize > 0
                    ? solrDocIds.stream().collect(Collectors.groupingBy(docId -> counter.getAndIncrement() / batchSize)).values()
                    : Collections.singleton(solrDocIds);
            return solrDocIdsPartitions.stream()
                    .map(partition -> partition.stream().map(id -> "id:" + id.toString()).reduce((id1, id2) -> id1 + " OR " + id2).orElse(null))
                    .filter(queryStr -> queryStr != null)
                    .map(SolrQuery::new)
                    .map(this::search)
                    .flatMap(queryResponse -> queryResponse.getResults().stream())
                    ;
        }
    }

}