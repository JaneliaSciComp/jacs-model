package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.PublishedImageDao;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.sample.PublishedImage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PublishedImageMongoDaoTest extends AbstractMongoDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(PublishedImageMongoDaoTest.class);

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;

    private SubjectMongoDao subjectMongoDao;
    private PublishedImageDao publishedImageDao;

    private Map<Long, PublishedImage> testImages = new HashMap<>();

    @Before
    public void setUp() {
        TimebasedIdentifierGenerator timebasedIdentifierGenerator = new TimebasedIdentifierGenerator(0);
        subjectMongoDao = new SubjectMongoDao(testMongoDatabase, timebasedIdentifierGenerator);
        publishedImageDao = new PublishedImageMongoDao(
                testMongoDatabase,
                timebasedIdentifierGenerator,
                new DomainPermissionsMongoHelper(subjectMongoDao),
                new DomainUpdateMongoHelper(testObjectMapper)) {

        };
        testImages.putAll(createTestImages());
    }

    @After
    public void tearDown() {
        testImages.forEach((id, img) -> publishedImageDao.delete(img));
    }

    private void setOwnership(DomainObject domainObject) {
        domainObject.setOwnerKey(testUser);
        domainObject.setReaders(Sets.newHashSet(testUser));
        domainObject.setWriters(Sets.newHashSet(testUser));
    }

    private Map<Long, PublishedImage> createTestImages() {
        List<PublishedImage> images = new ArrayList<>();

        PublishedImage image1 = new PublishedImage();
        image1.setLine("line 1");
        image1.setSampleRef(Reference.createFor("Sample#1234"));
        image1.setArea("brain");
        image1.setTile("tile 1");
        image1.setReleaseName("Gen1 GAL4");
        image1.setSlideCode("line-date_1_A1");
        image1.setOriginalLine("original-line-1");
        image1.setObjective("40x");
        image1.setAlignmentSpace("JRC2018_Unisex_20x_HR");
        Map<FileType, String> imageMap1 = new HashMap<>();
        imageMap1.put(FileType.VisuallyLosslessStack, "http://s3/images/etc");
        image1.setFiles(imageMap1);
        images.add(image1);

        PublishedImage image2 = new PublishedImage();
        image2.setLine("line 2");
        image2.setSampleRef(Reference.createFor("Sample#5678"));
        image2.setArea("brain");
        image2.setTile("tile 2");
        image2.setReleaseName("Gen1 GAL4");
        image2.setSlideCode("line-date_2_B3");
        image2.setOriginalLine("original-line-2");
        image2.setObjective("40x");
        image2.setAlignmentSpace("JRC2018_Unisex_20x_HR");
        Map<FileType, String> imageMap2 = new HashMap<>();
        imageMap2.put(FileType.VisuallyLosslessStack, "http://s3/images/etc2");
        image2.setFiles(imageMap1);
        images.add(image2);

        PublishedImage image3 = new PublishedImage();
        image3.setLine("line 3");
        image3.setSampleRef(Reference.createFor("Sample#1357"));
        image3.setArea("brain");
        image3.setTile("tile 3");
        image3.setReleaseName("Gen1 LexA");
        image3.setSlideCode("line-date_3_C5");
        image3.setOriginalLine("original-line-3");
        image3.setObjective("40x");
        image3.setAlignmentSpace("JRC2018_Unisex_20x_HR");
        Map<FileType, String> imageMap3 = new HashMap<>();
        imageMap3.put(FileType.VisuallyLosslessStack, "http://s3/images/etc3");
        imageMap3.put(FileType.ColorDepthMip1, "http://s3/images/etc3");
        image3.setFiles(imageMap1);
        images.add(image3);

        publishedImageDao.saveAll(images);
        LOG.trace("Created test images");
        return images.stream().collect(Collectors.toMap(AbstractDomainObject::getId, i -> i));
    }

    @Test
    public void testGetImage() {
        Map<Pair<String, String>, List<PublishedImage>> testImagesByAlignmentSpaceAndObjective =
                testImages.values().stream().collect(Collectors.groupingBy(
                        i -> ImmutablePair.of(i.getAlignmentSpace(), i.getObjective()),
                        Collectors.toList()
                ));

        testImagesByAlignmentSpaceAndObjective.forEach((asAndObjective, testImagesSubset) -> {
            Set<String> testSlideCodes = testImagesSubset.stream().map(PublishedImage::getSlideCode).collect(Collectors.toSet());
            List<PublishedImage> foundImages = publishedImageDao.getImages(asAndObjective.getLeft(), testSlideCodes, asAndObjective.getRight());
            assertEquals(testImagesSubset.size(), foundImages.size());
            compareImages(testImagesSubset, foundImages);
        });
    }

    private void compareImages(Collection<PublishedImage> referenceImages, Collection<PublishedImage> toCheck) {
        Map<Long, PublishedImage> indexedReferenceImages = referenceImages.stream().collect(Collectors.toMap(AbstractDomainObject::getId, i -> i));
        toCheck.forEach(foundImage -> {
            PublishedImage image = indexedReferenceImages.get(foundImage.getId());
            assertNotNull(image);
            // test a few key attributes
            assertEquals(image.getId(), foundImage.getId());
            assertEquals(image.getSampleRef(), foundImage.getSampleRef());
            assertEquals(image.getTile(), foundImage.getTile());
            assertEquals(image.getOriginalLine(), foundImage.getOriginalLine());
            for (FileType key: image.getFiles().keySet()) {
                assertTrue(foundImage.getFiles().containsKey(key));
                assertEquals(image.getFiles().get(key), foundImage.getFiles().get(key));
            }
        });
    }

    @Test
    public void testGetGen1GAL3LexAImage() {
        Map<String, List<PublishedImage>> testImagesByAnatomicalArea =
                testImages.values().stream().collect(Collectors.groupingBy(
                        PublishedImage::getArea,
                        Collectors.toList()
                ));
        testImagesByAnatomicalArea.forEach((area, testImagesSubset) -> {
            Set<String> testLines = testImagesSubset.stream().map(PublishedImage::getOriginalLine).collect(Collectors.toSet());
            List<PublishedImage> foundImages = publishedImageDao.getGen1Gal4LexAImages(area, testLines);
            assertEquals(testImagesSubset.size(), foundImages.size());
            compareImages(testImagesSubset, foundImages);
        });
    }

    @Test
    public void testNoImageFoundWithBadObjective() {
        PublishedImage firstImage = testImages.values().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("There should always be some data"));
        List<PublishedImage> foundImages = publishedImageDao.getImages(
                firstImage.getAlignmentSpace(),
                Collections.singleton(firstImage.getSlideCode()),
                "bad objective");
        assertTrue(foundImages.isEmpty());
    }

}
