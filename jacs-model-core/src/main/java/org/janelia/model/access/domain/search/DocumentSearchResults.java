package org.janelia.model.access.domain.search;

import org.apache.solr.common.SolrDocumentList;
import org.janelia.model.domain.DomainObject;

import java.util.List;
import java.util.Map;

/**
 * Created by schauderd on 2/4/16.
 */
public class DocumentSearchResults {

    private List<DomainObject> domainObjects;
    private SolrDocumentList results;
    private Map<String, List<FacetValue>> facetValues;
    private long numFound = 0;

    // For Jackson deserialization
    public DocumentSearchResults() {
    }

    public DocumentSearchResults(SolrDocumentList results,
                                 Map<String, List<FacetValue>> facetValues,
                                 long numFound) {
        this.results = results;
        this.facetValues = facetValues;
        this.numFound = numFound;
    }

    public DocumentSearchResults(List<DomainObject> domainObjects,
                                 Map<String, List<FacetValue>> facetValues,
                                 long numFound) {
        this.domainObjects = domainObjects;
        this.facetValues = facetValues;
        this.numFound = numFound;
    }

    public SolrDocumentList getResults() {
        return results;
    }

    public void setResults(SolrDocumentList results) {
        this.results = results;
    }

    public Map<String, List<FacetValue>> getFacetValues() {
        return facetValues;
    }

    public void setFacetValues(Map<String, List<FacetValue>> facetValues) {
        this.facetValues = facetValues;
    }

    public long getNumFound() {
        return numFound;
    }

    public void setNumFound(long numFound) {
        this.numFound = numFound;
    }

    public List<DomainObject> getDomainObjects() {
        return domainObjects;
    }
}
