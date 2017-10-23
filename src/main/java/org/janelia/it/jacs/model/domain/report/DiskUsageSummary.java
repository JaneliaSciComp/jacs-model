package org.janelia.it.jacs.model.domain.report;

public class DiskUsageSummary {
    
    private QuotaUsage quotaUsage;
    private Double userDataSetsTB;
    
    public QuotaUsage getQuotaUsage() {
        return quotaUsage;
    }
    public void setQuotaUsage(QuotaUsage quotaUsage) {
        this.quotaUsage = quotaUsage;
    }
    public Double getUserDataSetsTB() {
        return userDataSetsTB;
    }
    public void setUserDataSetsTB(Double userDataSetsTB) {
        this.userDataSetsTB = userDataSetsTB;
    }
    
}
