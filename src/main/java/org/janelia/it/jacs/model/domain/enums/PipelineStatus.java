package org.janelia.it.jacs.model.domain.enums;

/**
 * Captures possible Pipeline run statuses.  Intent is to capture the status transitions (StatusTransiitons) for history purposes.
 *
 * @author <a href="mailto:schauderd@janelia.hhmi.org">David Schauderi</a>
 */
public enum PipelineStatus {
    New("New"),
    Scheduled("Scheduled"),
    Queued("Queued"),
    Processing("Processing"),
    Complete("Complete"),
    Error("Error"),
    Retired("Retired");

    private final String status;
    private PipelineStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
