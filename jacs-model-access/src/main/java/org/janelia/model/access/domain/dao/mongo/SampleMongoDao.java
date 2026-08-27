package org.janelia.model.access.domain.dao.mongo;

import java.util.List;

import javax.inject.Inject;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.janelia.model.access.domain.SampleQuery;
import org.janelia.model.access.domain.dao.SampleDao;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;

/**
 * {@link DataSet} Mongo DAO.
 */
public class SampleMongoDao extends AbstractDomainObjectMongoDao<Sample> implements SampleDao {
    @Inject
    SampleMongoDao(MongoDatabase mongoDatabase,
                   TimebasedIdentifierGenerator idGenerator,
                   DomainPermissionsMongoHelper permissionsHelper,
                   DomainUpdateMongoHelper updateHelper) {
        super(mongoDatabase, idGenerator, permissionsHelper, updateHelper);
    }

    @Override
    public List<Sample> findMatchingSamples(SampleQuery sampleQuery) {
        return find(
                MongoDaoHelper.createFilterCriteria(
                        sampleQuery.hasSampleIds() ? MongoDaoHelper.createFilterByIds(sampleQuery.getSampleIds()) : null,
                        sampleQuery.hasDatasetIds() ? Filters.in("dataSet", sampleQuery.getDatasetIds()) : null,
                        sampleQuery.hasSampleNames() ? Filters.in("name", sampleQuery.getSampleNames()) : null,
                        sampleQuery.hasSampleLines() ? Filters.in("line", sampleQuery.getSampleLines()) : null,
                        sampleQuery.hasSampleSlideCodes() ? Filters.in("slideCode", sampleQuery.getSampleSlideCodes()) : null,
                        sampleQuery.hasSampleFlycoreIds() ? Filters.in("flycoreId", sampleQuery.getSampleFlycoreIds()) : null,
                        sampleQuery.hasSampleCrossBarcodes() ? Filters.in("crossBarcode", sampleQuery.getSampleCrossBarcodes()) : null),
                null,
                sampleQuery.getOffset(),
                sampleQuery.getLength(),
                getEntityType());
    }
}
