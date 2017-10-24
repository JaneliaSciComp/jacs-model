package org.janelia.model.domain.report;

/**
 * Usage information for a group's (i.e. lab's) filestore quota. 
 *  *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class QuotaUsage {

	private String lab;
	private Double spaceUsedTB;
	private Double totalSpaceTB;
	private Long totalFiles;
	private Double percentUsage;
	
    public String getLab() {
        return lab;
    }
    public void setLab(String lab) {
        this.lab = lab;
    }
    public Double getSpaceUsedTB() {
        return spaceUsedTB;
    }
    public void setSpaceUsedTB(Double spaceUsedTB) {
        this.spaceUsedTB = spaceUsedTB;
    }
    public Double getTotalSpaceTB() {
        return totalSpaceTB;
    }
    public void setTotalSpaceTB(Double totalSpaceTB) {
        this.totalSpaceTB = totalSpaceTB;
    }
    public Long getTotalFiles() {
        return totalFiles;
    }
    public void setTotalFiles(Long totalFiles) {
        this.totalFiles = totalFiles;
    }
    public Double getPercentUsage() {
        return percentUsage;
    }
    public void setPercentUsage(Double percentUsage) {
        this.percentUsage = percentUsage;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("QuotaUsage [");
        if (lab != null) {
            builder.append("lab=");
            builder.append(lab);
            builder.append(", ");
        }
        if (spaceUsedTB != null) {
            builder.append("spaceUsedTB=");
            builder.append(spaceUsedTB);
            builder.append(", ");
        }
        if (totalSpaceTB != null) {
            builder.append("totalSpaceTB=");
            builder.append(totalSpaceTB);
            builder.append(", ");
        }
        if (totalFiles != null) {
            builder.append("totalFiles=");
            builder.append(totalFiles);
            builder.append(", ");
        }
        if (percentUsage != null) {
            builder.append("percentUsage=");
            builder.append(percentUsage);
        }
        builder.append("]");
        return builder.toString();
    }
}
