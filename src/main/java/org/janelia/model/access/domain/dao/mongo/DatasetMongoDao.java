package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.DatasetDao;
import org.janelia.model.cdi.DaoObjectMapper;
import org.janelia.model.domain.sample.DataSet;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link DataSet} Mongo DAO.
 */
public class DatasetMongoDao extends AbstractDomainObjectMongoDao<DataSet> implements DatasetDao {
    @Inject
    DatasetMongoDao(MongoDatabase mongoDatabase,
                    DomainPermissionsMongoHelper permissionsHelper,
                    DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, permissionsHelper, updateHelper);
    }

    @Override
    public List<String> getAllDatasetNames() {
        return MongoDaoHelper.getDistinctValues("name", null, mongoCollection, String.class);
    }

    @Override
    public Map<String, String> getDatasetsByGroupName(String groupName) {
        String groupKey = "group:" + groupName;
        List<DataSet> groupDatasets = find(
                MongoDaoHelper.createFilterCriteria(
                        MongoDaoHelper.createFilterByClass(DataSet.class),
                        permissionsHelper.createReadPermissionFilterForSubjectKey(groupKey)
                ),
                null,
                0,
                -1,
                DataSet.class
        );
        return groupDatasets.stream()
                .filter(ds -> ds.getOwnerKey().contains(groupKey) ||
                        ds.getReaders().contains(groupKey) ||
                        ds.getWriters().contains(groupKey))
                .collect(Collectors.toMap(ds -> ds.getName(), ds -> {
                    String owner = ds.getOwnerKey();
                    Set<String> writers = ds.getWriters();
                    if (owner.contains(groupKey)) {
                        return "Owner";
                    } else if (writers.contains(groupKey)) {
                        return "Writer";
                    } else {
                        return "Reader";
                    }
                }));
    }
}
