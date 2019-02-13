package org.janelia.rendering;

import com.sun.media.jai.codec.FileSeekableStream;
import org.janelia.rendering.utils.ImageUtils;
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
    public byte[] readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber) {
        return ImageUtils.mergeImageBands(channelImageNames.stream()
                .map(channelImageName -> volumeBasePath.resolve(tileRelativePath).resolve(channelImageName))
                .filter(channelImagePath -> Files.exists(channelImagePath))
                .map(channelImagePath -> () -> {
                    RenderedImage rim;
                    try {
                        rim = ImageUtils.loadRenderedImageFromTiffStream(new FileSeekableStream(channelImagePath.toFile()), pageNumber);
                    } catch (IOException e) {
                        LOG.error("Error reading image from {}", channelImagePath, e);
                        throw new IllegalStateException(e);
                    }
                    if (rim == null) {
                        return Optional.empty();
                    } else {
                        return Optional.of(rim);
                    }
                })
        ).orElseGet(() -> null);
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
