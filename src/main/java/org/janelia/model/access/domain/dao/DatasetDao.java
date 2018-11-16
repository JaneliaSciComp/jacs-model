package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.sample.DataSet;

import java.math.BigDecimal;

/**
 * TmReviewTask data access object
 */
public interface DatasetDao extends DomainObjectDao<DataSet> {
    BigDecimal getDiskSpaceUsageByOwnerKey(String subjectKey);
}
