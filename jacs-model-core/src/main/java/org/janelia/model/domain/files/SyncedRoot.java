package org.janelia.model.domain.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.search.Filtering;
import org.janelia.model.domain.gui.search.criteria.AttributeValueCriteria;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;
import org.janelia.model.domain.workspace.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a synchronized path to a top-level folder that is periodically searched for data sets
 * (N5 containers, Zarr containers, TM samples, etc.)
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="syncedRoot",label="Synchronized Folder")
@MongoMapped(collectionName="syncedRoot",label="Synchronized Folder")
public class SyncedRoot extends SyncedPath implements Node, Filtering {

    /** Depth to search */
    private int depth = 2;

    /** Agents to use during discovery */
    private List<DiscoveryAgentType> discoveryAgents = new ArrayList<>();

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

    @JsonIgnore
    private List<Criteria> lazyCriteria;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Returns the DiscoveryAgentTypes representing the agents that are to be used by the synchronization
     * service when processing this root.
     * @see DiscoveryAgentType
     * @return list of agent types
     */
    public List<DiscoveryAgentType> getDiscoveryAgents() {
        return discoveryAgents;
    }

    public void setDiscoveryAgents(List<DiscoveryAgentType> discoveryAgents) {
        this.discoveryAgents = discoveryAgents;
    }

    @JsonIgnore
    public void addDiscoveryAgent(DiscoveryAgentType discoveryAgent) {
        discoveryAgents.add(discoveryAgent);
    }

    @JsonIgnore
    public void removeDiscoveryAgent(String discoveryAgent) {
        discoveryAgents.remove(discoveryAgent);
    }

    /* implement Node interface */

    @Override
    public List<Reference> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Reference> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }

    /* implement Filtering interface */

    @JsonIgnore
    @Override
    public String getSearchClass() {
        return SyncedPath.class.getName();
    }

    @JsonIgnore
    @Override
    public boolean hasCriteria() {
        return true;
    }

    @JsonIgnore
    @Override
    public String getSearchString() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<Criteria> getCriteriaList() {
        if (lazyCriteria==null) {
            lazyCriteria = new ArrayList<>();
            AttributeValueCriteria existsInStorage = new AttributeValueCriteria();
            existsInStorage.setAttributeName("existsInStorage");
            existsInStorage.setValue("true");
            lazyCriteria.add(existsInStorage);
            AttributeValueCriteria rootRef = new AttributeValueCriteria();
            rootRef.setAttributeName("rootRef");
            rootRef.setValue(toString());
            lazyCriteria.add(rootRef);
        }
        return lazyCriteria;
    }
}
