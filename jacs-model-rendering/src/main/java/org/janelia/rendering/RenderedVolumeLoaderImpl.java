package org.janelia.rendering;

import java.io.BufferedReader;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.janelia.rendering.ymlrepr.RawVolData;
import org.janelia.rendering.ymlrepr.RawVolReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderedVolumeLoaderImpl implements RenderedVolumeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(RenderedVolumeLoaderImpl.class);

    @Override
    public Optional<RenderedVolumeMetadata> loadVolume(RenderedVolumeLocation rvl) {
        LOG.debug("Load volume from {}", rvl.getVolumeLocation());
        return loadVolumeSizeAndCoord(rvl)
                .flatMap(coord -> loadTileInfo(rvl)
                        .map(tileInfos -> {
                            int scaleFactor = (int) coord.getScaleFactor();
                            int[] tileVolumeDims = Arrays.stream(tileInfos)
                                    .filter(tileInfo -> tileInfo != null)
                                    .findFirst()
                                    .map(tileInfo -> tileInfo.getVolumeSize())
                                    .orElseGet(() -> new int[]{0, 0, 0});
                            int[] volumeSizeInVoxels = Arrays.stream(tileVolumeDims).map(tileDim -> tileDim * scaleFactor).toArray();

                            RenderedVolumeMetadata renderedVolumeMetadata = new RenderedVolumeMetadata();
                            renderedVolumeMetadata.setConnectionURI(rvl.getConnectionURI().toString());
                            renderedVolumeMetadata.setDataStorageURI(rvl.getDataStorageURI().toString());
                            renderedVolumeMetadata.setVolumeBasePath(rvl.getRenderedVolumePath());
                            renderedVolumeMetadata.setRenderingType(RenderingType.OCTREE);
                            renderedVolumeMetadata.setOriginVoxel(coord.getOriginVoxel());
                            renderedVolumeMetadata.setVolumeSizeInVoxels(volumeSizeInVoxels);
                            renderedVolumeMetadata.setMicromsPerVoxel(coord.getHighestResMicromsPerVoxel());
                            renderedVolumeMetadata.setNumZoomLevels(coord.getNumZoomLevels());
                            renderedVolumeMetadata.setXyTileInfo(tileInfos[Coordinate.Z.index()]);
                            renderedVolumeMetadata.setYzTileInfo(tileInfos[Coordinate.X.index()]);
                            renderedVolumeMetadata.setZxTileInfo(tileInfos[Coordinate.Y.index()]);

                            return renderedVolumeMetadata;
                        }));
    }

    @Override
    public Optional<byte[]> loadSlice(RenderedVolumeLocation rvl, RenderedVolumeMetadata renderedVolumeMetada, TileKey tileKey) {
        return renderedVolumeMetada.getTileInfo(tileKey.getSliceAxis())
                .flatMap(tileInfo -> renderedVolumeMetada.getRelativeTilePath(tileKey)
                        .flatMap(tileRelativePath -> {
                            List<String> chanelImageNames = IntStream.range(0, tileInfo.getChannelCount())
                                    .mapToObj(channel -> TileInfo.getImageNameForChannel(tileKey.getSliceAxis(), channel))
                                    .collect(Collectors.toList());
                            LOG.trace("Retrieve imagefiles {} for tile {} from {} and {}", chanelImageNames, tileKey, rvl.getVolumeLocation(), tileRelativePath);
                            byte[] content = rvl.readTileImagePageAsTexturedBytes(tileRelativePath, chanelImageNames, tileKey.getSliceIndex());
                            return content != null && content.length > 0 ? Optional.of(content) : Optional.empty();
                        }))
                ;
    }

    private Optional<RawCoord> loadVolumeSizeAndCoord(RenderedVolumeLocation rvl) {
        return rvl.getTransformData()
                .map(streamableTransform -> {
                    InputStream transformStream = streamableTransform.getStream();
                    try {
                        RawCoord rawCoord = parseTransformStream(transformStream);
                        return Optional.of(rawCoord);
                    } finally {
                        try {
                            transformStream.close();
                        } catch (IOException ignore) {
                            LOG.trace("Exception while trying to close transform stream for {}", rvl.getDataStorageURI(), ignore);
                        }
                    }
                }).orElseGet(() -> {
                    LOG.warn("No transform file found at", rvl.getVolumeLocation());
                    return Optional.empty();
                });
    }

    private RawCoord parseTransformStream(@Nonnull InputStream stream) {
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
            Map<Coordinate, List<String>> channelTilesByOrthoProjection = rvl.listImageUris(0).stream()
                    .map(tileUri -> {
                        String fn;
                        if (StringUtils.equalsIgnoreCase("file", tileUri.getScheme())) {
                            fn = Paths.get(tileUri).getFileName().toString();
                        } else {
                            String uriPath = tileUri.getPath();
                            int fnIndex = uriPath.lastIndexOf('/');
                            if (fnIndex == -1) {
                                fn = uriPath;
                            } else {
                                fn = uriPath.substring(fnIndex + 1);
                            }
                        }
                        if (StringUtils.isBlank(fn)) {
                            return null;
                        } else {
                            Coordinate sliceAxis = TileInfo.getSliceAxisFromImageNameForChannel(fn);
                            if (sliceAxis != null) {
                                return ImmutablePair.of(sliceAxis, fn);
                            } else {
                                return null;
                            }
                        }
                    })
                    .filter(tileImageNameWithSliceAxis -> tileImageNameWithSliceAxis != null)
                    .collect(Collectors.groupingBy(tileImageNameWithSliceAxis -> tileImageNameWithSliceAxis.getLeft(),
                            Collectors.mapping(tileImageNameWithSliceAxis -> tileImageNameWithSliceAxis.getRight(), Collectors.toList())));
            if (channelTilesByOrthoProjection.isEmpty()) {
                return Optional.empty();
            }
            TileInfo[] tileInfos = new TileInfo[3];
            for (Coordinate coord : Coordinate.values()) {
                if (channelTilesByOrthoProjection.get(coord) != null) {
                    RenderedImageInfo imageInfo = rvl.readTileImageInfo(channelTilesByOrthoProjection.get(coord).get(0));
                    tileInfos[coord.index()] = new TileInfo(
                            coord,
                            channelTilesByOrthoProjection.get(coord).size(),
                            new int[]{imageInfo.sx, imageInfo.sy, imageInfo.sz},
                            imageInfo.cmPixelSize,
                            imageInfo.sRGBspace);
                } else {
                    tileInfos[coord.index()] = null;
                }
            }
            return Optional.of(tileInfos);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Optional<RawImage> findClosestRawImageFromVoxelCoord(RenderedVolumeLocation rvl, int xVoxel, int yVoxel, int zVoxel) {
        return loadVolume(rvl)
                .flatMap(rv -> {
                    Integer[] p = Arrays.stream(convertToMicroscopeCoord(
                            new int[]{xVoxel, yVoxel, zVoxel},
                            rv.getOriginVoxel(),
                            rv.getMicromsPerVoxel())).boxed().toArray(Integer[]::new);
                    return loadVolumeRawImageTiles(rvl).stream()
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

    private Double squaredMetricDistance(Integer[] p1, Integer[] p2) {
        return Streams.zip(Arrays.stream(p1), Arrays.stream(p2), (p1_coord, p2_coord) -> Math.pow((p1_coord.doubleValue() - p2_coord.doubleValue()), 2.))
                .reduce(0., (d1, d2) -> d1 + d2);
    }

    @Override
    public Optional<byte[]> loadRawImageContentFromVoxelCoord(RenderedVolumeLocation rvl,
                                                              RawImage rawImage,
                                                              int channel, int xCenter, int yCenter, int zCenter,
                                                              int dimx, int dimy, int dimz) {
        byte[] rawImageBytes = rvl.readRawTileROIPixels(rawImage, channel, xCenter, yCenter, zCenter, dimx, dimy, dimz);
        return rawImageBytes != null && rawImageBytes.length > 0 ? Optional.of(rawImageBytes) : Optional.empty();
    }

    public List<RawImage> loadVolumeRawImageTiles(RenderedVolumeLocation rvl) {
        try {
            RawVolData rawVolData = loadRawVolumeData(rvl);
            if (rawVolData == null) {
                LOG.warn("No rawimages found at {}", rvl.getVolumeLocation());
                return Collections.emptyList();
            }
            if (rawVolData.getTiles() == null || rawVolData.getTiles().isEmpty()) {
                return Collections.emptyList();
            } else {
                return rawVolData.getTiles().stream()
                        .map(t -> {
                            RawImage tn = new RawImage();
                            tn.setRenderedVolumePath(rvl.getVolumeLocation().toString());
                            tn.setAcquisitionPath(rawVolData.getPath());
                            tn.setRelativePath(t.getPath());
                            if (t.getAabb() != null) {
                                tn.setOriginInNanos(t.getAabb().getOri());
                                tn.setDimsInNanos(t.getAabb().getShape());
                            }
                            if (t.getShape() != null) {
                                String intensityType = t.getShape().getType();
                                if (StringUtils.equalsIgnoreCase("u16", intensityType)) {
                                    tn.setBytesPerIntensity(2);
                                } else {
                                    tn.setBytesPerIntensity(1);
                                }
                                tn.setTileDims(t.getShape().getDims());
                            } else {
                                tn.setBytesPerIntensity(1);
                            }
                            tn.setTransform(t.getTransform());
                            return tn;
                        })
                        .collect(Collectors.toList())
                        ;
            }
        } catch (Exception e) {
            LOG.error("Error reading tiled volume metadata from {}", rvl.getVolumeLocation(), e);
            throw new IllegalStateException(e);
        }
    }

    private RawVolData loadRawVolumeData(RenderedVolumeLocation rvl) {
        return rvl.getTileBaseData()
                .map(streamableTileBase -> {
                    try (InputStream tileBaseStream = streamableTileBase.getStream()) {
                        return new RawVolReader().readRawVolData(tileBaseStream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } finally {
                        try {
                            streamableTileBase.close();
                        } catch (IOException ignore) {
                            // ignore this exception
                            LOG.trace("Error closing tilebase stream from  {}", rvl.getDataStorageURI(), ignore);
                        }
                    }
                })
                .orElse(null);
    }

    private int[] convertToMicroscopeCoord(int[] screenCoord, int[] origin, double[] microsPerVoxel) {
        int[] microscopeCoord = new int[3];
        for (int i = 0; i < screenCoord.length; i++) {
            microscopeCoord[i] = origin[i] + (int) (screenCoord[i] * microsPerVoxel[i]);
        }
        return microscopeCoord;
    }

}
