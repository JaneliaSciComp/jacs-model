package org.janelia.rendering;

import org.janelia.rendering.utils.ImageInfo;
import org.janelia.rendering.utils.ImageUtils;
import org.janelia.testutils.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class FileBasedRenderedVolumeLocationTest {
    private static final String TEST_DATADIR = "src/test/resources/testdata/rendering";

    private static Path testSuiteDirectory;

    private Path testDirectory;
    private RenderedVolumeLocation testVolumeLocation;

    @BeforeClass
    public static void createTestDir() throws IOException {
        testSuiteDirectory = Files.createTempDirectory("testrendering");
    }

    @AfterClass
    public static void deleteTestDir() throws IOException {
        TestUtils.deletePath(testSuiteDirectory);
    }

    @Before
    public void setUp() throws IOException {
        testDirectory = Files.createTempDirectory(testSuiteDirectory, null);
        testVolumeLocation = new FileBasedRenderedVolumeLocation(testDirectory);
    }

    @Test
    public void loadSliceRange() {
        String imageName = "default.0.tif";
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, imageName);
        ImageInfo originalImageInfo = ImageUtils.loadImageInfoFromTiffStream(testVolumeLocation.readTileImage(imageName));
        byte[] imagePages = testVolumeLocation.readTileImagePages(imageName, 10, 15);
        ImageInfo copyImageInfo = ImageUtils.loadImageInfoFromTiffStream(new ByteArrayInputStream(imagePages));
        assertEquals(originalImageInfo.sx, copyImageInfo.sx);
        assertEquals(originalImageInfo.sy, copyImageInfo.sy);
        assertEquals(Math.min(15, originalImageInfo.sz - 10), copyImageInfo.sz);
    }
}
