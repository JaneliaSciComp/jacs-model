package org.janelia.model.rendering;

import com.google.common.base.Splitter;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedImageAdapter;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RenderedVolumeLoaderImpl implements RenderedVolumeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RenderedVolumeLoaderImpl.class);
    private static final String TRANSFORM_FILE_NAME = "transform.txt";
    private static final String XY_CH_TIFF_PATTERN = "default.%s.tif";
    private static final String YZ_CH_TIFF_PATTERN = "YZ.%s.tif";
    private static final String ZX_CH_TIFF_PATTERN = "ZX.%s.tif";

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
                                    .reduce(Optional.<Pair<ParameterBlock, List<RenderedImageProvider>>>empty(),
                                            (ores, rimp) -> ores
                                                    .map(res -> Optional.of(
                                                            Pair.of(res.getLeft().addSource(rimp.get()),
                                                                    Stream.concat(res.getRight().stream(), Stream.of(rimp)).collect(Collectors.toList()))))
                                                    .orElseGet(() -> Optional.of(
                                                            Pair.of(new ParameterBlockJAI("bandmerge").addSource(rimp.get()),
                                                                    Arrays.asList(rimp)))),
                                            (ores1, ores2) -> ores1)
                                    .map(renderedResult -> {
                                        ParameterBlock combinedChannelsPB = renderedResult.getLeft();
                                        byte[] renderedImage = ImageUtils.renderedImageToBytes(JAI.create("bandmerge", combinedChannelsPB, null));
                                        renderedResult.getRight().forEach(rimp -> rimp.close());
                                        return renderedImage;
                                    }))
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
}
