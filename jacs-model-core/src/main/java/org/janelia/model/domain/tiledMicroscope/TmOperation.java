package org.janelia.model.domain.tiledMicroscope;
import java.util.Date;

public class TmOperation {
    public enum Activity {
        LOAD_KTX_TILE, LOAD_ZARR_TILE, ANNOTATE_NEURON, LOAD_WORKSPACE,
        ROTATE_SCREEN,
        UPDATE_ANCHOR,
        CLEAR_PARENT_ANCHOR,
        CENTER_CURRENT_ANCHOR,
        RECENTER_3D_VIEW,
        DELETE_NEURON_SUBTREE,
        SET_VERTEX_NEURON_ROOT,
        SPLIT_NEURON_EDGE_VERTICES,
        SPLIT_NEURITE_AT_VERTEX,
        HIDE_NEURON,
        MERGE_NEURITES,
        CREATE_NEURON,
        SELECT_NEURON,
        SELECT_PARENT_VERTEX,
        APPEND_VERTEX,
        MOVE_VERTEX,
        DELETE_VERTEX,
        SELECT_VERTEX
    }
    private Long sampleId;
    private Long workspaceId;
    private Long neuronId;
    private Long vertexId;
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

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
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

    public Long getVertexId() {
        return vertexId;
    }

    public void setVertexId(Long vertexId) {
        this.vertexId = vertexId;
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

