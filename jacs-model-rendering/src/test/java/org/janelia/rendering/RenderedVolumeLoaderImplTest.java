package org.janelia.rendering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import org.janelia.testutils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenderedVolumeLoaderImplTest {
    private static final String TEST_DATADIR = "src/test/resources/testdata/rendering";

    private static Path testSuiteDirectory;

    private RenderedVolumeLoader renderedVolumeLoader;
    private Path testDirectory;
    private RenderedVolumeLocation testVolumeLocation;

    @BeforeAll
    public static void createTestDir() throws IOException {
        testSuiteDirectory = Files.createTempDirectory("testrendering");
    }

    @AfterAll
    public static void deleteTestDir() throws IOException {
        TestUtils.deletePath(testSuiteDirectory);
    }

    @BeforeEach
    public void setUp() throws IOException {
        renderedVolumeLoader = new RenderedVolumeLoaderImpl();
        testDirectory = Files.createTempDirectory(testSuiteDirectory, null);
        testVolumeLocation = new FileBasedRenderedVolumeLocation(testDirectory, Function.identity());
    }

    @Test
    public void loadVolumeWithNoTransform() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "default.0.tif");
        RenderedVolumeMetadata rvm = renderedVolumeLoader.loadVolume(testVolumeLocation).orElse(null);
        assertNull(rvm);
    }

    @Test
    public void loadVolumeWithNoTiles() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt");
        RenderedVolumeMetadata rvm = renderedVolumeLoader.loadVolume(testVolumeLocation).orElse(null);
        assertNull(rvm);
    }

    @Test
    public void loadVolumeWithXYTiles() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "default.0.tif", "default.1.tif");
        RenderedVolumeMetadata rvm = renderedVolumeLoader.loadVolume(testVolumeLocation).orElse(null);
        assertNotNull(rvm);
        assertFalse(rvm.hasXSlices());
        assertFalse(rvm.hasYSlices());
        assertTrue(rvm.hasZSlices());
        assertArrayEquals(new int[]{234764, 50122, 27931}, rvm.getOriginVoxel());
    }

    @Test
    public void loadVolumeWithYZTiles() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "YZ.0.tif", "YZ.1.tif");
        RenderedVolumeMetadata rvm = renderedVolumeLoader.loadVolume(testVolumeLocation).orElse(null);
        assertNotNull(rvm);
        assertTrue(rvm.hasXSlices());
        assertFalse(rvm.hasYSlices());
        assertFalse(rvm.hasZSlices());
    }

    @Test
    public void loadVolumeWithZXTiles() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "ZX.0.tif", "ZX.1.tif");
        RenderedVolumeMetadata rvm = renderedVolumeLoader.loadVolume(testVolumeLocation).orElse(null);
        assertNotNull(rvm);
        assertFalse(rvm.hasXSlices());
        assertTrue(rvm.hasYSlices());
        assertFalse(rvm.hasZSlices());
    }

    @Test
    public void loadVolumeWithAllOrthoTiles() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "default.0.tif", "default.1.tif", "YZ.0.tif", "YZ.1.tif", "ZX.0.tif", "ZX.1.tif");
        RenderedVolumeMetadata rvm = renderedVolumeLoader.loadVolume(testVolumeLocation).orElse(null);
        assertNotNull(rvm);
        assertTrue(rvm.hasXSlices());
        assertTrue(rvm.hasYSlices());
        assertTrue(rvm.hasZSlices());
    }

    @Test
    public void loadXYSlice() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "default.0.tif", "default.1.tif");
        Streamable<byte[]> sliceContent = renderedVolumeLoader.loadVolume(testVolumeLocation)
                .flatMap(rv -> rv.getTileInfo(Coordinate.Z)
                        .map(tileInfo -> TileKey.fromTileCoord(
                                0,
                                0,
                                0,
                                rv.getNumZoomLevels() - 1,
                                Coordinate.Z,
                                0))
                        .map(tileIndex -> renderedVolumeLoader.loadSlice(testVolumeLocation, rv, tileIndex)))
                .orElse(Streamable.empty());
        assertNotNull(sliceContent.getContent());
        assertEquals(sliceContent.getSize(), (long) sliceContent.getContent().length);
    }

    @Test
    public void loadSingleChannelXYSlice() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "default.0.tif");
        Streamable<byte[]> sliceContent = renderedVolumeLoader.loadVolume(testVolumeLocation)
                .flatMap(rv -> rv.getTileInfo(Coordinate.Z)
                        .map(tileInfo -> TileKey.fromTileCoord(
                                0,
                                0,
                                0,
                                rv.getNumZoomLevels() - 1,
                                Coordinate.Z,
                                1))
                        .map(tileIndex -> renderedVolumeLoader.loadSlice(testVolumeLocation, rv, tileIndex)))
                .orElse(Streamable.empty());
        assertNotNull(sliceContent.getContent());
        assertEquals(sliceContent.getSize(), (long) sliceContent.getContent().length);
    }

    @Test
    public void loadMissingXYSlice() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "default.0.tif", "default.1.tif");
        Streamable<byte[]> sliceContent = renderedVolumeLoader.loadVolume(testVolumeLocation)
                .flatMap(rvm -> rvm.getTileInfo(Coordinate.Z)
                        .map(tileInfo -> TileKey.fromTileCoord(
                                1,
                                1,
                                1,
                                0,
                                Coordinate.Z,
                                0))
                        .map(tileIndex -> renderedVolumeLoader.loadSlice(testVolumeLocation, rvm, tileIndex)))
                .orElse(Streamable.empty());
        assertNull(sliceContent.getContent());
    }

    @Test
    public void retrieveClosestRawImage() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "transform.txt", "default.0.tif", "default.1.tif", "tilebase.cache.yml");
        RawImage rawImage = renderedVolumeLoader.findClosestRawImageFromVoxelCoord(testVolumeLocation, 0, 0, 0)
                .orElse(null);
        assertNotNull(rawImage);
    }

    @Test
    public void noRawImageFound() {
        RawImage rawImage = renderedVolumeLoader.findClosestRawImageFromVoxelCoord(testVolumeLocation, 0, 0, 0 )
                .orElse(null);
        assertNull(rawImage);
    }

    @Test
    public void loadRawImageContent() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, ImmutableMap.of(
                "default.0.tif", "default/default-ngc.0.tif",
                "default.1.tif", "default/default-ngc.1.tif"));
        RawImage rawImage = new RawImage();
        rawImage.setAcquisitionPath(testDirectory.toString());
        rawImage.setRelativePath("default");
        class TestData {
            private final int xVoxel;
            private final int yVoxel;
            private final int zVoxel;
            private final int dimx;
            private final int dimy;
            private final int dimz;
            private final int channel;
            private final Consumer<byte[]> resultAssertion;

            private TestData(int xVoxel, int yVoxel, int zVoxel, int dimx, int dimy, int dimz, int channel, Consumer<byte[]> resultAssertion) {
                this.xVoxel = xVoxel;
                this.yVoxel = yVoxel;
                this.zVoxel = zVoxel;
                this.dimx = dimx;
                this.dimy = dimy;
                this.dimz = dimz;
                this.channel = channel;
                this.resultAssertion = resultAssertion;
            }
        }
        TestData[] testData = new TestData[]{
                new TestData(0, 0, 0, -1, -1, -1, 0, imageBytes -> assertNotNull(imageBytes, "Test 0")),
                new TestData(0, 0, 0, -1, -1, -1, 1, imageBytes -> assertNotNull(imageBytes, "Test 1")),
                new TestData(0, 0, 0, -1, -1, -1, 2, imageBytes -> assertNull(imageBytes, "Test 2")),
                new TestData(10, 11, 9, 10, 10, 3, 0, imageBytes -> assertNotNull(imageBytes, "Test 3")),
                new TestData(10, 11, 9, 100, 100, 100, 0, imageBytes -> assertNotNull(imageBytes, "Test 4")),
                new TestData(10, 11, 9, -1, -1, -1, 0, imageBytes -> assertNotNull(imageBytes, "Test 5")),
        };
        for (TestData td : testData) {
            Streamable<byte[]> rawImageContent = renderedVolumeLoader.loadRawImageContentFromVoxelCoord(testVolumeLocation, rawImage, td.channel, td.xVoxel, td.yVoxel, td.zVoxel, td.dimx, td.dimy, td.dimz);
            td.resultAssertion.accept(rawImageContent.getContent());
        }
    }

}
