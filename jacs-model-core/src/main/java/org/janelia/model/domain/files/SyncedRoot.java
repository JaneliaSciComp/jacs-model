package org.janelia.model.domain.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a synchronized path to a top-level folder that is periodically searched for data sets
 * (n5 containers, zarr containers, TM samples, etc.)
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="syncedRoot",label="Synchronized Folder")
public class SyncedRoot extends SyncedPath implements IsParent {

    private List<DiscoveryAgentType> discoveryAgents = new ArrayList<>();

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
}
