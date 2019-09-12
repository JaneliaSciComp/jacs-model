package org.janelia.model.access.domain.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainObjectGetter;
import org.janelia.model.domain.ontology.DomainAnnotationGetter;
import org.janelia.model.domain.workspace.NodeAncestorsGetter;

public class SolrBasedDomainObjectIndexer implements DomainObjectIndexer {

    private final SolrConnector solrConnector;
    private final DomainObject2SolrDoc domainObject2SolrDocConverter;
    private final int solrBatchSize;
    private final int solrCommitSize;

    public SolrBasedDomainObjectIndexer(SolrServer solrServer,
                                        NodeAncestorsGetter nodeAncestorsGetter,
                                        DomainAnnotationGetter nodeAnnotationGetter,
                                        DomainObjectGetter objectGetter,
                                        int solrBatchSize,
                                        int solrCommitSize) {
        this.solrConnector = new SolrConnector(solrServer);
        this.domainObject2SolrDocConverter = new DomainObject2SolrDoc(nodeAncestorsGetter, nodeAnnotationGetter, objectGetter);
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
        return solrConnector.addDocToIndex(domainObject2SolrDocConverter.domainObjectToSolrDocument(domainObject));
    }

    @Override
    public int indexDocumentStream(Stream<? extends DomainObject> domainObjectStream) {
        return solrConnector.addDocsToIndex(
                domainObjectStream.map(domainObject2SolrDocConverter::domainObjectToSolrDocument),
                solrBatchSize,
                solrCommitSize
        );
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
