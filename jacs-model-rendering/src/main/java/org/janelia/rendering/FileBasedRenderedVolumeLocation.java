package org.janelia.rendering;

import org.janelia.rendering.utils.ImageUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileBasedRenderedVolumeLocation implements RenderedVolumeLocation {

    private static class OctreeImageVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> tileImages = new ArrayList<>();
        private final int detailLevel;
        private int currentLevel;

        public OctreeImageVisitor(int detailLevel) {
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
    public InputStream readTileImage(String tileRelativePath) {
        return openContentStream(volumeBasePath.resolve(tileRelativePath));
    }

    @Nullable
    @Override
    public byte[] readTileImagePages(String tileRelativePath, int startPage, int nPages) {
        InputStream tileImageStream = readTileImage(tileRelativePath);
        if (tileImageStream == null) {
            return null;
        } else
        return ImageUtils.loadRenderedImageBytesFromTiffStream(tileImageStream, startPage, nPages);
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
