package org.janelia.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.sun.media.jai.codec.FileSeekableStream;

import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedRenderedVolumeLocation extends AbstractRenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedRenderedVolumeLocation.class);

    private static class TiffOctreeImageVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> tileImages = new ArrayList<>();
        private final int detailLevel;
        private int currentLevel;

        TiffOctreeImageVisitor(int detailLevel) {
            this.detailLevel = detailLevel;
            this.currentLevel = 0;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (currentLevel == detailLevel) {
                String fn = file.getFileName().toString().toLowerCase();
                if (fn.endsWith(".tif") || fn.endsWith(".tiff")) {
                    tileImages.add(file);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            ++currentLevel;
            if (currentLevel > detailLevel) {
                return FileVisitResult.TERMINATE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }
    }

    private final Path volumeBasePath;

    public FileBasedRenderedVolumeLocation(Path volumeBasePath) {
        this.volumeBasePath = volumeBasePath;
    }

    @Override
    public URI getConnectionURI() {
        // for local file based location the connection URI and the base data storage URI are the same.
        return volumeBasePath.toUri();
    }

    @Override
    public URI getDataStorageURI() {
        return volumeBasePath.toUri();
    }

    @Override
    public String getRenderedVolumePath() {
        return StringUtils.replace(volumeBasePath.toString(), "\\", "/");
    }

    @Override
    public List<URI> listImageUris(int level) {
        TiffOctreeImageVisitor imageVisitor = new TiffOctreeImageVisitor(level);
        try {
            Files.walkFileTree(volumeBasePath, imageVisitor);
            return imageVisitor.tileImages.stream().map(p -> p.toUri()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nullable
    @Override
    public RenderedImageInfo readTileImageInfo(String tileRelativePath) {
        return openContentStream(volumeBasePath.resolve(tileRelativePath), ImageUtils.getImagePathHandler())
                .map(streamableTiffImage -> {
                    try {
                        return ImageUtils.loadImageInfoFromTiffStream(streamableTiffImage.getStream());
                    } finally {
                        closeContentStream(streamableTiffImage);
                    }
                })
                .orElse(null);
    }

    @Nullable
    @Override
    public byte[] readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber) {
        return ImageUtils.bandMergedTextureBytesFromImageStreams(
                channelImageNames.stream()
                        .map(channelImageName -> volumeBasePath.resolve(tileRelativePath).resolve(channelImageName))
                        .filter(channelImagePath -> Files.exists(channelImagePath))
                        .map(channelImagePath -> NamedSupplier.namedSupplier(
                                channelImagePath.toString(),
                                () -> {
                                    try {
                                        return new FileSeekableStream(channelImagePath.toFile());
                                    } catch (IOException e) {
                                        LOG.error("Error opening image {} for reading {}", channelImagePath, pageNumber, e);
                                        throw new IllegalStateException(e);
                                    }

                                })
                        ),
                pageNumber
        );
    }

    @Nullable
    @Override
    public byte[] readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        Path rawImagePath = Paths.get(rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel)));
        return openContentStream(volumeBasePath.resolve(rawImagePath), ImageUtils.getImagePathHandler())
                .map(streamableRawImage -> {
                    try {
                        return ImageUtils.loadImagePixelBytesFromTiffStream(streamableRawImage.getStream(), xCenter, yCenter, zCenter, dimx, dimy, dimz);
                    } finally {
                        closeContentStream(streamableRawImage);
                    }
                })
                .orElse(null);
    }

    @Nullable
    @Override
    public StreamableContent readRawTileContent(RawImage rawImage, int channel) {
        Path rawImagePath = Paths.get(rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel)));
        return openContentStream(rawImagePath, ImageUtils.getImagePathHandler()).orElse(null);
    }

    @Nullable
    @Override
    public StreamableContent streamContentFromRelativePath(String relativePath) {
        return openContentStream(volumeBasePath.resolve(relativePath), ImageUtils.getImagePathHandler()).orElse(null);
    }

    @Nullable
    @Override
    public StreamableContent streamContentFromAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return openContentStream(Paths.get(absolutePath), ImageUtils.getImagePathHandler()).orElse(null);
    }

    private Optional<StreamableContent> openContentStream(Path fp, Function<Path, InputStream> contentStreamSupplier) {
        if (Files.exists(fp)) {
            try {
                return Optional.of(new StreamableContent(Files.size(fp), contentStreamSupplier.apply(fp)));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return Optional.empty();
        }
    }

}
