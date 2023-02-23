package org.janelia.model.access.domain.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainObjectGetter;
import org.janelia.model.domain.ontology.DomainAnnotationGetter;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.access.domain.nodetools.NodeAncestorsGetter;
import org.slf4j.MDC;

public class SolrBasedDomainObjectIndexer implements DomainObjectIndexer {

    private final SolrConnector solrConnector;
    private final DomainObject2SolrDoc domainObject2SolrDocConverter;
    private final int solrBatchSize;

    public SolrBasedDomainObjectIndexer(SolrClient solrClient,
                                        List<NodeAncestorsGetter<? extends Node>> nodeAncestorsGetters,
                                        DomainAnnotationGetter nodeAnnotationGetter,
                                        DomainObjectGetter objectGetter,
                                        int solrBatchSize,
                                        int solrAutoCommitMillis) {
        this.solrConnector = new SolrConnector(solrClient, solrAutoCommitMillis);
        this.domainObject2SolrDocConverter = new DomainObject2SolrDoc(nodeAncestorsGetters, nodeAnnotationGetter, objectGetter);
        this.solrBatchSize = solrBatchSize;
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
        return solrConnector.addDocToIndex(domainObject2SolrDocConverter.domainObjectToSolrDocument(domainObject));
    }

    @Override
    public boolean removeDocument(Long docId) {
        return solrConnector.removeDocIdFromIndex(docId);
    }

    @Override
    public int indexDocumentStream(Stream<? extends DomainObject> domainObjectStream) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return solrConnector.addDocsToIndex(
                domainObjectStream.map(domainObject2SolrDocConverter::domainObjectToSolrDocument),
                solrBatchSize,
                mdcContext
        );
    }

    @Override
    public int removeDocumentStream(Stream<Long> docIdsStream) {
        return solrConnector.removeDocIdsFromIndex(docIdsStream, solrBatchSize);
    }

    @Override
    public void removeIndex() {
        solrConnector.clearIndex();
    }

    @Override
    public void updateDocsAncestors(Set<Long> docIds, Long ancestorId) {
        solrConnector.updateDocsAncestors(docIds, ancestorId, solrBatchSize);
    }

    @Override
    public void commitChanges() {
        solrConnector.commitChanges();
    }
}
