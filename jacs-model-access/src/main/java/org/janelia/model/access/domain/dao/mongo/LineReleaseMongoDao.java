package org.janelia.model.access.domain.dao.mongo;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.dao.LineReleaseDao;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.LineRelease;
import org.janelia.model.util.TimebasedIdentifierGenerator;

/**
 * {@link DataSet} Mongo DAO.
 */
public class LineReleaseMongoDao extends AbstractNodeMongoDao<LineRelease> implements LineReleaseDao {
    @Inject
    LineReleaseMongoDao(MongoDatabase mongoDatabase,
                        TimebasedIdentifierGenerator idGenerator,
                        DomainPermissionsMongoHelper permissionsHelper,
                        DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<LineRelease> findReleasesByPublishingSites(List<String> publishingSites) {
        if (CollectionUtils.isEmpty(publishingSites)) {
            return Collections.emptyList();
        }
        return find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.in("targetWebsite", publishingSites)),
                null,
                0,
                -1,
                getEntityType());
    }

    @Override
    public List<LineRelease> findReleasesByName(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Collections.emptyList();
        }
        return find(
                MongoDaoHelper.createFilterCriteria(
                        Filters.in("name", names)),
                null,
                0,
                -1,
                getEntityType());
    }
}
