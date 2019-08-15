package org.janelia.rendering.utils;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.media.jai.JAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedImageAdapter;

import com.google.common.base.Stopwatch;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codecimpl.TIFFImageDecoder;

import org.janelia.rendering.NamedSupplier;
import org.janelia.rendering.RenderedImageInfo;
import org.janelia.rendering.RenderedImageWithStream;
import org.janelia.rendering.RenderedImagesWithStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtils {

    private static final int DEFAULT_BORDER = 0;
    private static final Logger LOG = LoggerFactory.getLogger(ImageUtils.class);

    private static class TiffROI {
        SeekableStream tiffStream;
        int imageWidth;
        int imageHeight;
        int numSlices;
        int bytesPerPixel;
        int startX;
        int endX;
        int startY;
        int endY;
        int startZ;
        int endZ;
    }

    @FunctionalInterface
    private interface DataCopier<B> {
        /**
         * Copies the pixel data from the source offset to the destination offset and returns then next destination offset
         * @param sourceBuffer
         * @param sourceOffset
         * @param destBuffer
         * @param destOffset
         * @return the next destination offset
         */
        int copyFrom(B sourceBuffer, int sourceOffset, byte[] destBuffer, int destOffset);
    }

    private static class PixelDataHandlers<B> {
        private final Function<BufferedImage, B> pixelDataSupplier;
        private final BiFunction<Integer, Integer, Integer> pixelOffsetCalculator;
        private final DataCopier<B> pixelCopier;

        PixelDataHandlers(Function<BufferedImage, B> pixelDataSupplier,
                          BiFunction<Integer, Integer, Integer> pixelOffsetCalculator,
                          DataCopier<B> pixelCopier) {
            this.pixelDataSupplier = pixelDataSupplier;
            this.pixelOffsetCalculator = pixelOffsetCalculator;
            this.pixelCopier = pixelCopier;
        }
    }

    public static Function<Path, InputStream> getImagePathHandler() {
        return (Path p) -> {
            try {
                return new FileSeekableStream(p.toFile());
            } catch (IOException e) {
                LOG.error("Error opening {}", p, e);
                throw new IllegalArgumentException(e);
            }
        };
    }

    @Nullable
    public static RenderedImageInfo loadImageInfoFromTiffStream(InputStream inputStream) {
        SeekableStream tiffStream;
        if (inputStream == null) {
            return null;
        } else if (inputStream instanceof SeekableStream) {
            tiffStream = (SeekableStream) inputStream;
        } else {
            tiffStream = new MemoryCacheSeekableStream(inputStream);
        }
        try {
            TIFFDirectory tiffDirectory = new TIFFDirectory(tiffStream, 0);
            int imageWidth = (int) tiffDirectory.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_WIDTH);
            int imageHeight = (int) tiffDirectory.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_LENGTH);
            int numSlices = TIFFDirectory.getNumDirectories(tiffStream);

            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
            RenderedImageAdapter ria = new RenderedImageAdapter(decoder.decodeAsRenderedImage(0));
            ColorModel colorModel = ria.getColorModel();
            return new RenderedImageInfo(imageWidth, imageHeight, numSlices, colorModel.getPixelSize(), colorModel.getColorSpace().isCS_sRGB());
        } catch (Exception e) {
            LOG.error("Error reading TIFF image stream", e);
            throw new IllegalStateException(e);
        } finally {
            try {
                tiffStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    @Nullable
    public static byte[] loadImagePixelBytesFromTiffStream(InputStream inputStream,
                                                           int x0, int y0, int z0,
                                                           int deltax, int deltay, int deltaz) {
        try {
            TiffROI tiffROI = getROIFromTiffStream(inputStream, x0, y0, z0, deltax, deltay, deltaz);
            if (tiffROI == null) {
                return null;
            }
            // allocate the buffer
            int sliceSize = (tiffROI.endX - tiffROI.startX) * (tiffROI.endY - tiffROI.startY);
            int totalVoxels = sliceSize * (tiffROI.endZ - tiffROI.startZ);
            byte[] rgbBuffer = new byte[totalVoxels * tiffROI.bytesPerPixel];

            PixelDataHandlers<?> pixelDataHandlers = createDataHandlers(tiffROI.imageWidth, tiffROI.imageHeight, tiffROI.bytesPerPixel);;

            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffROI.tiffStream, null);
            IntStream.range(0, tiffROI.endZ - tiffROI.startZ)
                    .forEach(sliceIndex -> {
                        try {
                            BufferedImage sliceImage = ImageUtils.renderedImageToBufferedImage(
                                    new NullOpImage(
                                    decoder.decodeAsRenderedImage(tiffROI.startZ + sliceIndex),
                                    null,
                                    null,
                                    NullOpImage.OP_IO_BOUND),
                                    0, 0, -1, -1);
                            transferPixels(sliceImage, tiffROI.startX, tiffROI.startY, tiffROI.endX, tiffROI.endY, rgbBuffer,
                                    sliceIndex * sliceSize, pixelDataHandlers);
                        } catch (IOException e) {
                            LOG.error("Error reading slice {}", sliceIndex, e);
                            throw new IllegalStateException(e);
                        }
                    });
            return rgbBuffer;
        } catch (Exception e) {
            LOG.error("Error reading TIFF image stream", e);
            throw new IllegalStateException(e);
        }
    }

    public static long sizeImagePixelBytesFromTiffStream(InputStream inputStream,
                                                         int x0, int y0, int z0,
                                                         int deltax, int deltay, int deltaz) {
        TiffROI tiffROI = getROIFromTiffStream(inputStream, x0, y0, z0, deltax, deltay, deltaz);
        if (tiffROI == null) {
            return 0L;
        } else {
            try {
                return (tiffROI.endX - tiffROI.startX) *
                        (tiffROI.endY - tiffROI.startY) *
                        (tiffROI.endZ - tiffROI.startZ) *
                        tiffROI.bytesPerPixel;
            } finally {
                try {
                    tiffROI.tiffStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Nullable
    private static TiffROI getROIFromTiffStream(InputStream inputStream,
                                                int x0, int y0, int z0,
                                                int deltax, int deltay, int deltaz) {
        SeekableStream tiffStream;
        try {
            if (inputStream == null) {
                return null;
            } else if (inputStream instanceof SeekableStream) {
                tiffStream = (SeekableStream) inputStream;
            } else {
                tiffStream = new MemoryCacheSeekableStream(inputStream);
            }
            TIFFDirectory tiffDirectory = new TIFFDirectory(tiffStream, 0);
            int imageWidth = (int) tiffDirectory.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_WIDTH);
            int imageHeight = (int) tiffDirectory.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_LENGTH);
            int numSlices = TIFFDirectory.getNumDirectories(tiffStream);
            int bitsPerPixel = (int) tiffDirectory.getFieldAsLong(TIFFImageDecoder.TIFF_BITS_PER_SAMPLE);
            int bytesPerPixel = bitsPerPixel / 8;

            int startX;
            int endX;
            int startY;
            int endY;
            int startZ;
            int endZ;
            if (deltax != -1) {
                startX = clamp(0, imageWidth - deltax,
                        x0 - deltax / 2);
                endX = clamp(0, imageWidth, startX + deltax);
            } else {
                startX = 0;
                endX = imageWidth;
            }
            if (deltay != -1) {
                startY = clamp(0, imageHeight - deltay,
                        y0 - deltay / 2);
                endY = clamp(0, imageHeight, startY + deltay);
            } else {
                startY = 0;
                endY = imageHeight;
            }
            if (deltaz > 0) {
                startZ = clamp(0, numSlices - deltaz, z0 - deltaz / 2);
                endZ = clamp(0, numSlices, startZ + deltaz);
            } else {
                startZ = 0;
                endZ = numSlices;
            }
            if (startZ >= endZ) {
                return null;
            }

            TiffROI tiffROI = new TiffROI();
            tiffROI.tiffStream = tiffStream;
            tiffROI.imageWidth = imageWidth;
            tiffROI.imageHeight = imageHeight;
            tiffROI.numSlices = numSlices;
            tiffROI.bytesPerPixel = bytesPerPixel;
            tiffROI.startX = startX;
            tiffROI.endX = endX;
            tiffROI.startY = startY;
            tiffROI.endY = endY;
            tiffROI.startZ = startZ;
            tiffROI.endZ = endZ;

            return tiffROI;
        } catch (Exception e) {
            LOG.error("Error reading TIFF image stream", e);
            throw new IllegalStateException(e);
        }
    }

    @Nullable
    public static byte[] loadRenderedImageBytesFromTiffStream(InputStream inputStream, int x0, int y0, int z0, int deltax, int deltay, int deltaz) {
        SeekableStream tiffStream;
        if (inputStream == null) {
            return null;
        } else if (inputStream instanceof SeekableStream) {
            tiffStream = (SeekableStream) inputStream;
        } else {
            tiffStream = new MemoryCacheSeekableStream(inputStream);
        }
        try {
            int numSlices = TIFFDirectory.getNumDirectories(tiffStream);
            int startZ;
            int endZ;
            if (deltaz > 0) {
                startZ = clamp(0, numSlices - 1, z0);
                endZ = clamp(0, numSlices, startZ + deltaz);
            } else {
                startZ = 0;
                endZ = numSlices;
            }
            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
            Iterator<BufferedImage> pagesIterator = IntStream.range(0, endZ - startZ)
                    .mapToObj(sliceIndex -> {
                        try {
                            return ImageUtils.renderedImageToBufferedImage(
                                    new NullOpImage(
                                            decoder.decodeAsRenderedImage(startZ + sliceIndex),
                                            null,
                                            null,
                                            NullOpImage.OP_IO_BOUND),
                                    x0, y0, deltax, deltay);
                        } catch (IOException e) {
                            LOG.error("Error reading slice {}", sliceIndex, e);
                            throw new IllegalStateException(e);
                        }
                    })
                    .iterator();
            if (endZ - startZ <= 0) {
                return null;
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                TIFFEncodeParam param = new TIFFEncodeParam();
                ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", outputStream, param);
                param.setExtraImages(pagesIterator);
                encoder.encode(pagesIterator.next());
                outputStream.flush();
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            LOG.error("Error reading TIFF image stream", e);
            throw new IllegalStateException(e);
        } finally {
            try {
                tiffStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static byte[] bandMergedTextureBytesFromImageStreams(Stream<NamedSupplier<InputStream>> imageStreamsSuppliers, int pageNumber) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stream<RenderedImagesWithStreamsSupplier> imageSuppliers = imageStreamsSuppliers
                .map(isSupplier -> () -> {
                    RenderedImagesWithStreams rims = ImageUtils.loadRenderedImageFromTiffStream(isSupplier, pageNumber);
                    if (rims == null) {
                        return Optional.empty();
                    } else {
                        return Optional.of(rims);
                    }
                });
        RenderedImagesWithStreams mergedImages = ImageUtils.mergeImages(imageSuppliers);
        RenderedImageWithStream imageResult = mergedImages.combine("bandmerge");
        if (imageResult == null) {
            return null;
        } else {
            try {
                return ImageUtils.renderedImageToTextureBytes(imageResult.getRenderedImage());
            } finally {
                imageResult.close();
                LOG.debug("bandMergedTextureBytesFromImageStreams page {} for {} took {} ms", pageNumber, mergedImages.getRenderedImageNames(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    public static long sizeBandMergedTextureBytesFromImageStreams(Stream<NamedSupplier<InputStream>> imageStreamsSuppliers, int pageNumber) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stream<RenderedImagesWithStreamsSupplier> imageSuppliers = imageStreamsSuppliers
                .map(isSupplier -> () -> {
                    RenderedImagesWithStreams rims = ImageUtils.loadRenderedImageFromTiffStream(isSupplier, pageNumber);
                    if (rims == null) {
                        return Optional.empty();
                    } else {
                        return Optional.of(rims);
                    }
                });
        RenderedImagesWithStreams mergedImages = ImageUtils.mergeImages(imageSuppliers);
        RenderedImageWithStream imageResult = mergedImages.combine("bandmerge");
        if (imageResult == null) {
            return 0L;
        } else {
            try {
                return ImageUtils.sizeOfRenderedImageAsTextureBytes(imageResult.getRenderedImage());
            } finally {
                imageResult.close();
                LOG.debug("sizeBandMergedTextureBytesFromImageStreams page {} for {} took {} ms", pageNumber, mergedImages.getRenderedImageNames(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    private static RenderedImagesWithStreams mergeImages(Stream<RenderedImagesWithStreamsSupplier> imageSuppliers) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        RenderedImagesWithStreams mergedImage = imageSuppliers
                .map(rims -> rims.get().orElse(null))
                .filter(rim -> rim != null)
                .reduce(RenderedImagesWithStreams.empty(), (r1, r2) -> r1.append(r2))
                ;
        LOG.debug("mergeImages {} took {} ms", mergedImage.getRenderedImageNames(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return mergedImage;
    }

    @Nullable
    private static RenderedImagesWithStreams loadRenderedImageFromTiffStream(NamedSupplier<InputStream> namedInputStreamSupplier, int pageNumber) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        SeekableStream tiffStream;
        InputStream inputStream = namedInputStreamSupplier.get();
        if (inputStream == null) {
            return null;
        } else if (inputStream instanceof SeekableStream) {
            tiffStream = (SeekableStream) inputStream;
        } else {
            tiffStream = new MemoryCacheSeekableStream(inputStream);
        }
        try {
            LOG.debug("Load page {} from {}", pageNumber, namedInputStreamSupplier.getName());
            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
            LOG.debug("Created decoder for page {} from {} after {} ms", pageNumber, namedInputStreamSupplier.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            RenderedImage renderedImage = new NullOpImage(
                    decoder.decodeAsRenderedImage(pageNumber),
                    null,
                    null,
                    OpImage.OP_IO_BOUND);
            return RenderedImagesWithStreams.withImageAndStream(namedInputStreamSupplier.getName(), renderedImage, tiffStream);
        } catch (Exception e) {
            LOG.error("Error reading TIFF image stream", e);
            throw new IllegalStateException(e);
        } finally {
            LOG.debug("Finished loading page {} from {} after {} ms", pageNumber, namedInputStreamSupplier.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private static int clamp(int min, int max, int startingValue) {
        int rtnVal = startingValue;
        if (startingValue < min) {
            rtnVal = min;
        } else if ( startingValue > max ) {
            rtnVal = max;
        }
        return rtnVal;
    }

    private static PixelDataHandlers<?> createDataHandlers(int imageWidth, int imageHeight, int bytesPerPixel) {
        PixelDataHandlers<?> pixelDataHandlers;
        switch (bytesPerPixel) {
            case 1: {
                pixelDataHandlers = new PixelDataHandlers<>(
                        image -> {
                            DataBufferByte db = ((DataBufferByte) image.getTile(0, 0).getDataBuffer());
                            return db.getData();
                        },
                        (x, y) -> x * imageWidth + y,
                        (sBuffer, sOffset, dBuffer, dOffset) -> {
                            sBuffer[sOffset] = dBuffer[dOffset];
                            return dOffset + 1;
                        }
                );
                break;
            }
            case 2: {
                pixelDataHandlers = new PixelDataHandlers<>(
                        image -> {
                            DataBufferUShort db = ((DataBufferUShort) image.getTile(0, 0).getDataBuffer());
                            return db.getData();
                        },
                        (x, y) -> y * imageWidth + x,
                        (sBuffer, sOffset, dBuffer, dOffset) -> {
                            int unsignedPixelVal;
                            int pixelVal = sBuffer[sOffset];
                            if (pixelVal < 0) {
                                unsignedPixelVal = pixelVal + 65536;
                            } else {
                                unsignedPixelVal = pixelVal;
                            }
                            dBuffer[dOffset] = (byte) (unsignedPixelVal & 0x000000ff);
                            dBuffer[dOffset + 1] = (byte) ((unsignedPixelVal >>> 8) & 0x000000ff);
                            return dOffset + 2;
                        }
                );
                break;
            }
            case 4: {
                pixelDataHandlers = new PixelDataHandlers<>(
                        image -> {
                            int[] buffer = new int[imageWidth * imageHeight];
                            image.getRGB(0, 0, imageWidth, imageHeight,
                                    buffer, 0, imageWidth);
                            return buffer;
                        },
                        (x, y) -> x * imageWidth + y,
                        (sBuffer, sOffset, dBuffer, dOffset) -> {
                            long unsignedPixelVal;
                            long pixelVal = sBuffer[sOffset];
                            if (pixelVal < 0) {
                                unsignedPixelVal = pixelVal + Integer.MAX_VALUE;
                            } else {
                                unsignedPixelVal = pixelVal;
                            }
                            dBuffer[dOffset] = (byte) (unsignedPixelVal & 0x000000ff);
                            dBuffer[dOffset + 1] = (byte) ((unsignedPixelVal >>> 8) & 0x000000ff);
                            dBuffer[dOffset + 2] = (byte) ((unsignedPixelVal >>> 16) & 0x000000ff);
                            dBuffer[dOffset + 3] = (byte) ((unsignedPixelVal >>> 24) & 0x000000ff);
                            return dOffset + 4;
                        }
                );
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported value for bytes per pixel - " + bytesPerPixel);
        }
        return pixelDataHandlers;
    }

    private static <B> void transferPixels(
            BufferedImage image,
            int startX, int startY, int endX, int endY,
            byte[] destBuffer, int destOffset,
            PixelDataHandlers<B> pixelDataHandlers) {
        B sourceBuffer = pixelDataHandlers.pixelDataSupplier.apply(image);
        int currentDestOffset = destOffset;
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                currentDestOffset = pixelDataHandlers.pixelCopier.copyFrom(
                        sourceBuffer,
                        pixelDataHandlers.pixelOffsetCalculator.apply(x, y),
                        destBuffer, currentDestOffset);
            }
        }
    }

    public static byte[] renderedImageToTextureBytes(RenderedImage renderedImage) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            RenderedImage rgbImage;
            // If input image uses indexed color table, convert to RGB first.
            if (renderedImage.getColorModel() instanceof IndexColorModel) {
                IndexColorModel indexColorModel = (IndexColorModel) renderedImage.getColorModel();
                rgbImage = indexColorModel.convertToIntDiscrete(renderedImage.getData(), false);
            } else {
                rgbImage = renderedImage;
            }
            ColorModel colorModel = rgbImage.getColorModel();
            int mipmapLevel = 0;
            int usedWidth = rgbImage.getWidth();
            // pad image to a multiple of 8
            int width;
            float textureCoordX;
            if ((usedWidth % 8) != 0) {
                width = usedWidth + 8 - (usedWidth % 8);
                textureCoordX = usedWidth / (float) width;
            } else {
                width = usedWidth;
                textureCoordX = 1.0f;
            }
            int height = rgbImage.getHeight();
            int srgb = colorModel.getColorSpace().isCS_sRGB() ? 1 : 0;
            int channelCount = colorModel.getNumComponents();
            int perChannelBitDepth = colorModel.getPixelSize() / channelCount;
            int bitDepth = Math.max(perChannelBitDepth, 8);
            // treat indexed image as rgb
            int pixelByteCount = channelCount * bitDepth / 8;
            int rowByteCount = pixelByteCount * width;
            int imageByteCount = height * rowByteCount;

            int byteBufferSize = (Integer.SIZE / 8) * 8 + (Float.SIZE / 8) + imageByteCount;
            byte[] dataBytesArray = new byte[byteBufferSize];
            ByteBuffer dataBytesBuffer = ByteBuffer.wrap(dataBytesArray);

            dataBytesBuffer.putInt(mipmapLevel);
            dataBytesBuffer.putInt(width);
            dataBytesBuffer.putInt(usedWidth);
            dataBytesBuffer.putInt(height);
            dataBytesBuffer.putInt(DEFAULT_BORDER);
            dataBytesBuffer.putInt(srgb);
            dataBytesBuffer.putInt(bitDepth);
            dataBytesBuffer.putInt(channelCount);
            dataBytesBuffer.putFloat(textureCoordX);

            ByteBuffer pixelsBuffer = ByteBuffer.wrap(dataBytesArray, dataBytesBuffer.position(), dataBytesBuffer.capacity() - dataBytesBuffer.position());
            pixelsBuffer.order(ByteOrder.nativeOrder());

            Raster raster = rgbImage.getData();

            int pixelData[] = new int[channelCount];
            int padData[] = new int[channelCount]; // color for edge padding
            final boolean is16Bit = bitDepth == 16;
            if (is16Bit) {
                ShortBuffer shortPixelsBuffer = pixelsBuffer.asShortBuffer(); // for 16-bit case
                for (int y = 0; y < height; y++) {
                    // Choose ragged right edge pad color from right
                    // edge of used portion of scan line.
                    raster.getPixel(usedWidth - 1, y, padData);
                    for (int x = 0; x < width; ++x) {
                        if (x < usedWidth) { // used portion of scan line
                            raster.getPixel(x, y, pixelData);
                            for (int i : pixelData) {
                                shortPixelsBuffer.put((short) i);
                            }
                        } else { // (not zero) pad right edge
                            for (int i : padData) {
                                shortPixelsBuffer.put((short) i);
                            }
                        }
                    }
                }
            } else { // 8-bit
                for (int y = 0; y < height; ++y) {
                    raster.getPixel(usedWidth - 1, y, padData);
                    for (int x = 0; x < width; ++x) {
                        if (x < usedWidth) {
                            raster.getPixel(x, y, pixelData);
                            for (int i : pixelData) {
                                pixelsBuffer.put((byte) i);
                            }
                        } else { // zero pad right edge
                            for (int i : padData) {
                                pixelsBuffer.put((byte) i);
                            }
                        }
                    }
                }
            }
            return dataBytesArray;
        } finally {
            LOG.debug("Render texture bytes took {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private static long sizeOfRenderedImageAsTextureBytes(RenderedImage renderedImage) {
        RenderedImage rgbImage;
        // If input image uses indexed color table, convert to RGB first.
        if (renderedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel indexColorModel = (IndexColorModel) renderedImage.getColorModel();
            rgbImage = indexColorModel.convertToIntDiscrete(renderedImage.getData(), false);
        } else {
            rgbImage = renderedImage;
        }
        ColorModel colorModel = rgbImage.getColorModel();
        int usedWidth = rgbImage.getWidth();
        // pad image to a multiple of 8
        int width;
        if ((usedWidth % 8) != 0) {
            width = usedWidth + 8 - (usedWidth % 8);
        } else {
            width = usedWidth;
        }
        int height = rgbImage.getHeight();
        int channelCount = colorModel.getNumComponents();
        int perChannelBitDepth = colorModel.getPixelSize() / channelCount;
        int bitDepth = Math.max(perChannelBitDepth, 8);
        // treat indexed image as rgb
        int pixelByteCount = channelCount * bitDepth / 8;
        int rowByteCount = pixelByteCount * width;
        int imageByteCount = height * rowByteCount;

        return (Integer.SIZE / 8) * 8 + (Float.SIZE / 8) + imageByteCount;
    }

    private static BufferedImage renderedImageToBufferedImage(RenderedImage img, int x0, int y0, int width, int height) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int x = clamp(0, imgWidth, x0);
        int y = clamp(0, imgHeight, y0);
        int w = width < 0 ? imgWidth : clamp(0, imgWidth, width);
        int h = height < 0 ? imgHeight : clamp(0, imgHeight, height);
        boolean extractSubImage;
        if (x > 0 || y > 0 || w < imgWidth || h < imgHeight) {
            LOG.debug("Extract subimage ({}, {}), ({}, {})", x, y, w, h);
            extractSubImage = true;
        } else {
            extractSubImage = false;
        }
        if (img instanceof BufferedImage) {
            BufferedImage bimg = (BufferedImage) img;
            return extractSubImage ? bimg.getSubimage(x, y, w, h) : bimg;
        } else {
            RenderedImageAdapter imageAdapter = new RenderedImageAdapter(img);
            return extractSubImage
                    ? imageAdapter.getAsBufferedImage(new Rectangle(x, y, w, h), img.getColorModel())
                    : imageAdapter.getAsBufferedImage();
        }
    }

}
