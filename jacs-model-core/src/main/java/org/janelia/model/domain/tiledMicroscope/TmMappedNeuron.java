package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A tiled microscope neuron that is published and mapped to an external database, such as the ML NeuronBrowser.
 *
 * This information is stored in a separate class away from TmNeuronMetadata, because those objects
 * can be created for neuron fragments and number in the hundreds of millions, whereas this object
 * represents curated single neurons that are typically published and imported from a JSON file. This allows
 * for making these neurons searchable with metadata in SOLR, without impacting system performance with
 * indexing of many tiny fragments, which do not need to be searched by name.
 */
@SearchType(key="tmMappedNeuron",label="Tiled Microscope Mapped Neuron")
@MongoMapped(collectionName="tmMappedNeuron",label="Tiled Microscope Mapped Neuron")
public class TmMappedNeuron extends AbstractDomainObject {

    public enum DatabaseName {
        Internal,
        NeuronBrowser
    }

    @SearchAttribute(key="soma_loc_txt",label="Soma Location",facet="soma_loc_s")
    private String somaLocation;

    @SearchTraversal({})
    private Reference workspaceRef;

    @SearchTraversal({})
    private Set<Reference> neuronRefs = new HashSet<>();

    private Map<DatabaseName, String> dbXrefs = new HashMap<>();

    public TmMappedNeuron() {
    }

    public String getSomaLocation() {
        return somaLocation;
    }

    public void setSomaLocation(String somaLocation) {
        this.somaLocation = somaLocation;
    }

    public Reference getWorkspaceRef() {
        return workspaceRef;
    }

    public void setWorkspaceRef(Reference workspaceRef) {
        this.workspaceRef = workspaceRef;
    }

    public void addNeuronRef(Reference ref) {
        neuronRefs.add(ref);
    }

    public Set<Reference> getNeuronRefs() {
        return neuronRefs;
    }

    public Map<DatabaseName, String> getDbXrefs() {
        return dbXrefs;
    }

    public void setDbXrefs(Map<DatabaseName, String> dbXrefs) {
        this.dbXrefs = dbXrefs;
    }

    @JsonIgnore
    public void setCrossRefInternal(String value) {
        dbXrefs.put(DatabaseName.Internal, value);
    }

    @JsonIgnore
    @SearchAttribute(key="xref_internal_s",label="Internal identifier")
    public String getCrossRefInternal() {
        return dbXrefs.get(DatabaseName.Internal);
    }

    @JsonIgnore
    public void setCrossRefNeuronBrowser(String value) {
        dbXrefs.put(DatabaseName.NeuronBrowser, value);
    }

    @JsonIgnore
    @SearchAttribute(key="xref_neuronbrowser_s",label="NeuronBrowser identifier")
    public String getCrossRefNeuronBrowser() {
        return dbXrefs.get(DatabaseName.NeuronBrowser);
    }
}
