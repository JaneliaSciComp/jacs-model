package org.janelia.model.domain.workflow;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;

import java.util.*;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class WorkflowDAG extends AbstractDomainObject {

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

//    public void putComputeDAG(DAG<WorkflowTask> dag) {
//        tasks.clear();
//        edges.clear();
//        for (WorkflowTask workflowTask : dag.getNodes()) {
//            tasks.add(Reference.createFor(workflowTask));
//        }
//        Map<Long, Set<Long>> edges = dag.getEdges();
//        for (Long sourceId : edges.keySet()) {
//            Set<Long> sourceEdges = new HashSet<>();
//            sourceEdges.addAll(edges.get(sourceId));
//            this.edges.put(sourceId, sourceEdges);
//        }
//    }
//
//    public DAG<WorkflowTask> getComputeDAG(List<WorkflowTask> tasks) {
//        DAG<WorkflowTask> dag = new DAG<>();
//        for (Reference taskRef : tasks) {
//            dag.addNode(task);
//        }
//        for (Long sourceId : edges.keySet()) {
//            WorkflowTask source = dag.getNodeById(sourceId);
//            for (Long targetId : edges.get(sourceId)) {
//                WorkflowTask target = dag.getNodeById(targetId);
//                dag.addEdge(source, target);
//            }
//        }
//        return dag;
//    }
}
