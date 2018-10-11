package org.janelia.model.rendering;

import com.google.common.base.Splitter;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.media.jai.RenderedImageAdapter;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RenderedVolumeLoader {

    private static final String TRANSFORM_FILE_NAME = "transform.txt";
    private static final String XY_CH_TIFF_PATTERN = "default.%s.tif";
    private static final String YZ_CH_TIFF_PATTERN = "YZ.%s.tif";
    private static final String ZX_CH_TIFF_PATTERN = "ZX.%s.tif";

    public static Optional<RenderedVolume> loadFrom(Path basePath) {
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
                                return new RenderedVolume(RenderingType.OCTREE,
                                        coord.getOriginVoxel(),
                                        volumeSizeInVoxels,
                                        coord.getHighestResMicromsPerVoxel(),
                                        coord.getNumZoomLevels(),
                                        xyTileInfo,
                                        yzTileInfo,
                                        zxTileInfo);
                            }));
    }

    private static Optional<RawCoord> loadVolumeSizeAndCoord(Path basePath) {
        try {
            PathMatcher transformFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + TRANSFORM_FILE_NAME);
            return Files.find(basePath, 1, (p, a) -> transformFileMatcher.matches(p))
                    .findFirst()
                    .map(p -> parseTransformFile(p));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static RawCoord parseTransformFile(Path p) {
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

    private static Optional<TileInfo[]> loadTileInfo(Path basePath) {
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

    private static TileInfo loadTileInfoFromTiff(Path tiffPath, int nChannels, CoordinateAxis sliceAxis) {
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
                    new int[] {sx, sy, sz},
                    colorModel.getPixelSize(),
                    colorModel.getColorSpace().isCS_sRGB());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
