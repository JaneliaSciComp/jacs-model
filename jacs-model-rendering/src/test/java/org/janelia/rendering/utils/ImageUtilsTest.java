package org.janelia.rendering.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;
import org.janelia.rendering.NamedSupplier;
import org.janelia.rendering.RenderedImageInfo;
import org.janelia.testutils.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageUtilsTest {
    private static final String TEST_DATADIR = "src/test/resources/testdata/rendering";

    private static Path testSuiteDirectory;

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
        testDirectory = Files.createTempDirectory(testSuiteDirectory, null);
    }

    @Test
    public void loadTiffPixelBytes() throws IOException {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "default.0.tif");
        Path testFilePath = testDirectory.resolve("default.0.tif");
        RenderedImageInfo imageInfo;
        try (InputStream tiffStream = Files.newInputStream(testFilePath)) {
            imageInfo = ImageUtils.loadImageInfoFromTiffStream(tiffStream);
            assertNotNull(imageInfo);
        }
        try (InputStream tiffStream = Files.newInputStream(testFilePath)) {
            byte[] imageBytes = ImageUtils.loadImagePixelBytesFromTiffStream(tiffStream, -1, -1, -1, -1, -1, -1);
            assertNotNull(imageBytes);
            // save the bytes as tiff to make sure they can be read
            Path testOutputFilePath = testDirectory.resolve("outputTiff.tif");
            try (OutputStream testOutputStream = Files.newOutputStream(testOutputFilePath)) {
                TIFFEncodeParam param = new TIFFEncodeParam();
                ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", testOutputStream, param);
                Iterator<BufferedImage> pagesIterator = IntStream.range(0, imageInfo.sz)
                        .mapToObj(sliceIndex -> extractSliceFromImageBytes(imageBytes, imageInfo, sliceIndex))
                        .iterator();
                param.setExtraImages(pagesIterator);
                encoder.encode(pagesIterator.next());
                testOutputStream.flush();
            }
            try (FileSeekableStream tiffResultStream = new FileSeekableStream(testOutputFilePath.toFile())) {
                RenderedImageInfo resultImageInfo = ImageUtils.loadImageInfoFromTiffStream(tiffResultStream);
                assertNotNull(resultImageInfo);
                assertEquals(imageInfo.sx, resultImageInfo.sx);
                assertEquals(imageInfo.sy, resultImageInfo.sy);
                assertEquals(imageInfo.sz, resultImageInfo.sz);
                assertEquals(imageInfo.cmPixelSize, resultImageInfo.cmPixelSize);
                assertEquals(imageInfo.sRGBspace, resultImageInfo.sRGBspace);
            }
        }
    }

    private BufferedImage extractSliceFromImageBytes(byte[] imageBytes, RenderedImageInfo imageInfo, int sliceIndex) {
        BufferedImage bufferedImage = new BufferedImage(imageInfo.sx, imageInfo.sy, BufferedImage.TYPE_USHORT_GRAY);
        int sliceSize = imageInfo.sx * imageInfo.sy * (imageInfo.cmPixelSize / 8);
        int bufferOffset = sliceIndex * sliceSize;
        for (int y = 0; y < imageInfo.sy; y++) {
            for (int x = 0; x < imageInfo.sx; x++) {
                int pixel = imageBytes[bufferOffset] & 0x000000ff;
                pixel |= (imageBytes[bufferOffset + 1] & 0x000000ff) << 8;
                bufferedImage.setRGB(x, y, pixel);
                bufferOffset += 2;
            }
        }
        return bufferedImage;
    }

    @Test
    public void combineSlices() {
        TestUtils.prepareTestDataFiles(Paths.get(TEST_DATADIR), testDirectory, "default.0.tif", "default.1.tif");
        Path testFile0 = testDirectory.resolve("default.0.tif");
        Path testFile1 = testDirectory.resolve("default.1.tif");

        byte[] contentBytes = ImageUtils.bandMergedTextureBytesFromImageStreams(
                Stream.of(testFile0, testFile1)
                                .map(p  -> NamedSupplier.namedSupplier(
                                        p.getFileName().toString(),
                                        () -> {
                                            try {
                                                return Files.newInputStream(p);
                                            } catch (IOException e) {
                                                throw new UncheckedIOException(e);
                                            }
                                        })),
                /* pageNumber */10);

        assertNotNull(contentBytes);
    }
}
