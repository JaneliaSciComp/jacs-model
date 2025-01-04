package org.janelia.model.access.domain.dao.mongo;

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.IdGenerator;
import org.janelia.model.access.domain.dao.SampleDao;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.Sample;

/**
 * {@link DataSet} Mongo DAO.
 */
@Dependent
public class SampleMongoDao extends AbstractDomainObjectMongoDao<Sample> implements SampleDao {
    @Inject
    SampleMongoDao(MongoDatabase mongoDatabase,
                   IdGenerator<Long> idGenerator,
                   DomainPermissionsMongoHelper permissionsHelper,
                   DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<Sample> findMatchingSample(Collection<Long> ids,
                                           Collection<String> dataSetIds,
                                           Collection<String> sampleNames,
                                           Collection<String> slideCodes,
                                           long offset,
                                           int length) {
        return find(
                MongoDaoHelper.createFilterCriteria(
                        CollectionUtils.isEmpty(ids) ? null : MongoDaoHelper.createFilterByIds(ids),
                        CollectionUtils.isEmpty(dataSetIds) ? null : Filters.in("dataSet", dataSetIds),
                        CollectionUtils.isEmpty(sampleNames) ? null : Filters.in("name", sampleNames),
                        CollectionUtils.isEmpty(slideCodes) ? null : Filters.in("slideCode", slideCodes)),
                null,
                offset,
                length,
                getEntityType());
    }
}
