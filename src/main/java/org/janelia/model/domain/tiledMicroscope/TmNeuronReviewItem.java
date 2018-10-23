package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;

@JsonTypeName("neuronreview")
public class TmNeuronReviewItem implements TmReviewItem {
    private String pathname;
    private String workspaceRef;
    private Long neuronId;
    private List<Long> annotationIds = new ArrayList<>();
    private boolean reviewed;

    @Override
    public boolean isReviewed() {
        return reviewed;
    }

    @Override
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    @Override
    public void setName(String name) {
        pathname = name;
    }

    @Override
    public String getName() {
        return pathname;
    }

    @Override
    public List getReviewItems() {
        return annotationIds;
    }

    @Override
    @JsonIgnore
    public void addReviewItem(Object item) {
        annotationIds.add((Long)item);

    }

    @Override
    public String getWorkspaceRef() {
        return workspaceRef;
    }

    @Override
    public void setWorkspaceRef(String workspaceRef) {
        this.workspaceRef = workspaceRef;
    }

    public Long getNeuronId() {
        return neuronId;
    }

    public void setNeuronId(Long neuronId) {
        this.neuronId = neuronId;
    }

}
