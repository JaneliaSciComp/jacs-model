package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.MongoMapped;
import java.util.*;

/**
 * this class captures state information about review tasks for Mouselight,
 * which can be consensus manual review tasks or semi-automated tasks driven
 * by machine learning.  The main difference between the two being a consensus
 * review is of a neuron, so it needs to capture appropriate metadata,
 * while the semi-automated tasks are mostly point driven reviews.
 */

@MongoMapped(collectionName="tmReviewTask",label="Tiled Microscope Review Tasks")
public class TmReviewTask extends AbstractDomainObject {
    private String category;
    private Boolean completed = false;
    private List<Map<String,String>> reviewerHistory = new ArrayList<>();
    private String title;
    private List<TmReviewItem> reviewItems = new ArrayList<>();
    private String workspaceRef;

    public TmReviewTask() {}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public List<Map<String, String>> getReviewerHistory() {
        return reviewerHistory;
    }

    public void addReviewerHistory(Map<String, String> historyItem) {
        reviewerHistory.add(historyItem);
    }

    public String getTitle() {
        return title;
    }

    public String getWorkspaceRef() {
        return workspaceRef;
    }

    public void setWorkspaceRef(String workspaceRef) {
        this.workspaceRef = workspaceRef;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TmReviewItem> getReviewItems() {
        return reviewItems;
    }

    public void addReviewItem(TmReviewItem reviewItem) {
        reviewItems.add(reviewItem);
    }
}
