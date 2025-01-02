package org.janelia.testutils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtils {
    /**
     * Deletes the given directory even if it's non empty.
     *
     * @param dir
     * @throws IOException
     */
    public static void deletePath(Path dir) throws IOException {
        if (dir == null) {
            return; // do nothing
        }
        Files.walkFileTree(dir, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void prepareTestDataFiles(Path sourcePath, Path destPath, String... testDataFileNames) {
        prepareTestDataFiles(sourcePath, destPath, Stream.of(testDataFileNames).collect(Collectors.toMap(fn -> fn, fn -> fn)));
    }

    public static void prepareTestDataFiles(Path sourcePath, Path destPath, Map<String, String> testDataFileMapping) {
        try {
            for (String testFileName : testDataFileMapping.keySet()) {
                // copy file and rename it.
                Path dest = destPath.resolve(testDataFileMapping.get(testFileName));
                if (Files.notExists(dest.getParent())) {
                    Files.createDirectories(dest.getParent());
                }
                Files.copy(sourcePath.resolve(testFileName), dest);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
