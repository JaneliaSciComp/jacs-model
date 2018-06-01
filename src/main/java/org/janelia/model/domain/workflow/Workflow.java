package org.janelia.model.domain.workflow;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchType;

import java.util.*;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="workflow",label="Workflow")
@SearchType(key="workflow",label="Workflow")
public class Workflow extends AbstractDomainObject {

    private List<Reference> tasks = new ArrayList<>();
    private Map<Long,Set<Long>> edges = new HashMap<>();

    public List<Reference> getTasks() {
        return tasks;
    }

    public void setTasks(List<Reference> tasks) {
        if (tasks==null) throw new IllegalArgumentException("Property cannot be null");
        this.tasks = tasks;
    }

    public Map<Long, Set<Long>> getEdges() {
        return edges;
    }

    public void setEdges(Map<Long, Set<Long>> edges) {
        if (edges==null) throw new IllegalArgumentException("Property cannot be null");
        this.edges = edges;
    }
}
