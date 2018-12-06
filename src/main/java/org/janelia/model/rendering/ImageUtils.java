package org.janelia.model.rendering;

import javax.media.jai.RenderedImageAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

class ImageUtils {

    private static final int DEFAULT_BORDER = 0;

    static byte[] renderedImageToBytes(RenderedImage renderedImage) {
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

    static BufferedImage renderedImageToBufferedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        } else {
            RenderedImageAdapter imageAdapter = new RenderedImageAdapter(img);
            return imageAdapter.getAsBufferedImage();
        }
    }
}
