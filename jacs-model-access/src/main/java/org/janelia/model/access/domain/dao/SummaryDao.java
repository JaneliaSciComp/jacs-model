package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.report.DatabaseSummary;

import java.math.BigDecimal;

/**
 * Summary facade Dao
 */
public interface SummaryDao {
    DatabaseSummary getDataSummaryBySubjectKey(String subjectKey);
    BigDecimal getDiskSpaceUsageByOwnerKey(String subjectKey);
}
