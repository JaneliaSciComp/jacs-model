package org.janelia.rendering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import com.sun.media.jai.codec.FileSeekableStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.janelia.rendering.utils.ImageInfo;
import org.janelia.rendering.utils.ImageUtils;
import org.janelia.rendering.ymlrepr.RawVolData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
    private static final String RAW_CH_TIFF_PATTERN = "-ngc.%s.tif";

    private interface RenderedImageProvider {
        Optional<RenderedImage> get();
    }

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
                            TileInfo xyTileInfo = tileInfos[CoordinateAxis.Z.index()];
                            TileInfo zxTileInfo = tileInfos[CoordinateAxis.Y.index()];
                            TileInfo yzTileInfo = tileInfos[CoordinateAxis.X.index()];
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
        Stream<Path> channelImagePaths = renderedVolume.getTileInfo(tileKey.getSliceAxis())
                .flatMap(tileInfo -> renderedVolume.getRelativeTilePath(tileKey)
                        .map(relativeTilePath -> {
                            LOG.debug("Try to load tile {} from {} : {}", tileKey, renderedVolume.getBaseURI(), relativeTilePath);
                            return IntStream.range(0, tileInfo.getChannelCount())
                                    .mapToObj(channel -> relativeTilePath.resolve(getImageNameForChannel(tileKey.getSliceAxis(), channel)))
                                    ;
                        }))
                .orElse(Stream.of())
                ;
        Pair<ParameterBlock, RenderedImage> pbImagePair = channelImagePaths
                .map(channelImagePath -> {
                    LOG.debug("Read TIFF image {} from volume located at {}, for tile {}", channelImagePath, renderedVolume.getBaseURI(), tileKey);
                    return getRenderedImageProvider(renderedVolume.getRvl(), channelImagePath.toString(), tileKey.getSliceIndex());
                })
                .map(rimp -> rimp.get().orElse(null))
                .filter(rim -> rim != null)
                .reduce(new ImmutablePair<ParameterBlock, RenderedImage>(null, null),
                        (Pair<ParameterBlock, RenderedImage> p, RenderedImage r) -> ImageUtils.acumulateImage(p, r),
                        (Pair<ParameterBlock, RenderedImage> p1, Pair<ParameterBlock, RenderedImage> p2) -> {
                            if (p1.getRight() == null) {
                                return p2;
                            } else {
                                RenderedImage p2Image = ImageUtils.reduceImage(p2);
                                return ImageUtils.acumulateImage(p1, p2Image);
                            }
                        })
                ;
        RenderedImage imageResult = ImageUtils.reduceImage(pbImagePair);
        if (imageResult == null) {
            return Optional.empty();
        } else {
            return Optional.of(ImageUtils.renderedImageToTextureBytes(imageResult));
        }
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<TileInfo[]> loadTileInfo(RenderedVolumeLocation rvl) {
        try {
            Pattern xyTilePattern = Pattern.compile(String.format(XY_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Pattern yzTilePattern = Pattern.compile(String.format(YZ_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
            Pattern zxTilePattern = Pattern.compile(String.format(ZX_CH_TIFF_PATTERN, "(\\d+)"), Pattern.CASE_INSENSITIVE);
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
                ImageInfo xyImageInfo = ImageUtils.loadImageInfoFromTiffStream(rvl.readTileImage(channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN).get(0)));
                tileInfos[CoordinateAxis.Z.index()] = new TileInfo(
                        CoordinateAxis.Z,
                        channelTilesByOrthoProjection.get(XY_CH_TIFF_PATTERN).size(),
                        new int[] {xyImageInfo.sx, xyImageInfo.sy, xyImageInfo.sz},
                        xyImageInfo.colorModel.getPixelSize(),
                        xyImageInfo.colorModel.getColorSpace().isCS_sRGB());
            } else {
                tileInfos[CoordinateAxis.Z.index()] = null;
            }
            if (channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN) != null) {
                ImageInfo yzImageInfo = ImageUtils.loadImageInfoFromTiffStream(rvl.readTileImage(channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN).get(0)));
                tileInfos[CoordinateAxis.X.index()] = new TileInfo(
                        CoordinateAxis.X,
                        channelTilesByOrthoProjection.get(YZ_CH_TIFF_PATTERN).size(),
                        new int[] {yzImageInfo.sx, yzImageInfo.sy, yzImageInfo.sz},
                        yzImageInfo.colorModel.getPixelSize(),
                        yzImageInfo.colorModel.getColorSpace().isCS_sRGB());
            } else {
                tileInfos[CoordinateAxis.X.index()] = null;
            }
            if (channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN) != null) {
                ImageInfo zxImageInfo = ImageUtils.loadImageInfoFromTiffStream(rvl.readTileImage(channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN).get(0)));
                tileInfos[CoordinateAxis.Y.index()] = new TileInfo(
                        CoordinateAxis.Y,
                        channelTilesByOrthoProjection.get(ZX_CH_TIFF_PATTERN).size(),
                        new int[] {zxImageInfo.sx, zxImageInfo.sy, zxImageInfo.sz},
                        zxImageInfo.colorModel.getPixelSize(),
                        zxImageInfo.colorModel.getColorSpace().isCS_sRGB());
            } else {
                tileInfos[CoordinateAxis.Y.index()] = null;
            }
            return Optional.of(tileInfos);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RenderedImageProvider getRenderedImageProvider(RenderedVolumeLocation rvl, String imagePath, int pageNumber) {
        return new RenderedImageProvider() {
            @Override
            public Optional<RenderedImage> get() {
                byte[] imageBytes = rvl.readTileImagePages(imagePath, pageNumber, 1);

                if (imageBytes == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(ImageUtils.loadRenderedImageFromTiffStream(new ByteArrayInputStream(imageBytes), 0));
                }
            }
        };
    }

    private String getImageNameForChannel(CoordinateAxis sliceAxis, int channelNumber) {
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
        ObjectMapper ymlReader = new ObjectMapper(new YAMLFactory());
        try {
            InputStream tileBaseStream = rvl.readTileBaseData();
            if (tileBaseStream == null) {
                LOG.warn("No rawimages found at {}", rvl.getBaseURI());
                return Stream.of();
            }
            RawVolData rawVolData = ymlReader.readValue(tileBaseStream, RawVolData.class);
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
    public byte[] loadRawImageContentFromVoxelCoord(RawImage rawImage,
                                                    int xCenter, int yCenter, int zCenter,
                                                    int dimx, int dimy, int dimz, int channel) {
        InputStream tiffStream;
        try {
            File rawImageFile = rawImage.getRawImagePath(String.format(RAW_CH_TIFF_PATTERN, channel)).toFile();
            if (!rawImageFile.exists()) {
                return null;
            }
            tiffStream = new FileSeekableStream(rawImageFile);
        } catch (IOException e) {
            LOG.error("Error opening raw image {}", rawImage, e);
            throw new UncheckedIOException(e);
        }
        try {
            return ImageUtils.loadImagePixelBytesFromTiffStream(tiffStream, xCenter, yCenter, zCenter, dimx, dimy, dimz);
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

}
