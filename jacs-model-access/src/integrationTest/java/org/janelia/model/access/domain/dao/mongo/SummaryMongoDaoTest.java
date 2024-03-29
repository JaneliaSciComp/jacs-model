package org.janelia.model.access.domain.dao.mongo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SummaryMongoDaoTest extends AbstractMongoDaoTest {

    private SubjectMongoDao subjectMongoDao;
    private DatasetMongoDao datasetMongoDao;
    private SummaryMongoDao summaryMongoDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        datasetMongoDao = new DatasetMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
        summaryMongoDao = new SummaryMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
    }

    @Test
    public void diskUsage() {
        List<DataSet> testData = ImmutableList.of(
                persistData(createTestDataset("ds1", "user:u1", 700000000000000010L)),
                persistData(createTestDataset("ds2", "user:u1", 800000000000000011L)),
                persistData(createTestDataset("ds3", "user:u1", 900000000000000013L)),
                persistData(createTestDataset("ds1", "user:u2", 20L)),
                persistData(createTestDataset("ds2", "user:u2", 21L)),
                persistData(createTestDataset("ds3", "user:u2", 23L)),
                persistData(createTestDataset("ds3", "user:u3", 0L))
        );
        Function<String, BigDecimal> expectedSizeCalc = (String u) -> testData.stream()
                    .filter(ds -> ds.getOwnerKey().equals(u))
                    .map(ds -> new BigDecimal(ds.getDiskSpaceUsage()))
                    .reduce((ds1, ds2) -> ds1.add(ds2))
                    .orElse(new BigDecimal(0));

        Map<String, BigDecimal> expectedResults = ImmutableMap.of(
                "u1", expectedSizeCalc.apply("u1"),
                "u2", expectedSizeCalc.apply("u2"),
                "u3", expectedSizeCalc.apply("u3"),
                "u4", expectedSizeCalc.apply("u4")
        );
        for (String testUser : expectedResults.keySet()) {
            assertEquals(expectedResults.get(testUser), summaryMongoDao.getDiskSpaceUsageByOwnerKey(testUser));
        }
    }

    private DataSet persistData(DataSet ds) {
        datasetMongoDao.saveBySubjectKey(ds, ds.getOwnerKey());
        return ds;
    }

    private DataSet createTestDataset(String datasetName, String ownerKey, Long diskSpace) {
        DataSet dataSet = new DataSet();
        dataSet.setName(datasetName);
        dataSet.setOwnerKey(ownerKey);
        dataSet.setDiskSpaceUsage(diskSpace);
        return dataSet;
    }
}
