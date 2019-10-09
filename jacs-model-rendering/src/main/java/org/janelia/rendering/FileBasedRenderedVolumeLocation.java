package org.janelia.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.sun.media.jai.codec.FileSeekableStream;

import org.janelia.rendering.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedRenderedVolumeLocation extends FileBasedDataLocation implements RenderedVolumeLocation {

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedRenderedVolumeLocation.class);

    private static class TiffOctreeImageVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> tileImages = new ArrayList<>();
        private final Path startPath;
        private final int detailLevel;
        private int currentLevel;

        TiffOctreeImageVisitor(Path startPath, int detailLevel) {
            this.startPath = startPath;
            this.detailLevel = detailLevel;
            this.currentLevel = 0;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (currentLevel <= detailLevel) {
                String fn = file.getFileName().toString().toLowerCase();
                if (fn.endsWith(".tif") || fn.endsWith(".tiff")) {
                    tileImages.add(file);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (startPath.equals(dir)) {
                currentLevel = 0;
            } else {
                currentLevel = startPath.relativize(dir).getNameCount();
            }
            if (currentLevel > detailLevel) {
                return FileVisitResult.TERMINATE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }

    }

    public FileBasedRenderedVolumeLocation(Path volumeBasePath, Function<Path, Path> pathMapper) {
        super(volumeBasePath, pathMapper);
    }

    public FileBasedRenderedVolumeLocation(FileBasedDataLocation fileBasedDataLocation) {
        super(fileBasedDataLocation);
    }

    @Override
    public List<URI> listImageUris(int level) {
        Path volumeBasePath = Paths.get(getBaseDataStoragePath());
        TiffOctreeImageVisitor imageVisitor = new TiffOctreeImageVisitor(volumeBasePath, level);
        try {
            Files.walkFileTree(volumeBasePath, EnumSet.noneOf(FileVisitOption.class), level + 1, imageVisitor);
            return imageVisitor.tileImages.stream().map(Path::toUri).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nullable
    @Override
    public RenderedImageInfo readTileImageInfo(String tileRelativePath) {
        return getContentFromRelativePath(tileRelativePath)
                .consume(tiffStream -> {
                    try {
                        return ImageUtils.loadImageInfoFromTiffStream(tiffStream);
                    } finally {
                        try {
                            tiffStream.close();
                        } catch (Exception ignore) {
                        }
                    }
                }, (i, l) -> l)
                .getContent();
    }

    @Override
    public Streamable<byte[]> readTiffPageAsTexturedBytes(String imageRelativePath, List<String> channelImageNames, int pageNumber) {
        byte[] imageTextureBytes = ImageUtils.bandMergedTextureBytesFromImageStreams(
                channelImageNames.stream()
                        .map(channelImageName -> Paths.get(getBaseDataStoragePath(), imageRelativePath, channelImageName))
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
        return imageTextureBytes == null ? Streamable.empty() : Streamable.of(imageTextureBytes, imageTextureBytes.length);
    }

    @Override
    public Streamable<byte[]> readTiffImageROIPixels(String imagePath, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        return getContentFromAbsolutePath(imagePath)
                .consume(imageStream -> {
                    try {
                        return ImageUtils.loadImagePixelBytesFromTiffStream(imageStream, xCenter, yCenter, zCenter, dimx, dimy, dimz);
                    } finally {
                        try {
                            imageStream.close();
                        } catch (Exception ignore) {
                        }
                    }
                }, (bytes, l) -> (long) bytes.length);
    }

    @Override
    Function<Path, InputStream> defaultPathHandler() {
        return ImageUtils.getImagePathHandler();
    }
}
