package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.sample.DataSet;

import java.util.Map;

/**
 * Dataset data access object
 */
public interface DatasetDao extends DomainObjectDao<DataSet> {
    Map<String, String> getDatasetsByGroupName(String groupName);
}
