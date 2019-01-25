package org.janelia.model.util;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codecimpl.TIFFImageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.NullOpImage;
import javax.media.jai.RenderedImageAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ImageUtils {

    private static final int DEFAULT_BORDER = 0;
    private static final Logger LOG = LoggerFactory.getLogger(ImageUtils.class);

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

    public static byte[] loadImageFromTiffStream(InputStream inputStream,
                                                 int x0, int y0, int z0,
                                                 int deltax, int deltay, int deltaz) {
        SeekableStream tiffStream;
        try {
            if (inputStream instanceof SeekableStream) {
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

            // allocate the buffer
            int sliceSize = (endX - startX) * (endY - startY);
            int totalVoxels = sliceSize * (endZ - startZ);
            byte[] rgbBuffer = new byte[totalVoxels * bytesPerPixel];

            PixelDataHandlers<?> pixelDataHandlers = createDataHandlers(imageWidth, imageHeight, bytesPerPixel);;

            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
            IntStream.range(0, endZ - startZ)
                    .forEach(sliceIndex -> {
                        try {
                            BufferedImage sliceImage = ImageUtils.renderedImageToBufferedImage(
                                    new NullOpImage(
                                            decoder.decodeAsRenderedImage(startZ + sliceIndex),
                                            null,
                                            null,
                                            NullOpImage.OP_IO_BOUND));
                            transferPixels(sliceImage, startX, startY, endX, endY, rgbBuffer,
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

    private static int clamp(int min, int max, int startingValue) {
        int rtnVal = startingValue;
        if ( startingValue < min ) {
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

    public static byte[] renderedImageToBytes(RenderedImage renderedImage) {
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
            textureCoordX = usedWidth / (float)width;
        } else {
            width = usedWidth;
            textureCoordX = 1.0f;
        }
        int height = rgbImage.getHeight();
        int border = DEFAULT_BORDER;
        int srgb = colorModel.getColorSpace().isCS_sRGB() ? 1 : 0;
        int channelCount = colorModel.getNumComponents();
        int perChannelBitDepth = colorModel.getPixelSize() / channelCount;
        int bitDepth;
        // treat indexed image as rgb
        if (perChannelBitDepth < 8)
            bitDepth = 8;
        else
            bitDepth = perChannelBitDepth;
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
        dataBytesBuffer.putInt(border);
        dataBytesBuffer.putInt(srgb);
        dataBytesBuffer.putInt(bitDepth);
        dataBytesBuffer.putInt(channelCount);
        dataBytesBuffer.putFloat(textureCoordX);

        dataBytesBuffer.order(ByteOrder.nativeOrder());

        Raster raster = rgbImage.getData();

        int pixelData[] = new int[channelCount];
        int padData[] = new int[channelCount]; // color for edge padding
        final boolean is16Bit = bitDepth == 16;
        if (is16Bit) {
            ShortBuffer shortDataBuffer = dataBytesBuffer.asShortBuffer(); // for 16-bit case
            for (int y = 0; y < height; y++) {
                // Choose ragged right edge pad color from right
                // edge of used portion of scan line.
                raster.getPixel(usedWidth-1, y, padData);
                for (int x = 0; x < width; ++x) {
                    if (x < usedWidth) { // used portion of scan line
                        raster.getPixel(x, y, pixelData);
                        for (int i : pixelData) {
                            shortDataBuffer.put((short)i);
                        }
                    } else { // (not zero) pad right edge
                        for (int i : padData) {
                            shortDataBuffer.put((short)i);
                        }
                    }
                }
            }
        } else { // 8-bit
            for (int y = 0; y < height; ++y) {
                raster.getPixel(usedWidth-1, y, padData);
                for (int x = 0; x < width; ++x) {
                    if (x < usedWidth) {
                        raster.getPixel(x, y, pixelData);
                        for (int i : pixelData) {
                            dataBytesBuffer.put((byte)i);
                        }
                    } else { // zero pad right edge
                        for (int i : padData) {
                            dataBytesBuffer.put((byte)i);
                        }
                    }
                }
            }
        }
        return dataBytesArray;
    }

    public static BufferedImage renderedImageToBufferedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        } else {
            RenderedImageAdapter imageAdapter = new RenderedImageAdapter(img);
            return imageAdapter.getAsBufferedImage();
        }
    }

}
