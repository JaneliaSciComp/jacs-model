package org.janelia.model.access.domain.search;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.searchable.SearchableDocType;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.workspace.DirectNodeAncestorsGetter;
import org.janelia.model.domain.workspace.NodeUtils;
import org.janelia.model.util.ReflectionHelper;
import org.reflections.ReflectionUtils;

public class SolrBasedDomainObjectIndexer implements DomainObjectIndexer {

    private final SolrConnector solrConnector;
    private final DirectNodeAncestorsGetter directNodeAncestorsGetter;
    private final int solrBatchSize;
    private final int solrCommitSize;

    public SolrBasedDomainObjectIndexer(SolrServer solrServer,
                                        DirectNodeAncestorsGetter directNodeAncestorsGetter,
                                        int solrBatchSize,
                                        int solrCommitSize,
                                        int solrCommitDelayInMillis) {
        this.solrConnector = new SolrConnector(solrServer, solrCommitDelayInMillis);
        this.directNodeAncestorsGetter = directNodeAncestorsGetter;
        this.solrBatchSize = solrBatchSize;
        this.solrCommitSize = solrCommitSize;
    }

    @Override
    public DocumentSearchResults searchIndex(DocumentSearchParams searchParams) {
        SolrQuery query = SolrQueryBuilder.deSerializeSolrQuery(searchParams);
        query.setFacetMinCount(1);
        query.setFacetLimit(500);
        QueryResponse response = solrConnector.search(query);
        Map<String, List<FacetValue>> facetFieldValueMap = new HashMap<>();
        if (response.getFacetFields() != null) {
            for (final FacetField ff : response.getFacetFields()) {
                List<FacetValue> facetValues = new ArrayList<>();
                if (ff.getValues() != null) {
                    for (final FacetField.Count count : ff.getValues()) {
                        facetValues.add(new FacetValue(count.getName(), count.getCount()));
                    }
                }
                facetFieldValueMap.put(ff.getName(), facetValues);
            }
        }
        long numResults = response.getResults() != null
                ? response.getResults().getNumFound()
                : 0L;
        return new DocumentSearchResults(response.getResults(), facetFieldValueMap, numResults);
    }

    @Override
    public boolean indexDocument(DomainObject domainObject) {
        return solrConnector.addDocToIndex(domainObjectToSolrDocument(domainObject));
    }

    @Override
    public int indexDocumentStream(Stream<? extends DomainObject> domainObjectStream) {
        return solrConnector.addDocsToIndex(
                domainObjectStream.map(this::domainObjectToSolrDocument),
                solrBatchSize,
                solrCommitSize
        );
    }

    private SolrInputDocument domainObjectToSolrDocument(DomainObject domainObject) {
        Set<Long> domainObjectAncestorsIds = new LinkedHashSet<>();
        NodeUtils.traverseAllAncestors(
                Reference.createFor(domainObject),
                directNodeAncestorsGetter,
                n -> domainObjectAncestorsIds.add(n.getTargetId()));
        return createSolrDoc(domainObject, domainObjectAncestorsIds);
    }

    @SuppressWarnings("unchecked")
    private SolrInputDocument createSolrDoc(DomainObject domainObject, Set<Long> ancestorIds) {
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

    @Override
    public boolean removeDocument(Long docId) {
        return solrConnector.removeDocIdFromIndex(docId);
    }

    @Override
    public int removeDocumentStream(Stream<Long> docIdsStream) {
        return solrConnector.removeDocIdsFromIndex(docIdsStream, solrBatchSize, solrCommitSize);
    }

    @Override
    public void removeIndex() {
        solrConnector.clearIndex();
    }

    @Override
    public void updateDocsAncestors(Set<Long> docIds, Long ancestorId) {
        solrConnector.updateDocsAncestors(docIds, ancestorId, solrBatchSize, solrCommitSize);
    }
}
