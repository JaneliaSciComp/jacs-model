package org.janelia.rendering;

import com.google.common.base.Preconditions;
import com.sun.media.jai.codec.FileSeekableStream;
import org.apache.commons.lang3.StringUtils;
import org.janelia.rendering.utils.ImageUtils;
import org.janelia.rendering.utils.RenderedImagesWithStreamsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.image.RenderedImage;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBasedRenderedVolumeLocation extends AbstractRenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedRenderedVolumeLocation.class);

    private static class OctreeImageVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> tileImages = new ArrayList<>();
        private final int detailLevel;
        private int currentLevel;

        OctreeImageVisitor(int detailLevel) {
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
    public URI getBaseURI() {
        return volumeBasePath.toUri();
    }

    @Override
    public String getRenderedVolumePath() {
        return StringUtils.replace(volumeBasePath.toString(), "\\", "/");
    }

    @Override
    public List<URI> listImageUris(int level) {
        OctreeImageVisitor imageVisitor = new OctreeImageVisitor(level);
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
        InputStream tileImageStream = openContentStream(volumeBasePath.resolve(tileRelativePath));
        try {
            return ImageUtils.loadImageInfoFromTiffStream(tileImageStream);
        } finally {
            closeContentStream(tileImageStream);
        }
    }

    @Nullable
    @Override
    public InputStream streamContentFromRelativePath(String relativePath) {
        return openContentStream(volumeBasePath.resolve(relativePath));
    }

    @Nullable
    @Override
    public InputStream streamContentFromAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return openContentStream(Paths.get(absolutePath));
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

    @Override
    public byte[] readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        InputStream rawImageStream = openContentStream(rawImage.getRawImagePath(String.format(RAW_CH_TIFF_PATTERN, channel)));
        try {
            return ImageUtils.loadImagePixelBytesFromTiffStream(
                    rawImageStream,
                    xCenter, yCenter, zCenter,
                    dimx, dimy, dimz
            );
        } finally {
            closeContentStream(rawImageStream);
        }
    }

    @Nullable
    @Override
    public InputStream readTransformData() {
        return openContentStream(volumeBasePath.resolve(TRANSFORM_FILE_NAME));
    }

    @Nullable
    @Override
    public InputStream readTileBaseData() {
        return openContentStream(volumeBasePath.resolve(TILED_VOL_BASE_FILE_NAME));
    }

    @Nullable
    private InputStream openContentStream(Path fp) {
        try {
            if (Files.exists(fp)) {
                return Files.newInputStream(fp);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
