package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.sample.DataSet;

import java.util.List;
import java.util.Map;

/**
 * Dataset data access object
 */
public interface DatasetDao extends DomainObjectDao<DataSet> {
    List<String> getAllDatasetNames();
    Map<String, String> getDatasetsByGroupName(String groupName);
}
