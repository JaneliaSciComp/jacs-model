package org.janelia.model.domain.sample;

import java.io.Serializable;
import java.util.Date;

import org.janelia.model.domain.interfaces.HasFilepath;

/**
 * An error in processing a Sample. The filepath points to a file 
 * containing the stacktrace of the exception that was thrown.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class PipelineError implements HasFilepath, Serializable {

    private String filepath;
    private String operation;
    private String classification;
    private String description;
    private Date creationDate;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
