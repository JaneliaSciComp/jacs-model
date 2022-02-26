package org.janelia.model.domain.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.ReverseReference;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a synchronized path to a top-level folder that is periodically searched for data sets
 * (N5 containers, Zarr containers, TM samples, etc.)
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="syncedRoot",label="Synchronized Folder")
public class SyncedRoot extends SyncedPath implements IsParent {

    /** Depth to search */
    private int depth = 2;

    /** Agents to use during discovery */
    private List<DiscoveryAgentType> discoveryAgents = new ArrayList<>();

    /** Reference to discovered paths */
    private ReverseReference paths;

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

    public ReverseReference getPaths() {
        return paths;
    }

    public void setPaths(ReverseReference paths) {
        this.paths = paths;
    }
}
