package org.janelia.model.rendering;

import Jama.Matrix;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.model.rendering.ymlrepr.RawVolData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.JAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedImageAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RenderedVolumeLoaderImpl implements RenderedVolumeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RenderedVolumeLoaderImpl.class);
    private static final String TRANSFORM_FILE_NAME = "transform.txt";
    private static final String TILED_VOL_BASE_FILE_NAME = "tilebase.cache.yml";
    private static final String XY_CH_TIFF_PATTERN = "default.%s.tif";
    private static final String YZ_CH_TIFF_PATTERN = "YZ.%s.tif";
    private static final String ZX_CH_TIFF_PATTERN = "ZX.%s.tif";
    private static final String RAW_CH_TIFF_PATTERN = "-ngc.%s.tif";

    private static final int START_X_INX = 0;
    private static final int START_Y_INX = 1;
    private static final int START_Z_INX = 2;
    private static final int END_X_INX = 3;
    private static final int END_Y_INX = 4;
    private static final int END_Z_INX = 5;

    private interface RenderedImageProvider {
        void close();
        RenderedImage get();
    }

    @Override
    public Optional<RenderedVolume> loadVolume(Path basePath) {
        LOG.debug("Load volume from {}", basePath);
        return loadVolumeSizeAndCoord(basePath)
                .flatMap(coord -> loadTileInfo(basePath)
                        .map(tileInfos -> {
                            int scaleFactor = (int) coord.getScaleFactor();
                            int[] tileVolumeDims = Arrays.stream(tileInfos)
                                    .filter(tileInfo -> tileInfo != null)
                                    .findFirst()
                                    .map(tileInfo -> tileInfo.getVolumeSize())
                                    .orElseGet(() -> new int[] {0, 0, 0});
                            int[] volumeSizeInVoxels = Arrays.stream(tileVolumeDims).map(tileDim -> tileDim * scaleFactor).toArray();
                            TileInfo xyTileInfo = tileInfos[CoordinateAxis.Z.index()];
                            TileInfo zxTileInfo = tileInfos[CoordinateAxis.Y.index()];
                            TileInfo yzTileInfo = tileInfos[CoordinateAxis.X.index()];
                            return new RenderedVolume(basePath,
                                    RenderingType.OCTREE,
                                    coord.getOriginVoxel(),
                                    volumeSizeInVoxels,
                                    coord.getHighestResMicromsPerVoxel(),
                                    coord.getNumZoomLevels(),
                                    xyTileInfo,
                                    yzTileInfo,
                                    zxTileInfo);
                        }));
    }

    @Override
    public Optional<byte[]> loadSlice(RenderedVolume renderedVolume, TileKey tileKey) {
        return renderedVolume.getTileInfo(tileKey.getSliceAxis())
                .flatMap(tileInfo -> renderedVolume.getRelativeTilePath(tileKey)
                        .map(relativeTilePath -> {
                            LOG.debug("Try to load tile {} from {} : {}", tileKey, renderedVolume.getBasePath(), relativeTilePath);
                            return IntStream.range(0, tileInfo.getChannelCount())
                                    .mapToObj(channel -> renderedVolume.getBasePath()
                                            .resolve(relativeTilePath)
                                            .resolve(getFilenameForChannel(tileKey.getSliceAxis(), channel)))
                                    .collect(Collectors.toList());
                        })
                        .flatMap(channelFiles -> channelFiles.stream()
                                .filter(channelFile -> channelFile.toFile().exists())
                                .map(channelFile -> {
                                    LOG.debug("Read TIFF file {} for tile {}", channelFile, tileKey);
                                    return readImage(channelFile, tileKey.getSliceIndex());
                                })
                                .collect(Collectors.collectingAndThen(Collectors.toList(), renderedImageProviders -> {
                                    if (renderedImageProviders.isEmpty()) {
                                        return Optional.empty();
                                    } else {
                                        try {
                                            if (renderedImageProviders.size() == 1) {
                                                return Optional.of(ImageUtils.renderedImageToBytes(renderedImageProviders.get(0).get()));
                                            } else {
                                                ParameterBlock combinedChannelsPB = new ParameterBlockJAI("bandmerge");
                                                renderedImageProviders.forEach(rimp -> combinedChannelsPB.addSource(rimp.get()));
                                                return Optional.of(ImageUtils.renderedImageToBytes(JAI.create("bandmerge", combinedChannelsPB, null)));
                                            }
                                        } finally {
                                            renderedImageProviders.forEach(RenderedImageProvider::close);
                                        }
                                    }
                                })))
                );
    }

    private Optional<RawCoord> loadVolumeSizeAndCoord(Path basePath) {
        try {
            Path transformFilePath = basePath.resolve(TRANSFORM_FILE_NAME);
            if (Files.exists(transformFilePath)) {
                return Optional.of(parseTransformFile(transformFilePath));
            } else {
                LOG.warn("No transform file found in {} folder", basePath);
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RawCoord parseTransformFile(Path p) {
        List<String> lines;
        try {
            lines = Files.readAllLines(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        RawCoord coord = new RawCoord();
        Splitter nameValueSplitter = Splitter.on(':').trimResults().omitEmptyStrings().limit(2);
        for (String l : lines) {
            if (l.startsWith("#")) { // comment line
                continue;
            }
            List<String> nameAndValue = nameValueSplitter.splitToList(l.toLowerCase());
            if (nameAndValue.size() < 2) {
                continue;
            }
            switch (nameAndValue.get(0)) {
                case "ox":
                    coord.setOriginInNanos(CoordinateAxis.X, Integer.valueOf(nameAndValue.get(1)));
                    break;
                case "oy":
                    coord.setOriginInNanos(CoordinateAxis.Y, Integer.valueOf(nameAndValue.get(1)));
                    break;
                case "oz":
                    coord.setOriginInNanos(CoordinateAxis.Z, Integer.valueOf(nameAndValue.get(1)));
                    break;
                case "sx":
                    coord.setLowestResNanosPerVoxel(CoordinateAxis.X, Double.valueOf(nameAndValue.get(1)));
                    break;
                case "sy":
                    coord.setLowestResNanosPerVoxel(CoordinateAxis.Y, Double.valueOf(nameAndValue.get(1)));
                    break;
                case "sz":
                    coord.setLowestResNanosPerVoxel(CoordinateAxis.Z, Double.valueOf(nameAndValue.get(1)));
                    break;
                case "nl":
                    coord.setNumZoomLevels(Integer.valueOf(nameAndValue.get(1)));
                    break;
                default:
                    break;
            }
        }
        return coord;
    }

    private Optional<TileInfo[]> loadTileInfo(Path basePath) {
        try {
            Pattern xyTilePattern = Pattern.compile(String.format(XY_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Pattern yzTilePattern = Pattern.compile(String.format(YZ_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Pattern zxTilePattern = Pattern.compile(String.format(ZX_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            List<Path> channelTiles = Files.find(basePath, 1,
                    (p, a) -> xyTilePattern.asPredicate()
                            .or(yzTilePattern.asPredicate())
                            .or(zxTilePattern.asPredicate()).test(p.getFileName().toString()))
                    .collect(Collectors.toList());
            Map<String, List<Pair<String, Path>>> channelTilesByOrthoProjection =
                    channelTiles.stream()
                    .map(p -> {
                        String fileName = p.getFileName().toString();
                        if (xyTilePattern.matcher(fileName).find()) {
                            return ImmutablePair.of(XY_CH_TIFF_PATTERN, p);
                        } else if (yzTilePattern.matcher(fileName).find()) {
                            return ImmutablePair.of(YZ_CH_TIFF_PATTERN, p);
                        } else {
                            // there should not be other option since the pattern was also used to retrieve the files
                            return ImmutablePair.of(ZX_CH_TIFF_PATTERN, p);
                        }
                    })
                    .collect(Collectors.groupingBy(p -> p.getLeft(), () -> new LinkedHashMap<>(), Collectors.toList()));
            if (channelTilesByOrthoProjection.isEmpty()) {
                return Optional.empty();
            }
            TileInfo[] tileInfos = new TileInfo[3];
            if (channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN) != null) {
                tileInfos[CoordinateAxis.Z.index()] = loadTileInfoFromTiff(
                        channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN).get(0).getRight(),
                        channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN).size(),
                        CoordinateAxis.Z);
            } else {
                tileInfos[CoordinateAxis.Z.index()] = null;
            }
            if (channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN) != null) {
                tileInfos[CoordinateAxis.X.index()] = loadTileInfoFromTiff(
                        channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN).get(0).getRight(),
                        channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN).size(),
                        CoordinateAxis.X);
            } else {
                tileInfos[CoordinateAxis.X.index()] = null;
            }
            if (channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN) != null) {
                tileInfos[CoordinateAxis.Y.index()] = loadTileInfoFromTiff(
                        channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN).get(0).getRight(),
                        channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN).size(),
                        CoordinateAxis.Y);
            } else {
                tileInfos[CoordinateAxis.Y.index()] = null;
            }
            return Optional.of(tileInfos);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private TileInfo loadTileInfoFromTiff(Path tiffPath, int nChannels, CoordinateAxis sliceAxis) {
        LOG.debug("Load tile info from TIFF {}", tiffPath);
        try (SeekableStream tiffStream = new FileSeekableStream(tiffPath.toFile())) {
            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
            // Get X/Y dimensions from first image
            RenderedImageAdapter ria = new RenderedImageAdapter(decoder.decodeAsRenderedImage(0));
            int sx = ria.getWidth();
            int sy = ria.getHeight();
            // Z dimension is related to number of tiff pages
            int sz = decoder.getNumPages();
            ColorModel colorModel = ria.getColorModel();
            return new TileInfo(sliceAxis,
                    nChannels,
                    new int[]{sx, sy, sz},
                    colorModel.getPixelSize(),
                    colorModel.getColorSpace().isCS_sRGB());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private RenderedImageProvider readImage(Path tiffPath, int pageNumber) {
        return new RenderedImageProvider() {
            private SeekableStream tiffStream;

            @Override
            public void close() {
                try {
                    if (tiffStream != null) tiffStream.close();
                } catch (IOException e) {
                    LOG.info("Error closing tiff stream for {} page {}", tiffPath, pageNumber);
                } finally {
                    tiffStream = null;
                }
            }

            @Override
            public RenderedImage get() {
                try {
                    tiffStream = new FileSeekableStream(tiffPath.toFile());
                } catch (IOException e) {
                    LOG.error("Error opening TIFF stream for image {}, page {}", tiffPath, pageNumber, e);
                    throw new UncheckedIOException(e);
                }
                try {
                    ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
                    return decoder.decodeAsRenderedImage(pageNumber);
                } catch (IOException e) {
                    LOG.error("Error reading TIFF image {}, page {}", tiffPath, pageNumber, e);
                    close();
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    private String getFilenameForChannel(CoordinateAxis sliceAxis, int channelNumber) {
        switch (sliceAxis) {
            case X:
                return String.format(YZ_CH_TIFF_PATTERN, channelNumber);
            case Y:
                return String.format(ZX_CH_TIFF_PATTERN, channelNumber);
            case Z:
                return String.format(XY_CH_TIFF_PATTERN, channelNumber);
            default:
                throw new IllegalArgumentException("Invalid slice axis " + sliceAxis);
        }
    }

    @Override
    public Optional<RawImage> findClosestRawImageFromVoxelCoord(Path basePath, int xVoxel, int yVoxel, int zVoxel) {
        return loadVolume(basePath)
                .flatMap(rv -> {
                    Integer[] p = Arrays.stream(rv.convertToMicroscopeCoord(new int[] {xVoxel, yVoxel, zVoxel})).boxed().toArray(Integer[]::new);
                    return loadRawImageMetadataList(basePath)
                            .min((t1, t2) -> {
                                Double d1 = squaredMetricDistance(t1.getCenter(), p);
                                Double d2 = squaredMetricDistance(t2.getCenter(), p);
                                if (d1 < d2) {
                                    return -1;
                                } else if (d1 > d2) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            })
                            ;
                });
    }

    private Stream<RawImage> loadRawImageMetadataList(Path basePath) {
        Path rawImagesMetadataPath = basePath.resolve(TILED_VOL_BASE_FILE_NAME);
        ObjectMapper ymlReader = new ObjectMapper(new YAMLFactory());
        try {
            if (Files.notExists(rawImagesMetadataPath)) {
                LOG.warn("No rawimages file ({}) found in {}", rawImagesMetadataPath, basePath);
                return Stream.of();
            }
            RawVolData rawVolData = ymlReader.readValue(rawImagesMetadataPath.toFile(), RawVolData.class);
            if (CollectionUtils.isEmpty(rawVolData.getTiles())) {
                return Stream.of();
            } else {
                return rawVolData.getTiles().stream()
                        .map(t -> {
                            RawImage tn = new RawImage();
                            tn.setRenderedVolumePath(basePath.toString());
                            tn.setAcquisitionPath(rawVolData.getPath());
                            tn.setRelativePath(t.getPath());
                            if (t.getAabb() != null) {
                                tn.setOriginInMicros(t.getAabb().getOri());
                                tn.setDimsInMicros(t.getAabb().getShape());
                            }
                            tn.setTransform(t.getTransform());
                            if (t.getShape() != null) {
                                tn.setTileDims(t.getShape().getDims());
                            }
                            return tn;
                        })
                        ;
            }
        } catch (IOException e) {
            LOG.error("Error reading tiled volume metadata from {}", rawImagesMetadataPath, e);
            throw new IllegalStateException(e);
        }
    }

    private Double squaredMetricDistance(Integer[] p1, Integer[] p2) {
        return Streams.zip(Arrays.stream(p1), Arrays.stream(p2), (p1_coord, p2_coord) -> Math.pow((p1_coord.doubleValue() - p2_coord.doubleValue()), 2.))
                .reduce(0., (d1, d2) -> d1 + d2);
    }

    @Override
    public byte[] loadRawImageContentFromVoxelCoord(RawImage rawImage,
                                                    int xVoxel, int yVoxel, int zVoxel,
                                                    int dimx, int dimy, int dimz, int channel) {
        SeekableStream tiffStream;
        long rawImageFileSize;
        try {
            File rawImageFile = rawImage.getRawImagePath(String.format(RAW_CH_TIFF_PATTERN, channel)).toFile();
            if (!rawImageFile.exists()) {
                return null;
            }
            rawImageFileSize = rawImageFile.length();
            tiffStream = new FileSeekableStream(rawImageFile);
        } catch (IOException e) {
            LOG.error("Error opening raw image {}", rawImage, e);
            throw new UncheckedIOException(e);
        }
        try {
            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", tiffStream, null);
            return loadRawImageContentFromVoxelCoord(decoder,
                    xVoxel, yVoxel, zVoxel,
                    dimx, dimy, dimz,
                    (imageWidth, imageHeight, imageDepth) -> (int) Math.floor((double) rawImageFileSize / (imageWidth * imageHeight * imageDepth)));
        } catch (Exception e) {
            LOG.error("Error reading raw image {}", rawImage, e);
            throw new IllegalStateException(e);
        } finally {
            try {
                tiffStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    private byte[] loadRawImageContentFromVoxelCoord(ImageDecoder decoder,
                                                     int xVoxel, int yVoxel, int zVoxel,
                                                     int dimx, int dimy, int dimz,
                                                     TriFunction<Integer, Integer, Integer, Integer> bytesPerPixelCalculator)
            throws IOException {
        byte[] rgbBuffer;
        int numSlices = decoder.getNumPages();
        int startZ;
        int endZ;
        if (dimz > 0) {
            startZ = clamp(0, numSlices - dimz, zVoxel - dimz / 2);
            endZ = clamp(0, numSlices, startZ + dimz);
        } else {
            startZ = 0;
            endZ = numSlices;
        }
        if (startZ >= endZ) {
            return null;
        }
        int startX;
        int endX;
        int startY;
        int endY;
        int sliceSize;
        int totalVoxels;
        PixelDataHandlers<?> pixelDataHandlers;
        BufferedImage firstSliceImage = ImageUtils.renderedImageToBufferedImage(
                new NullOpImage(
                        decoder.decodeAsRenderedImage(startZ),
                        null,
                        null,
                        NullOpImage.OP_IO_BOUND));
        if (dimx != -1) {
            startX = clamp(0, firstSliceImage.getWidth() - dimx,
                    xVoxel - dimx / 2);
            endX = clamp(0, firstSliceImage.getWidth(), startX + dimx);
        } else {
            startX = 0;
            endX = firstSliceImage.getWidth();
        }
        if (dimy != -1) {
            startY = clamp(0, firstSliceImage.getHeight() - dimy,
                    yVoxel - dimy / 2);
            endY = clamp(0, firstSliceImage.getHeight(), startY + dimy);
        } else {
            startY = 0;
            endY = firstSliceImage.getHeight();
        }
        // allocate the buffer
        sliceSize = (endX - startX) * (endY - startY);
        totalVoxels = sliceSize * (endZ - startZ);
        int bytesPerPixel = bytesPerPixelCalculator.apply(firstSliceImage.getWidth(), firstSliceImage.getHeight(), numSlices);
        pixelDataHandlers = createDataHandlers(firstSliceImage.getWidth(), firstSliceImage.getHeight(), bytesPerPixel);
        rgbBuffer = new byte[totalVoxels * bytesPerPixel];
        transferPixels(firstSliceImage, startX, startY, endX, endY, rgbBuffer,
                0, pixelDataHandlers);

        for (int zSlice = startZ + 1, sliceIndex = 1; zSlice < endZ; zSlice++, sliceIndex++) {
            BufferedImage sliceImage = ImageUtils.renderedImageToBufferedImage(
                    new NullOpImage(
                            decoder.decodeAsRenderedImage(zSlice),
                            null,
                            null,
                            NullOpImage.OP_IO_BOUND));
            transferPixels(sliceImage, startX, startY, endX, endY, rgbBuffer,
                    sliceIndex * sliceSize, pixelDataHandlers);
        }
        return rgbBuffer;
    }

    private int clamp(int min, int max, int startingValue) {
        int rtnVal = startingValue;
        if ( startingValue < min ) {
            rtnVal = min;
        } else if ( startingValue > max ) {
            rtnVal = max;
        }
        return rtnVal;
    }

    /**
     * Function with three arguments.
     * @param <S> first arg type
     * @param <T> second arg type
     * @param <U> third arg type
     * @param <R> result type
     */
    @FunctionalInterface
    public interface TriFunction<S, T, U, R> {
        R apply(S s, T t, U u);
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
        int copyFrom(B sourceBuffer, int sourceOffset,
                     byte[] destBuffer, int destOffset);
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

    private PixelDataHandlers<?> createDataHandlers(int imageWidth, int imageHeight, int bytesPerPixel) {
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

    private <B> void transferPixels(
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
}
