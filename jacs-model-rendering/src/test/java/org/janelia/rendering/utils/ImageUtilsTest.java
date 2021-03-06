package org.janelia.rendering.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.IntStream;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import org.janelia.rendering.RenderedImageInfo;
import org.janelia.testutils.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        File testFile = testDirectory.resolve("default.0.tif").toFile();
        RenderedImageInfo imageInfo;
        try (FileSeekableStream tiffStream = new FileSeekableStream(testFile)) {
            imageInfo = ImageUtils.loadImageInfoFromTiffStream(tiffStream);
        }
        try (FileSeekableStream tiffStream = new FileSeekableStream(testFile)) {
            byte[] imageBytes = ImageUtils.loadImagePixelBytesFromTiffStream(tiffStream, -1, -1, -1, -1, -1, -1);
            // save the bytes as tiff to make sure they can be read
            int imageSize = imageInfo.sx * imageInfo.sy * (imageInfo.cmPixelSize / 8);
            Iterator<BufferedImage> pagesIterator = IntStream.range(0, imageInfo.sz)
                    .mapToObj(sliceIndex -> {
                        BufferedImage bufferedImage = new BufferedImage(imageInfo.sx, imageInfo.sy, BufferedImage.TYPE_USHORT_GRAY);
                        int bufferOffset = sliceIndex * imageSize;
                        for (int y = 0; y < imageInfo.sy; y++) {
                            for (int x = 0; x < imageInfo.sx; x++) {
                                int pixel = imageBytes[bufferOffset] & 0x000000ff;
                                pixel |= (imageBytes[bufferOffset + 1] & 0x000000ff) << 8;
                                bufferedImage.setRGB(x, y, pixel);
                                bufferOffset += 2;
                            }
                        }
                        return bufferedImage;
                    })
                    .iterator();
            File testOutputFile = testDirectory.resolve("outputTiff.tif").toFile();
            OutputStream testOutputStream = new FileOutputStream(testOutputFile);
            TIFFEncodeParam param = new TIFFEncodeParam();
            ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", testOutputStream, param);
            param.setExtraImages(pagesIterator);
            encoder.encode(pagesIterator.next());
            testOutputStream.flush();
            testOutputStream.close();
            try (FileSeekableStream tiffResultStream = new FileSeekableStream(testOutputFile)) {
                RenderedImageInfo resultImageInfo = ImageUtils.loadImageInfoFromTiffStream(tiffResultStream);
                assertEquals(imageInfo.sx, resultImageInfo.sx);
                assertEquals(imageInfo.sy, resultImageInfo.sy);
                assertEquals(imageInfo.sz, resultImageInfo.sz);
                assertEquals(imageInfo.cmPixelSize, resultImageInfo.cmPixelSize);
                assertEquals(imageInfo.sRGBspace, resultImageInfo.sRGBspace);
            }
        }
    }

}
