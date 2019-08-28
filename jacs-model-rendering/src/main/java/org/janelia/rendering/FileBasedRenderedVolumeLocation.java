package org.janelia.rendering;

import java.io.ByteArrayInputStream;
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

public class FileBasedRenderedVolumeLocation implements RenderedVolumeLocation {

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
        return openContentStream(volumeBasePath.resolve(tileRelativePath), ImageUtils.getImagePathHandler())
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
    public Streamable<byte[]> readTileImagePageAsTexturedBytes(String tileRelativePath, List<String> channelImageNames, int pageNumber) {
        byte[] imageTextureBytes = ImageUtils.bandMergedTextureBytesFromImageStreams(
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
        return imageTextureBytes == null ? Streamable.empty() : Streamable.of(imageTextureBytes, imageTextureBytes.length);
    }

    @Override
    public Streamable<byte[]> readRawTileROIPixels(RawImage rawImage, int channel, int xCenter, int yCenter, int zCenter, int dimx, int dimy, int dimz) {
        Path rawImagePath = Paths.get(rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel)));
        return openContentStream(volumeBasePath.resolve(rawImagePath), ImageUtils.getImagePathHandler())
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
    public Streamable<InputStream> getRawTileContent(RawImage rawImage, int channel) {
        Path rawImagePath = Paths.get(rawImage.getRawImagePath(String.format(DEFAULT_RAW_CH_SUFFIX_PATTERN, channel)));
        return openContentStream(rawImagePath, ImageUtils.getImagePathHandler());
    }

    @Override
    public Streamable<InputStream> getContentFromRelativePath(String relativePath) {
        return openContentStream(volumeBasePath.resolve(relativePath), ImageUtils.getImagePathHandler());
    }

    @Override
    public Streamable<InputStream> getContentFromAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return openContentStream(Paths.get(absolutePath), ImageUtils.getImagePathHandler());
    }

    private Streamable<InputStream> openContentStream(Path fp, Function<Path, InputStream> contentStreamSupplier) {
        if (Files.exists(fp)) {
            try {
                return Streamable.of(contentStreamSupplier.apply(fp), Files.size(fp));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return Streamable.empty();
        }
    }

    @Override
    public boolean checkContentAtRelativePath(String relativePath) {
        return Files.exists(volumeBasePath.resolve(relativePath));
    }

    @Override
    public boolean checkContentAtAbsolutePath(String absolutePath) {
        Preconditions.checkArgument(StringUtils.isNotBlank(absolutePath));
        return Files.exists(Paths.get(absolutePath));
    }
}
