package org.janelia.it.jacs.model.domain.enums;

/**
 * Captures overall order statuses.  Useful for statistics on processing.
 *
 * @author <a href="mailto:schauderd@janelia.hhmi.org">David Schauder</a>
 */
public enum OrderStatus {
    Intake("Intake"),
    Processing("Processing"),
    Completed("Complete");

    private final String status;
    private OrderStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
