package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.security.Subject;
import java.util.*;

/**
 * this class captures state information about review tasks for Mouselight,
 * which can be consensus manual review tasks or semi-automated tasks driven
 * by machine learning.  The main difference between the two being a consensus
 * review is of a neuron, so it needs to capture appropriate metadata,
 * while the semi-automated tasks are mostly point driven reviews.
 */
public class TmReviewTask {
    private String category;
    private Boolean completed = false;
    private List<Map<String,String>> reviewerHistory = new ArrayList<>();
    private String title;
    private Subject owner;
    private List<TmReviewItem> reviewItems;

    public TmReviewTask() {}

    @JsonIgnore
    public String getCategory() {
        return category;
    }

    @JsonIgnore
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonIgnore
    public Boolean getCompleted() {
        return completed;
    }

    @JsonIgnore
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    @JsonIgnore
    public List<Map<String, String>> getReviewerHistory() {
        return reviewerHistory;
    }

    @JsonIgnore
    public void addReviewerHistory(Map<String, String> historyItem) {
        reviewerHistory.add(historyItem);
    }

    @JsonIgnore
    public String getTitle() {
        return title;
    }

    @JsonIgnore
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public Subject getOwner() {
        return owner;
    }

    @JsonIgnore
    public void setOwner(Subject owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public List<TmReviewItem> getReviewItems() {
        return reviewItems;
    }

    @JsonIgnore
    public void addReviewItem(TmReviewItem reviewItem) {
        reviewItems.add(reviewItem);
    }
}
