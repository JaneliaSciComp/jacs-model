package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;


public class TmPointListReviewItem implements TmReviewItem {
    private String pathname;
    private Long workspaceId;
    private List<List<Long>> pointList  = new ArrayList<>();
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
        return pointList;
    }

    @Override
    public void addReviewItem(Object item) {
        pointList.add((List<Long>)item);

    }

    @JsonIgnore
    @Override
    public Long getWorkspaceId() {
        return workspaceId;
    }

    @JsonIgnore
    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }
}
