package org.janelia.model.access.domain;

import com.mongodb.MongoClient;
import org.janelia.dagobah.DAG;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workflow.Workflow;
import org.janelia.model.domain.workflow.WorkflowTask;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Persistence for workflows
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class WorkflowDAO extends BaseDAO {

    private static final Logger log = LoggerFactory.getLogger(DomainDAO.class);

    protected DomainDAO domainDao;
    protected MongoCollection workflowCollection;
    protected MongoCollection taskCollection;

    public WorkflowDAO(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
        this.domainDao = new DomainDAO(mongoClient, databaseName);
        this.workflowCollection = getCollectionByClass(Workflow.class);
        this.taskCollection = getCollectionByClass(WorkflowTask.class);
    }

    /**
     * Saves the given in-memory DAG representation into the database.
     * Returns the workflow with references to the saved tasks. The actual
     * tasks can be retrieved with the getTasks() method.
     * @param dag
     * @return
     */
    public Workflow saveDAG(DAG<WorkflowTask> dag) {

        // Create Workflow
        Workflow workflow = new Workflow();
        workflow.setId(getNewId());

        // Ensure all tasks have been persisted
        for (WorkflowTask workflowTask : dag.getNodes()) {
            workflowTask.setWorkflowId(getNewId());
            saveTask(workflowTask);
        }

        // Add task references
        for (WorkflowTask workflowTask : dag.getNodes()) {
            workflow.getTasks().add(Reference.createFor(workflowTask));
        }

        // Add edges
        Map<Long, Set<Long>> edges = dag.getEdges();
        for (Long sourceId : edges.keySet()) {
            Set<Long> sourceEdges = new HashSet<>();
            sourceEdges.addAll(edges.get(sourceId));
            workflow.getEdges().put(sourceId, sourceEdges);
        }

        return saveWorkflow(workflow);
    }

    /**
     * Returns an in-memory DAG representation built from a serialized Workflow.
     * The Workflow must exist in the database, since this method calls getTasks to
     * retrieve the workflow's tasks.
     * @param workflow persisted workflow
     * @return in-memory DAG
     */
    public DAG<WorkflowTask> getDAG(Workflow workflow) {

        Collection<WorkflowTask> tasks = getTasks(workflow);
        DAG<WorkflowTask> dag = new DAG<>();
        for (WorkflowTask task : tasks) {
            dag.addNode(task);
        }

        Map<Long, Set<Long>> edges = workflow.getEdges();
        for (Long sourceId : edges.keySet()) {
            WorkflowTask source = dag.getNodeById(sourceId);
            if (source==null) {
                throw new AssertionError("Missing WorkflowTask#"+sourceId);
            }
            for (Long targetId : edges.get(sourceId)) {
                WorkflowTask target = dag.getNodeById(targetId);
                if (target==null) {
                    throw new AssertionError("Missing WorkflowTask#"+targetId);
                }
                dag.addEdge(source, target);
            }
        }

        return dag;
    }

    public Collection<WorkflowTask> getTasks(Workflow workflow) {
        return domainDao.getDomainObjectsAs(workflow.getOwnerKey(), workflow.getTasks());
    }

    public Workflow saveWorkflow(Workflow workflow) {
        workflowCollection.update("{_id:#}", workflow.getId()).upsert().with(workflow);
        return workflow;
    }

    public WorkflowTask saveTask(WorkflowTask workflowTask) {
        taskCollection.update("{_id:#}", workflowTask.getId()).upsert().with(workflowTask);
        return workflowTask;
    }

    public Workflow getWorkflow(Long workflowId) {
        return domainDao.getDomainObject(null, Workflow.class, workflowId);
    }

    public WorkflowTask getWorkflowTask(Long taskId) {
        return domainDao.getDomainObject(null, WorkflowTask.class, taskId);
    }

}
