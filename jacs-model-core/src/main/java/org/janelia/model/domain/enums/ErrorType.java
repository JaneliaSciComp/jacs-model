package org.janelia.model.domain.enums;

/**
 * Classification of pipeline errors.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum ErrorType {
    
    RecoverableError("Recoverable Error", "Probably recoverable. We'll try to run this sample 3 times."),
    LabError("Lab Error", "There are inconsistencies in the Sample's metadata which the pipeline cannot resolve. Data was probably entered incorrectly into TMOG."),
    DiskQuotaError("Disk Quota Error", "The user must take action before this sample can be processed by creating more space in their filestore."),
    ComputeError("Compute Error", "There was an error during pipeline computation. This may indicate a bug, or an unexpected input."),
    UnclassifiedError("Unclassified Error", "We don't know how to automatically classify this error. It must be manually investigated.");
    
    private final String label;
    private final String description;
    
    private ErrorType(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}