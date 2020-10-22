package org.janelia.model.access.domain.dao.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.bson.Document;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.ColorDepthImageDao;
import org.janelia.model.access.domain.dao.SetFieldValueHandler;
import org.janelia.model.access.domain.nodetools.NodeUtils;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.workspace.Node;
import org.janelia.model.domain.workspace.TreeNode;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ColorDepthImageDaoTest extends AbstractMongoDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(ColorDepthImageDaoTest.class);
    private static final String TEST_OWNER = "user:test";

    private ColorDepthImageDao colorDepthImageDao;

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        SubjectMongoDao subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        this.colorDepthImageDao = new ColorDepthImageMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper));
    }

    @Test
    public void countColorDepthMips() {
        List<ColorDepthImage> cdmips = Stream.of(
                createColorDepthImage("i1", ImmutableSet.of("l1", "l2"), "a1"),
                createColorDepthImage("i2", ImmutableSet.of("l1", "l3"), "a1"),
                createColorDepthImage("i3", ImmutableSet.of("l1"), "a2"),
                createColorDepthImage("i4", ImmutableSet.of("l2"), "a2"),
                createColorDepthImage("i5", ImmutableSet.of("l3"), "a3"))
                .peek(cdmip -> colorDepthImageDao.save(cdmip))
                .collect(Collectors.toList());
        class CountData {
            String library;
            String alignmentSpace;
            Integer count;
        }
        Map<String, Map<String, Integer>> expectedResults = cdmips.stream()
                        .flatMap(mip -> mip.getLibraries().stream()
                                .map(l -> {
                                    CountData c = new CountData();
                                    c.library = l;
                                    c.alignmentSpace = mip.getAlignmentSpace();
                                    c.count = 1;
                                    return c;
                                }))
                        .collect(Collectors.groupingBy(
                                l -> l.library,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        lcList -> lcList.stream()
                                                .collect(Collectors.groupingBy(
                                                        lc -> lc.alignmentSpace,
                                                        Collectors.collectingAndThen(Collectors.toList(), List::size)))
                                )));
        List<ColorDepthLibrary> librariesWithMIPCounts = colorDepthImageDao.countColorDepthMIPsByAlignmentSpaceForAllLibraries();
        for (ColorDepthLibrary l : librariesWithMIPCounts) {
            assertNotNull(expectedResults.get(l.getIdentifier()));
            assertEquals(expectedResults.get(l.getIdentifier()), l.getColorDepthCounts());
            assertEquals(expectedResults.get(l.getIdentifier()), colorDepthImageDao.countColorDepthMIPsByAlignmentSpaceForLibrary(l.getIdentifier()));
        }

    }

    private ColorDepthImage createColorDepthImage(String name, Set<String> libraries, String alignmentSpace) {
        ColorDepthImage cdmip = new ColorDepthImage();
        cdmip.setName(name);
        cdmip.setLibraries(libraries);
        cdmip.setAlignmentSpace(alignmentSpace);
        return cdmip;
    }

}
