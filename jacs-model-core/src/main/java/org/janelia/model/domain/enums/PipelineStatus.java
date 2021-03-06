package org.janelia.model.domain.enums;

/**
 * Captures possible Pipeline run statuses.  Intent is to capture the status transitions (StatusTransiitons) for history purposes.
 *
 * @author <a href="mailto:schauderd@janelia.hhmi.org">David Schauder</a>
 */
public enum PipelineStatus {
    New("New"),
    Scheduled("Scheduled"),
    Queued("Queued"),
    Processing("Processing"),
    Complete("Complete"),
    Error("Error"),
    Deleted("Deleted"),
    Retired("Retired");

    private final String status;

    PipelineStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
