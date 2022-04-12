package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.Sets;
import org.janelia.model.access.domain.TimebasedIdentifierGenerator;
import org.janelia.model.access.domain.dao.PublishedImageDao;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishedImageMongoDaoTest extends AbstractMongoDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(PublishedImageMongoDaoTest.class);

    private static final String testName = "unittester";
    private static final String testUser = "user:"+testName;

    private SubjectMongoDao subjectMongoDao;
    private PublishedImageDao publishedImageDao;

    private List<PublishedImage> testImages = new ArrayList<>();

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
        testImages.addAll(createTestImages());
    }

    @After
    public void tearDown() {
        for (PublishedImage image: testImages) {
            publishedImageDao.delete(image);
        }
    }

    private void setOwnership(DomainObject domainObject) {
        domainObject.setOwnerKey(testUser);
        domainObject.setReaders(Sets.newHashSet(testUser));
        domainObject.setWriters(Sets.newHashSet(testUser));
    }

    private List<PublishedImage> createTestImages() {
        List<PublishedImage> images = new ArrayList<>();

        PublishedImage image1 = new PublishedImage();
        image1.setLine("line 1");
        image1.setSampleRef(Reference.createFor("Sample#1234"));
        image1.setArea("brain");
        image1.setTile("tile 1");
        image1.setReleaseName("release 1");
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
        image2.setReleaseName("release 2");
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
        image3.setReleaseName("release 3");
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
        return images;
    }

    @Test
    public void testGetImage() {
        for (PublishedImage image: testImages) {
            PublishedImage foundImage = publishedImageDao.getImage(image.getSlideCode(),
                image.getAlignmentSpace(), image.getObjective());
            Assert.assertNotNull(foundImage);
            // test a few key attributes
            Assert.assertEquals(image.getId(), foundImage.getId());
            Assert.assertEquals(image.getSampleRef(), foundImage.getSampleRef());
            Assert.assertEquals(image.getTile(), foundImage.getTile());
            Assert.assertEquals(image.getOriginalLine(), foundImage.getOriginalLine());
            for (FileType key: image.getFiles().keySet()) {
                Assert.assertTrue(foundImage.getFiles().containsKey(key));
                Assert.assertEquals(image.getFiles().get(key), foundImage.getFiles().get(key));
            }
        }
    }

    @Test
    public void testGetGen1GAL3LexAImage() {
        for (PublishedImage image: testImages) {
            PublishedImage foundImage = publishedImageDao.getGen1Gal4LexAImage(image.getOriginalLine(), image.getArea());
            Assert.assertNotNull(foundImage);
            // test a few key attributes
            Assert.assertEquals(image.getId(), foundImage.getId());
            Assert.assertEquals(image.getSampleRef(), foundImage.getSampleRef());
            Assert.assertEquals(image.getTile(), foundImage.getTile());
            Assert.assertEquals(image.getOriginalLine(), foundImage.getOriginalLine());
            for (FileType key: image.getFiles().keySet()) {
                Assert.assertTrue(foundImage.getFiles().containsKey(key));
                Assert.assertEquals(image.getFiles().get(key), foundImage.getFiles().get(key));
            }
        }
    }

    @Test
    public void testNoImage() {
        PublishedImage firstImage = testImages.get(0);
        PublishedImage foundImage = publishedImageDao.getImage(firstImage.getSlideCode(),
                firstImage.getAlignmentSpace(), "nonsense objective");
        Assert.assertNull(foundImage);
    }

}
