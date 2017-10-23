package org.janelia.it.jacs.model.domain.report;

import java.util.Map;

public class DatabaseSummary {
    
    private Map<String,Long> userCounts;
    
    public Map<String, Long> getUserCounts() {
        return userCounts;
    }
    public void setUserCounts(Map<String, Long> userCounts) {
        this.userCounts = userCounts;
    }
    
}
