package org.janelia.rendering;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.ByteStreams;

import org.janelia.testutils.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void loadSlice() {
        class TestData {
            private final List<String> imageNames;
            private TestData(List<String> imageNames) {
                this.imageNames = imageNames;
            }
        }
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "default.0.tif", "default.1.tif", "default.2.tif");
        RenderedImageInfo originalImageInfo = testVolumeLocation.readTileImageInfo("default.0.tif");
        TestData[] testData = new TestData[] {
                new TestData(Arrays.asList("default.0.tif")),
                new TestData(Arrays.asList("default.0.tif", "default.1.tif")),
                new TestData(Arrays.asList("default.0.tif", "default.1.tif", "default.2.tif"))
        };
        for (TestData td : testData) {
            byte[] imageBytes = testVolumeLocation.readTileImagePageAsTexturedBytes("", td.imageNames, 10)
                    .map(sc -> {
                        try {
                            return ByteStreams.toByteArray(sc.getStream());
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .orElse(null);
            ByteBuffer byteBuffer = ByteBuffer.wrap(imageBytes);
            int mipmapLevel = byteBuffer.getInt();
            int width = byteBuffer.getInt();
            int usedWidth = byteBuffer.getInt();
            int height = byteBuffer.getInt();
            int border = byteBuffer.getInt();
            int srgbProxy = byteBuffer.getInt();
            int bitDepth = byteBuffer.getInt();
            int channelCount = byteBuffer.getInt();
            float textureCoordX = byteBuffer.getFloat();
            float expectedTextureCoordX;
            if ((usedWidth % 8) != 0) {
                expectedTextureCoordX = usedWidth / (float)width;
            } else {
                width = usedWidth;
                expectedTextureCoordX = 1.0f;
            }

            assertEquals(originalImageInfo.sx, usedWidth);
            assertEquals(originalImageInfo.sy, height);
            assertEquals(td.imageNames.size(), channelCount);
            assertEquals(originalImageInfo.cmPixelSize, bitDepth);
            int bytesPerPixel = bitDepth / 8;
            assertEquals(originalImageInfo.sRGBspace, srgbProxy > 0 && bytesPerPixel >= channelCount ? true : false);
            assertEquals(expectedTextureCoordX, textureCoordX, 0.0000001);
            assertEquals(byteBuffer.remaining(), height * width * bytesPerPixel * channelCount);
        }
    }
}
