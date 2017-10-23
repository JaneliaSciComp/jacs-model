package org.janelia.it.jacs.model.domain.gui.search.criteria;

import java.util.Date;

public class DateRangeCriteria extends AttributeCriteria {

    private Date startDate;
    private Date endDate;
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
