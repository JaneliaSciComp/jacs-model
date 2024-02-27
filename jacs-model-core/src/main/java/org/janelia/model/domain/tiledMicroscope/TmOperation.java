package org.janelia.model.domain.tiledMicroscope;
import java.util.Date;

public class TmOperation {
    public enum Activity {
        LOAD_TILE, ANNOTATE_NEURON, LOAD_WORKSPACE
    }
    private Long workspaceId;
    private Long neuronId;
    private String user;
    private Activity activity;
    private Date timestamp;
    private Long elapsedTime;
    public TmOperation() {
    }
    
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
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

    public Date getTimestamp() {
        return timestamp;
    }

}

