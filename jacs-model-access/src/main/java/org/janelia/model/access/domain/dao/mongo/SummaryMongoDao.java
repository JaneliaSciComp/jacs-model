package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.SummaryDao;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.report.DatabaseSummary;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.LSMImage;
import org.janelia.model.domain.sample.Sample;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * Summary Mongo DAO.
 */
public class SummaryMongoDao extends AbstractMongoDao implements SummaryDao {

    @Inject
    public SummaryMongoDao(MongoDatabase mongoDatabase) {
        super(mongoDatabase);
    }

    @Override
    public DatabaseSummary getDataSummaryBySubjectKey(String subjectKey) {
        DatabaseSummary summary = new DatabaseSummary();

        long nDatasets = MongoDaoHelper.count(
                Filters.eq("ownerKey", subjectKey),
                getEntityCollection(DataSet.class));
        long nSyncedSamples = MongoDaoHelper.count(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("ownerKey", subjectKey),
                        Filters.eq("sageSynced", true)
                ),
                getEntityCollection(Sample.class));
        long nSyncedImages = MongoDaoHelper.count(
                MongoDaoHelper.createFilterCriteria(
                        Filters.eq("class", LSMImage.class.getName()),
                        Filters.eq("ownerKey", subjectKey),
                        Filters.eq("sageSynced", true)
                ),
                getEntityCollection(LSMImage.class));
        long nAnnotations = MongoDaoHelper.count(
                Filters.eq("ownerKey", subjectKey),
                getEntityCollection(Annotation.class));

        summary.setUserCounts(ImmutableMap.<String, Long>builder()
                .put("DataSet", nDatasets)
                .put("Sample", nSyncedSamples)
                .put("LSMImage", nSyncedImages)
                .put("Annotation", nAnnotations)
                .build()
        );

        return summary;
    }

    @Override
    public BigDecimal getDiskSpaceUsageByOwnerKey(String subjectKey) {
        List<Bson> diskUsagePipeline = ImmutableList.<Bson>builder()
                .add(Aggregates.match(Filters.eq("ownerKey", subjectKey)))
                .add(Aggregates.group(null, Accumulators.sum("diskSpaceUsage", "$diskSpaceUsage")))
                .build();

        List<Document> diskUsage = MongoDaoHelper.findPipeline(
                diskUsagePipeline,
                null,
                0,
                -1,
                getEntityCollection(DataSet.class),
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
