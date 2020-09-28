package org.janelia.model.domain.tiledMicroscope;
import java.util.Date;

public class TmOperation {
    private Long workspaceId;
    private Long neuronId;
    private String user;
    private String operation;
    private Date timestamp;
    public TmOperation() {
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setNeuronId(Long neuronId) {
        this.neuronId = neuronId;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getNeuronId() {
        return neuronId;
    }

    public String getUser() {
        return user;
    }

    public String getOperation() {
        return operation;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}

