package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.DatasetDao;
import org.janelia.model.domain.sample.DataSet;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * {@link DataSet} Mongo DAO.
 */
public class DatasetMongoDao extends AbstractPermissionAwareDomainMongoDao<DataSet> implements DatasetDao {

    @Inject
    DatasetMongoDao(MongoDatabase mongoDatabase, ObjectMapper objectMapper) {
        super(mongoDatabase, objectMapper);
    }

    @Override
    public BigDecimal getDiskSpaceUsageByOwnerKey(String subjectKey) {
        List<Bson> diskUsagePipeline = ImmutableList.<Bson>builder()
                .add(Aggregates.match(Filters.eq("ownerKey", subjectKey)))
                .add(Aggregates.group(null, Accumulators.sum("diskSpaceUsage", "$diskSpaceUsage")))
                .add(Aggregates.project(Projections.excludeId()))
                .build();

        List<Document> diskUsage = MongoDaoHelper.findPipeline(
                diskUsagePipeline,
                null,
                0,
                -1,
                mongoCollection,
                Document.class);
        if (CollectionUtils.isEmpty(diskUsage)) {
            return new BigDecimal(0);
        } else {
            return diskUsage.stream()
                    .map(d -> d.get("diskSpaceUsage", Number.class))
                    .map(n -> new BigDecimal(n.toString()))
                    .reduce(new BigDecimal(0), (ds1, ds2) -> ds1.add(ds2));
        }
    }
}
