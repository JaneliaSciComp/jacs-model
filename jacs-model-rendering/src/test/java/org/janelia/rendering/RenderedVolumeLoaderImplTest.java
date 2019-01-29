package org.janelia.rendering;

import com.google.common.collect.ImmutableMap;
import org.janelia.testutils.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RenderedVolumeLoaderImplTest {
    private static final String TEST_DATADIR = "src/test/resources/testdata/rendering";

    private static Path testSuiteDirectory;

    private RenderedVolumeLoader renderedVolumeLoader;
    private Path testDirectory;

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
        renderedVolumeLoader = new RenderedVolumeLoaderImpl();
        testDirectory = Files.createTempDirectory(testSuiteDirectory, null);
    }

    @Test
    public void loadVolumeWithNoTransform() {
        prepareTestDataFiles("default.0.tif");
        RenderedVolume rv = renderedVolumeLoader.loadVolume(testDirectory).orElse(null);
        assertNull(rv);
    }

    @Test
    public void loadVolumeWithNoTiles() {
        prepareTestDataFiles("transform.txt");
        RenderedVolume rv = renderedVolumeLoader.loadVolume(testDirectory).orElse(null);
        assertNull(rv);
    }

    @Test
    public void loadVolumeWithXYTiles() {
        prepareTestDataFiles("transform.txt", "default.0.tif", "default.1.tif");
        RenderedVolume rv = renderedVolumeLoader.loadVolume(testDirectory).orElse(null);
        assertNotNull(rv);
        assertFalse(rv.hasXSlices());
        assertFalse(rv.hasYSlices());
        assertTrue(rv.hasZSlices());
        assertArrayEquals(new int[] {234764, 50122, 27931}, rv.getOriginVoxel());
    }

    @Test
    public void loadVolumeWithYZTiles() {
        prepareTestDataFiles("transform.txt", "YZ.0.tif", "YZ.1.tif");
        RenderedVolume rv = renderedVolumeLoader.loadVolume(testDirectory).orElse(null);
        assertNotNull(rv);
        assertTrue(rv.hasXSlices());
        assertFalse(rv.hasYSlices());
        assertFalse(rv.hasZSlices());
    }

    @Test
    public void loadVolumeWithZXTiles() {
        prepareTestDataFiles("transform.txt", "ZX.0.tif", "ZX.1.tif");
        RenderedVolume rv = renderedVolumeLoader.loadVolume(testDirectory).orElse(null);
        assertNotNull(rv);
        assertFalse(rv.hasXSlices());
        assertTrue(rv.hasYSlices());
        assertFalse(rv.hasZSlices());
    }

    @Test
    public void loadVolumeWithAllOrthoTiles() {
        prepareTestDataFiles("transform.txt", "default.0.tif", "default.1.tif", "YZ.0.tif", "YZ.1.tif", "ZX.0.tif", "ZX.1.tif");
        RenderedVolume rv = renderedVolumeLoader.loadVolume(testDirectory).orElse(null);
        assertNotNull(rv);
        assertTrue(rv.hasXSlices());
        assertTrue(rv.hasYSlices());
        assertTrue(rv.hasZSlices());
    }

    @Test
    public void loadXYSlice() {
        prepareTestDataFiles("transform.txt", "default.0.tif", "default.1.tif");
        byte[] sliceBytes = renderedVolumeLoader.loadVolume(testDirectory)
                .flatMap(rv -> rv.getTileInfo(CoordinateAxis.Z)
                        .map(tileInfo -> TileKey.fromTileCoord(
                                0,
                                0,
                                0,
                                rv.getNumZoomLevels() - 1,
                                CoordinateAxis.Z,
                                0))
                        .flatMap(tileIndex -> renderedVolumeLoader.loadSlice(rv, tileIndex)))
                .orElse(null);
        assertNotNull(sliceBytes);
    }

    @Test
    public void loadSingleChannelXYSlice() {
        prepareTestDataFiles("transform.txt", "default.0.tif");
        byte[] sliceBytes = renderedVolumeLoader.loadVolume(testDirectory)
                .flatMap(rv -> rv.getTileInfo(CoordinateAxis.Z)
                        .map(tileInfo -> TileKey.fromTileCoord(
                                0,
                                0,
                                0,
                                rv.getNumZoomLevels() - 1,
                                CoordinateAxis.Z,
                                0))
                        .flatMap(tileIndex -> renderedVolumeLoader.loadSlice(rv, tileIndex)))
                .orElse(null);
        assertNotNull(sliceBytes);
    }

    @Test
    public void loadMissingXYSlice() {
        prepareTestDataFiles("transform.txt", "default.0.tif", "default.1.tif");
        byte[] sliceBytes = renderedVolumeLoader.loadVolume(testDirectory)
                .flatMap(rv -> rv.getTileInfo(CoordinateAxis.Z)
                        .map(tileInfo -> TileKey.fromTileCoord(
                                1,
                                1,
                                1,
                                0,
                                CoordinateAxis.Z,
                                0))
                        .flatMap(tileIndex -> renderedVolumeLoader.loadSlice(rv, tileIndex)))
                .orElse(null);
        assertNull(sliceBytes);
    }

    @Test
    public void retrieveClosestRawImage() {
        prepareTestDataFiles("transform.txt", "default.0.tif", "default.1.tif", "tilebase.cache.yml");
        RawImage rawImage = renderedVolumeLoader.findClosestRawImageFromVoxelCoord(testDirectory, 0, 0, 0)
                .orElse(null);
        assertNotNull(rawImage);
    }

    @Test
    public void noRawImageFound() {
        RawImage rawImage = renderedVolumeLoader.findClosestRawImageFromVoxelCoord(testDirectory, 0, 0, 0)
                .orElse(null);
        assertNull(rawImage);
    }

    @Test
    public void loadRawImageContent() {
        prepareTestDataFiles(ImmutableMap.of(
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
        TestData[] testData = new TestData[] {
                new TestData(0, 0, 0, -1, -1, -1, 0, imageBytes -> assertNotNull("Test 0", imageBytes)),
                new TestData(0, 0, 0, -1, -1, -1, 1, imageBytes -> assertNotNull("Test 1", imageBytes)),
                new TestData(0, 0, 0, -1, -1, -1, 2, imageBytes -> assertNull("Test 2", imageBytes)),
                new TestData(10, 11, 9, 10, 10, 3, 0, imageBytes -> assertNotNull("Test 3", imageBytes)),
                new TestData(10, 11, 9, 100, 100, 100, 0, imageBytes -> assertNotNull("Test 4", imageBytes)),
                new TestData(10, 11, 9, -1, -1, -1, 0, imageBytes -> assertNotNull("Test 5", imageBytes)),
        };
        for (TestData td : testData) {
            td.resultAssertion.accept(renderedVolumeLoader.loadRawImageContentFromVoxelCoord(rawImage, td.xVoxel, td.yVoxel, td.zVoxel, td.dimx, td.dimy, td.dimz, td.channel));
        }
    }

    private void prepareTestDataFiles(String... testDataFileNames) {
        prepareTestDataFiles(Stream.of(testDataFileNames).collect(Collectors.toMap(fn -> fn, fn -> fn)));
    }

    private void prepareTestDataFiles(Map<String, String> testDataFileMapping) {
        try {
            Path testDataDir = Paths.get(TEST_DATADIR);
            for (String testFileName : testDataFileMapping.keySet()) {
                // copy file and rename it.
                Path dest = testDirectory.resolve(testDataFileMapping.get(testFileName));
                if (Files.notExists(dest.getParent())) {
                    Files.createDirectories(dest.getParent());
                }
                Files.copy(testDataDir.resolve(testFileName), dest);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
