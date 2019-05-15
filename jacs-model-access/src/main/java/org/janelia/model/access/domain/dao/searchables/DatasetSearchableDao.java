package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.DatasetDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.sample.DataSet;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * {@link DataSet} DAO.
 */
@AsyncIndex
public class DatasetSearchableDao extends AbstractDomainSearchablDao<DataSet> implements DatasetDao {

    private final DatasetDao datasetDao;

    @Inject
    DatasetSearchableDao(DatasetDao datasetDao,
                         @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(datasetDao, objectIndexer);
        this.datasetDao = datasetDao;
    }

    @Override
    public List<String> getAllDatasetNames() {
        return datasetDao.getAllDatasetNames();
    }

    @Override
    public Map<String, String> getDatasetsByGroupName(String groupName) {
        return datasetDao.getDatasetsByGroupName(groupName);
    }

    @Override
    public List<DataSet> getDatasetsByOwnersAndSageSyncFlag(List<String> ownerKeys, Boolean sageSyncFlag) {
        return datasetDao.getDatasetsByOwnersAndSageSyncFlag(ownerKeys, sageSyncFlag);
    }

}
