package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.EmBodyDao;
import org.janelia.model.access.domain.dao.EmDataSetDao;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.flyem.EMBody;
import org.janelia.model.domain.flyem.EMDataSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class EMMongoDaoTest extends AbstractMongoDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(EMMongoDaoTest.class);

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;

    private SubjectMongoDao subjectMongoDao;
    private EmDataSetDao emDataSetDao;
    private EmBodyDao emBodyDao;

    private List<EMDataSet> testDataSets = new ArrayList<>();
    private List<EMBody> testBodies = new ArrayList<>();

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        emDataSetDao = new EmDataSetMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper)) {
        };
        emBodyDao = new EmBodyMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));

        testDataSets.addAll(createTestDataSets());
        for (EMDataSet testDataSet : testDataSets) {
            testBodies.addAll(createTestBodies(testDataSet));
        }
    }

    @After
    public void tearDown() {
        for (EMBody test : testBodies) {
            emBodyDao.delete(test);
        }
        for (EMDataSet test : testDataSets) {
            emDataSetDao.delete(test);
        }
    }

    private void setOwnership(DomainObject domainObject) {
        domainObject.setOwnerKey(testUser);
        domainObject.setReaders(Sets.newHashSet(testUser));
        domainObject.setWriters(Sets.newHashSet(testUser));
    }

    private List<EMDataSet> createTestDataSets() {
        List<EMDataSet> dataSets = new ArrayList<>();

        EMDataSet dataSet = new EMDataSet();
        dataSet.setName("hemibrain");
        dataSet.setVersion("1.0.1");
        dataSet.setPublished(true);
        setOwnership(dataSet);
        dataSets.add(dataSet);

        EMDataSet dataSet2 = new EMDataSet();
        dataSet2.setName("hemibrain");
        dataSet2.setVersion("1.0.2");
        dataSet2.setPublished(false);
        setOwnership(dataSet2);
        dataSets.add(dataSet2);

        emDataSetDao.saveAll(dataSets);
        LOG.trace("Created {} data sets", dataSets.size());
        return dataSets;
    }

    private List<EMBody> createTestBodies(EMDataSet dataSet) {
        List<EMBody> bodies = new ArrayList<>();

        EMBody body = new EMBody();
        body.setBodyId(1234567L);
        body.setDataSetRef(Reference.createFor(dataSet));
        body.setDataSetIdentifier(dataSet.getDataSetIdentifier());
        body.setStatus("Traced");
        body.setNeuronType("");
        body.setNeuronInstance("");
        DomainUtils.setFilepath(body, FileType.ColorDepthMip1, "/path/to/cdm.png");
        DomainUtils.setFilepath(body, FileType.SkeletonSWC, "/path/to/skeleton.swc");
        setOwnership(body);
        bodies.add(body);

        EMBody body2 = new EMBody();
        body2.setBodyId(1234568L);
        body2.setDataSetRef(Reference.createFor(dataSet));
        body2.setDataSetIdentifier(dataSet.getDataSetIdentifier());
        body2.setStatus("Rough");
        body2.setNeuronType("EMR-343");
        body2.setNeuronInstance("");
        DomainUtils.setFilepath(body2, FileType.ColorDepthMip1, "/path/to/cdm.png");
        DomainUtils.setFilepath(body2, FileType.SkeletonSWC, "/path/to/skeleton.swc");
        setOwnership(body2);
        bodies.add(body2);

        emBodyDao.saveAll(bodies);
        LOG.trace("Created {} bodies for {}", bodies.size(), dataSet);
        return bodies;
    }

    @Test
    public void testGetAllDataSets() {
        Set<Long> testIds = testDataSets.stream().map(AbstractDomainObject::getId).collect(Collectors.toSet());
        Set<Long> ids = emDataSetDao.findAll().stream().map(AbstractDomainObject::getId).collect(Collectors.toSet());
        Assert.assertEquals(testIds, ids);
    }

    @Test
    public void testFindDataSets() {
        for (EMDataSet testDataSet : testDataSets) {

            EMDataSet emDataSet2 = emDataSetDao.findById(testDataSet.getId());
            Assert.assertNotNull(emDataSet2);
            Assert.assertEquals(testDataSet.getName(), emDataSet2.getName());
            Assert.assertEquals(testDataSet.getVersion(), emDataSet2.getVersion());

            EMDataSet emDataSet = emDataSetDao.getDataSetByNameAndVersion(testDataSet.getName(), testDataSet.getVersion());
            Assert.assertNotNull(emDataSet);
            Assert.assertEquals(testDataSet.getName(), emDataSet.getName());
            Assert.assertEquals(testDataSet.getVersion(), emDataSet.getVersion());

        }
    }

    @Test
    public void testGetDataSetVersions() {

        String name = testDataSets.get(0).getName();
        List<EMDataSet> dataSetVersions = emDataSetDao.getDataSetVersions(name);
        Assert.assertTrue(dataSetVersions.size()>1);
        for (EMDataSet dataSetVersion : dataSetVersions) {
            Assert.assertEquals(name, dataSetVersion.getName());
        }

    }

    @Test
    public void testGetBodies() {

        for (EMDataSet testDataSet : testDataSets) {

            Set<Long> testIds = testBodies.stream()
                    .filter(f -> f.getDataSetRef().getTargetId().equals(testDataSet.getId()))
                    .map(AbstractDomainObject::getId).collect(Collectors.toSet());

            List<EMBody> bodies = emBodyDao.getBodiesForDataSet(testDataSet, 0, -1);
            Assert.assertFalse(bodies.isEmpty());

            for (EMBody body : bodies) {
                Assert.assertEquals(testDataSet.getId(), body.getDataSetRef().getTargetId());
                Assert.assertEquals(testDataSet.getDataSetIdentifier(), body.getDataSetIdentifier());
            }

            Set<Long> ids = bodies.stream().map(AbstractDomainObject::getId).collect(Collectors.toSet());
            Assert.assertEquals(testIds, ids);
        }

    }

    @Test
    public void getBodiesByName() {
        for (EMDataSet testDataSet : testDataSets) {

            Set<String> testNames = testBodies.stream()
                    .filter(f -> f.getDataSetRef().getTargetId().equals(testDataSet.getId()))
                    .map(AbstractDomainObject::getName)
                    .collect(Collectors.toSet());

            List<EMBody> bodies = emBodyDao.getBodiesWithNameForDataSet(testDataSet, testNames, 0, -1);
            Assert.assertFalse(bodies.isEmpty());

            for (EMBody body : bodies) {
                Assert.assertEquals(testDataSet.getId(), body.getDataSetRef().getTargetId());
                Assert.assertTrue(testNames.contains(body.getName()));
            }
        }
    }

    @Test
    public void testStreamBodies() {

        for (EMDataSet testDataSet : testDataSets) {

            Set<Long> testIds = testBodies.stream()
                    .filter(f -> f.getDataSetRef().getTargetId().equals(testDataSet.getId()))
                    .map(AbstractDomainObject::getId).collect(Collectors.toSet());

            emBodyDao.streamBodiesForDataSet(testDataSet).forEach(body -> {
                Assert.assertNotEquals("NEW", body.getStatus());
            });

            Set<Long> ids = emBodyDao.streamBodiesForDataSet(testDataSet)
                    .map(body -> {

                        emBodyDao.update(body.getId(), ImmutableMap.of(
                                "status", new SetFieldValueHandler<>("NEW")
                        ));

                        return body.getId();
                    })
                    .collect(Collectors.toSet());
            Assert.assertEquals(testIds, ids);

            emBodyDao.streamBodiesForDataSet(testDataSet).forEach(body -> {
                Assert.assertEquals("NEW", body.getStatus());
            });

        }

    }

    @Test
    public void testReplace() {

        for (EMDataSet testDataSet : testDataSets) {
            emBodyDao.streamBodiesForDataSet(testDataSet).forEach(body -> {

                Assert.assertNotEquals("NEW", body.getStatus());

                body.setStatus("NEW");
                emBodyDao.replace(body);

                EMBody newBody = emBodyDao.findById(body.getId());
                Assert.assertEquals("NEW", newBody.getStatus());

            });
        }
    }
}
