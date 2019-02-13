package org.janelia.rendering;

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.janelia.rendering.utils.ImageUtils;
import org.janelia.rendering.utils.RenderedImageSupplier;
import org.janelia.rendering.ymlrepr.RawVolData;
import org.janelia.rendering.ymlrepr.RawVolReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RenderedVolumeLoaderImpl implements RenderedVolumeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RenderedVolumeLoaderImpl.class);
    private static final String XY_CH_TIFF_PATTERN = "default.%s.tif";
    private static final String YZ_CH_TIFF_PATTERN = "YZ.%s.tif";
    private static final String ZX_CH_TIFF_PATTERN = "ZX.%s.tif";

    @Override
    public Optional<RenderedVolume> loadVolume(RenderedVolumeLocation rvl) {
        LOG.debug("Load volume from {}", rvl.getBaseURI());
        return loadVolumeSizeAndCoord(rvl)
                .flatMap(coord -> loadTileInfo(rvl)
                        .map(tileInfos -> {
                            int scaleFactor = (int) coord.getScaleFactor();
                            int[] tileVolumeDims = Arrays.stream(tileInfos)
                                    .filter(tileInfo -> tileInfo != null)
                                    .findFirst()
                                    .map(tileInfo -> tileInfo.getVolumeSize())
                                    .orElseGet(() -> new int[] {0, 0, 0});
                            int[] volumeSizeInVoxels = Arrays.stream(tileVolumeDims).map(tileDim -> tileDim * scaleFactor).toArray();
                            TileInfo xyTileInfo = tileInfos[Coordinate.Z.index()];
                            TileInfo zxTileInfo = tileInfos[Coordinate.Y.index()];
                            TileInfo yzTileInfo = tileInfos[Coordinate.X.index()];
                            return new RenderedVolume(rvl,
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
                        .map(tileRelativePath -> {
                            List<String> chanelImageNames = IntStream.range(0, tileInfo.getChannelCount())
                                    .mapToObj(channel -> tileRelativePath.resolve(getImageNameForChannel(tileKey.getSliceAxis(), channel)))
                                    .map(channelImageRelativePath -> channelImageRelativePath.toString())
                                    .collect(Collectors.toList());
                            return renderedVolume.getRvl().readTileImagePageAsTexturedBytes(tileRelativePath.toString(), chanelImageNames, tileKey.getSliceIndex());
                        }))
                ;
    }

    private Optional<RawCoord> loadVolumeSizeAndCoord(RenderedVolumeLocation rvl) {
        try {
            InputStream transformStream = rvl.readTransformData();
            if (transformStream == null) {
                LOG.warn("No transform file found at", rvl.getBaseURI());
                return Optional.empty();
            } else {
                RawCoord rawCoord = parseTransformStream(transformStream);
                return Optional.of(rawCoord);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RawCoord parseTransformStream(InputStream stream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            RawCoord coord = new RawCoord();
            String l;
            Splitter nameValueSplitter = Splitter.on(':').trimResults().omitEmptyStrings().limit(2);
            while ((l = br.readLine()) != null) {
                if (l.startsWith("#")) { // comment line
                    continue;
                }
                List<String> nameAndValue = nameValueSplitter.splitToList(l.toLowerCase());
                if (nameAndValue.size() < 2) {
                    continue;
                }
                switch (nameAndValue.get(0)) {
                    case "ox":
                        coord.setOriginInNanos(Coordinate.X, Integer.valueOf(nameAndValue.get(1)));
                        break;
                    case "oy":
                        coord.setOriginInNanos(Coordinate.Y, Integer.valueOf(nameAndValue.get(1)));
                        break;
                    case "oz":
                        coord.setOriginInNanos(Coordinate.Z, Integer.valueOf(nameAndValue.get(1)));
                        break;
                    case "sx":
                        coord.setLowestResNanosPerVoxel(Coordinate.X, Double.valueOf(nameAndValue.get(1)));
                        break;
                    case "sy":
                        coord.setLowestResNanosPerVoxel(Coordinate.Y, Double.valueOf(nameAndValue.get(1)));
                        break;
                    case "sz":
                        coord.setLowestResNanosPerVoxel(Coordinate.Z, Double.valueOf(nameAndValue.get(1)));
                        break;
                    case "nl":
                        coord.setNumZoomLevels(Integer.valueOf(nameAndValue.get(1)));
                        break;
                    default:
                        break;
                }
            }
            return coord;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<TileInfo[]> loadTileInfo(RenderedVolumeLocation rvl) {
        try {
            Pattern xyTilePattern = Pattern.compile("^" + String.format(XY_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Pattern yzTilePattern = Pattern.compile("^" + String.format(YZ_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Pattern zxTilePattern = Pattern.compile("^" + String.format(ZX_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Map<String, List<String>> channelTilesByOrthoProjection = rvl.listImageUris(0).stream()
                    .map(tileUri -> {
                        String uriPath = tileUri.getPath();
                        if (StringUtils.isBlank(uriPath)) {
                            return null;
                        } else {
                            String fn = Paths.get(uriPath).getFileName().toString();
                            if (xyTilePattern.asPredicate().test(fn)) {
                                return ImmutablePair.of(XY_CH_TIFF_PATTERN, fn);
                            } else if (yzTilePattern.asPredicate().test(fn)) {
                                return ImmutablePair.of(YZ_CH_TIFF_PATTERN, fn);
                            } else if (zxTilePattern.asPredicate().test(fn)) {
                                return ImmutablePair.of(ZX_CH_TIFF_PATTERN, fn);
                            } else {
                                return null;
                            }
                        }
                    })
                    .filter(tileImageNameWithChannel -> tileImageNameWithChannel != null)
                    .collect(Collectors.groupingBy(tileImageNameWithChannel -> tileImageNameWithChannel.getLeft(),
                            Collectors.mapping(tileImageNameWithChannel -> tileImageNameWithChannel.getRight(), Collectors.toList())));
            if (channelTilesByOrthoProjection.isEmpty()) {
                return Optional.empty();
            }
            TileInfo[] tileInfos = new TileInfo[3];
            if (channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN) != null) {
                RenderedImageInfo xyImageInfo = rvl.readTileImageInfo(channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN).get(0));
                tileInfos[Coordinate.Z.index()] = new TileInfo(
                        Coordinate.Z,
                        channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN).size(),
                        new int[] {xyImageInfo.sx, xyImageInfo.sy, xyImageInfo.sz},
                        xyImageInfo.cmPixelSize,
                        xyImageInfo.sRGBspace);
            } else {
                tileInfos[Coordinate.Z.index()] = null;
            }
            if (channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN) != null) {
                RenderedImageInfo yzImageInfo = rvl.readTileImageInfo(channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN).get(0));
                tileInfos[Coordinate.X.index()] = new TileInfo(
                        Coordinate.X,
                        channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN).size(),
                        new int[] {yzImageInfo.sx, yzImageInfo.sy, yzImageInfo.sz},
                        yzImageInfo.cmPixelSize,
                        yzImageInfo.sRGBspace);
            } else {
                tileInfos[Coordinate.X.index()] = null;
            }
            if (channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN) != null) {
                RenderedImageInfo zxImageInfo = rvl.readTileImageInfo(channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN).get(0));
                tileInfos[Coordinate.Y.index()] = new TileInfo(
                        Coordinate.Y,
                        channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN).size(),
                        new int[] {zxImageInfo.sx, zxImageInfo.sy, zxImageInfo.sz},
                        zxImageInfo.cmPixelSize,
                        zxImageInfo.sRGBspace);
            } else {
                tileInfos[Coordinate.Y.index()] = null;
            }
            return Optional.of(tileInfos);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getImageNameForChannel(Coordinate sliceAxis, int channelNumber) {
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
    public Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel) {
        return loadVolume(rvl)
                .flatMap(rv -> {
                    Integer[] p = Arrays.stream(rv.convertToMicroscopeCoord(new int[] {xVoxel, yVoxel, zVoxel})).boxed().toArray(Integer[]::new);
                    return loadRawImageMetadataList(rvl)
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

    private Stream<RawImage> loadRawImageMetadataList(RenderedVolumeLocation rvl) {
        try {
            InputStream tileBaseStream = rvl.readTileBaseData();
            RawVolData rawVolData = new RawVolReader().readRawVolData(tileBaseStream);
            if (rawVolData == null) {
                LOG.warn("No rawimages found at {}", rvl.getBaseURI());
                return Stream.of();
            }
            if (CollectionUtils.isEmpty(rawVolData.getTiles())) {
                return Stream.of();
            } else {
                return rawVolData.getTiles().stream()
                        .map(t -> {
                            RawImage tn = new RawImage();
                            tn.setRenderedVolumePath(rvl.getBaseURI().toString());
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
            LOG.error("Error reading tiled volume metadata from {}", rvl.getBaseURI(), e);
            throw new IllegalStateException(e);
        }
    }

    private Double squaredMetricDistance(Integer[] p1, Integer[] p2) {
        return Streams.zip(Arrays.stream(p1), Arrays.stream(p2), (p1_coord, p2_coord) -> Math.pow((p1_coord.doubleValue() - p2_coord.doubleValue()), 2.))
                .reduce(0., (d1, d2) -> d1 + d2);
    }

    @Override
    public byte[] loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                                    RawImage rawImage,
                                                    int channel, int xCenter, int yCenter, int zCenter,
                                                    int dimx, int dimy, int dimz) {
        return rvl.readRawTileROIPixels(rawImage, channel, xCenter, yCenter, zCenter, dimx, dimy, dimz);
    }

}
