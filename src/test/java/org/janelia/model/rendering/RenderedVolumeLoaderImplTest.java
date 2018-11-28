package org.janelia.model.rendering;

import org.janelia.testutils.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private void prepareTestDataFiles(String... testDataFileNames) {
        try {
            Path testDataDir = Paths.get(TEST_DATADIR);
            for (String testFileName : testDataFileNames) {
                Files.copy(testDataDir.resolve(testFileName), testDirectory.resolve(testFileName));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
