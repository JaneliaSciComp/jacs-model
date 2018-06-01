package org.janelia.model.domain.workflow;

import org.janelia.dagobah.Task;
import org.janelia.dagobah.TaskStatus;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="workflowTask",label="Workflow Task")
@SearchType(key="workflowTask",label="Workflow Task")
public class WorkflowTask extends AbstractDomainObject implements Task {

    private Long workflowId;
    private boolean hasEffects = false;
    private boolean force = false;
    private TaskStatus status = TaskStatus.Pending;
    private Map<String,Object> inputs = new HashMap<>();
    private Map<String,Object> outputs = new HashMap<>();
    private String serviceClass;

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public long getNodeId() {
        Long id = getId();
        return id==null ? -1 : id.longValue();
    }

    public boolean getHasEffects() {
        return hasEffects;
    }

    public void setHasEffects(Boolean hasEffects) {
        this.hasEffects = hasEffects;
    }

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        if (inputs==null) throw new IllegalArgumentException("Property cannot be null");
        this.inputs = inputs;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        if (outputs==null) throw new IllegalArgumentException("Property cannot be null");
        this.outputs = outputs;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

}
