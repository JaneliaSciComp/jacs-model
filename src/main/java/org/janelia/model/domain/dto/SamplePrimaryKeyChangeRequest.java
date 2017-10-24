package org.janelia.model.domain.dto;

import java.util.List;

/**
 * Parameter for the changeSamplePrimaryKey web service.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SamplePrimaryKeyChangeRequest {

    private List<Integer> sageIds;
    private String oldDataSet;
    private String oldSlideCode;
    private String newDataSet;
    private String newSlideCode;
   
    public SamplePrimaryKeyChangeRequest() {
    }

    public List<Integer> getSageIds() {
        return sageIds;
    }

    public void setSageIds(List<Integer> sageIds) {
        this.sageIds = sageIds;
    }

    public String getOldDataSet() {
        return oldDataSet;
    }

    public void setOldDataSet(String oldDataSet) {
        this.oldDataSet = oldDataSet;
    }

    public String getOldSlideCode() {
        return oldSlideCode;
    }

    public void setOldSlideCode(String oldSlideCode) {
        this.oldSlideCode = oldSlideCode;
    }

    public String getNewDataSet() {
        return newDataSet;
    }

    public void setNewDataSet(String newDataSet) {
        this.newDataSet = newDataSet;
    }

    public String getNewSlideCode() {
        return newSlideCode;
    }

    public void setNewSlideCode(String newSlideCode) {
        this.newSlideCode = newSlideCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SamplePrimaryKeyChangeRequest [");
        if (sageIds != null) {
            builder.append("sageIds=");
            builder.append(sageIds);
            builder.append(", ");
        }
        if (oldDataSet != null) {
            builder.append("oldDataSet=");
            builder.append(oldDataSet);
            builder.append(", ");
        }
        if (oldSlideCode != null) {
            builder.append("oldSlideCode=");
            builder.append(oldSlideCode);
            builder.append(", ");
        }
        if (newDataSet != null) {
            builder.append("newDataSet=");
            builder.append(newDataSet);
            builder.append(", ");
        }
        if (newSlideCode != null) {
            builder.append("newSlideCode=");
            builder.append(newSlideCode);
        }
        builder.append("]");
        return builder.toString();
    }
}
