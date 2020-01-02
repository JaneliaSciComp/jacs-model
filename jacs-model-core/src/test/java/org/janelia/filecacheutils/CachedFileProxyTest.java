package org.janelia.filecacheutils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.io.ByteStreams;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;

public class CachedFileProxyTest {

    private static Path testCacheRootDir;
    private static Random random = new Random(System.currentTimeMillis());

    private static class TestFileKey implements FileKey {
        private final Path keyPath;

        TestFileKey(Path keyPath) {
            this.keyPath = keyPath;
        }

        @Override
        public Path getLocalPath(LocalFileCacheStorage localFileCacheStorage) {
            return keyPath;
        }
    }

    @BeforeClass
    public static void createTestDir() throws IOException {
        testCacheRootDir = Files.createTempDirectory("testcache");
    }

    @AfterClass
    public static void deleteTestDir() throws IOException {
        Files.walkFileTree(testCacheRootDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private Path testFilePath;

    @Before
    public void setUp() {
        testFilePath = testCacheRootDir.resolve("readAndCacheContent" + random.nextLong() + ".tst");
    }

    @After
    public void cleanUp() {
        try {
            Files.deleteIfExists(testFilePath);
        } catch (IOException ignore) {
        }
    }

    @Test
    public void readAndCacheContent() throws IOException {
        byte[] testContent = new byte[100];
        random.nextBytes(testContent);

        TestFileKey testFileKey = new TestFileKey(testFilePath);
        FileKeyToProxyMapper<TestFileKey> testFileProxyMapper = prepareFileProxy(testContent);

        LocalFileCacheStorage testLocalFileCacheStorage = Mockito.mock(LocalFileCacheStorage.class);
        Mockito.when(testLocalFileCacheStorage.isBytesSizeAcceptable(anyLong())).thenReturn(true);

        ExecutorService testExecutorService = Executors.newWorkStealingPool();

        CachedFileProxy<TestFileKey> cachedFileProxy = new CachedFileProxy<>(testFileKey, testFileProxyMapper, testLocalFileCacheStorage, testExecutorService);
        InputStream contentStream = cachedFileProxy.openContentStream();
        assertFalse(Files.exists(testFilePath));
        byte[] readBuffer = new byte[32];
        int offset;
        for (offset = 0;;) {
            int n = verifyRead(contentStream, testContent, offset, readBuffer);
            if (n == -1) {
                break;
            }
            offset += n;
        }
        assertEquals(testContent.length, offset);
        contentStream.close();
        try {
            testExecutorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Cache executor interrupted: " + e.getMessage());
        }
        testExecutorService.shutdown();
        assertTrue(Files.exists(testFilePath));
        assertEquals(testContent.length, Files.size(testFilePath));

        byte[] cachedFileContent = Files.readAllBytes(testFilePath);
        assertArrayEquals(testContent, cachedFileContent);
    }

    @Test
    public void readContentWithoutCachingBecauseCacheStorageIsDisable() throws IOException {
        byte[] testContent = new byte[100];
        random.nextBytes(testContent);

        TestFileKey testFileKey = new TestFileKey(testFilePath);
        FileKeyToProxyMapper<TestFileKey> testFileProxyMapper = prepareFileProxy(testContent);

        LocalFileCacheStorage testLocalFileCacheStorage = Mockito.mock(LocalFileCacheStorage.class);
        Mockito.when(testLocalFileCacheStorage.isBytesSizeAcceptable(anyLong())).thenReturn(false);

        ExecutorService testExecutorService = null;
        CachedFileProxy<TestFileKey> cachedFileProxy = new CachedFileProxy<>(testFileKey, testFileProxyMapper, testLocalFileCacheStorage, testExecutorService);
        InputStream contentStream = cachedFileProxy.openContentStream();
        assertFalse(Files.exists(testFilePath));
        byte[] readBuffer = new byte[32];
        int offset;
        for (offset = 0;;) {
            int n = verifyRead(contentStream, testContent, offset, readBuffer);
            if (n == -1) {
                break;
            }
            offset += n;
        }
        assertEquals(testContent.length, offset);
        contentStream.close();
        assertFalse(Files.exists(testFilePath));
    }

    @Test
    public void twoReadsButOnlyOneCache() throws IOException {
        byte[] testContent = new byte[100];
        random.nextBytes(testContent);

        LocalFileCacheStorage testLocalFileCacheStorage = new LocalFileCacheStorage(testCacheRootDir, 100, 50, 100);

        TestFileKey testFileKey = new TestFileKey(testFilePath);

        ExecutorService testExecutorService = Executors.newWorkStealingPool(2);
        CachedFileProxy<TestFileKey> cachedFileProxy1 = new CachedFileProxy<>(testFileKey, prepareFileProxy(testContent), testLocalFileCacheStorage, testExecutorService);
        CachedFileProxy<TestFileKey> cachedFileProxy2 = new CachedFileProxy<>(testFileKey, prepareFileProxy(testContent), testLocalFileCacheStorage, testExecutorService);
        InputStream contentStream1 = cachedFileProxy1.openContentStream();
        InputStream contentStream2 = cachedFileProxy2.openContentStream();
        assertFalse(Files.exists(testFilePath));
        byte[] readBuffer = new byte[32];
        int offset;
        for (offset = 0;;) {
            int n1 = verifyRead(contentStream1, testContent, offset, readBuffer);
            int n2 = verifyRead(contentStream2, testContent, offset, readBuffer);
            assertTrue(n1 == n2);
            if (n1 == -1) {
                break;
            }
            offset += n1;
        }
        assertEquals(testContent.length, offset);
        contentStream1.close();
        contentStream2.close();
        try {
            testExecutorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Cache executor interrupted: " + e.getMessage());
        }
        testExecutorService.shutdown();
        assertTrue(Files.exists(testFilePath));
        assertEquals(testContent.length, Files.size(testFilePath));

        byte[] cachedFileContent = Files.readAllBytes(testFilePath);
        assertArrayEquals(testContent, cachedFileContent);
    }

    @Test
    public void readCachedFile() throws IOException {
        byte[] testContent = new byte[100];
        random.nextBytes(testContent);

        LocalFileCacheStorage testLocalFileCacheStorage = new LocalFileCacheStorage(testCacheRootDir, 100, 50, 100);

        cacheContent(testContent, testLocalFileCacheStorage);
        assertTrue(Files.exists(testFilePath));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
        }
        FileTime cachedFileTouchTime = Files.getLastModifiedTime(testFilePath);

        TestFileKey testFileKey = new TestFileKey(testFilePath);

        CachedFileProxy<TestFileKey> cachedFileProxy = new CachedFileProxy<>(testFileKey, prepareFileProxy(testContent), testLocalFileCacheStorage, null);
        InputStream contentStream = cachedFileProxy.openContentStream();
        byte[] readContent = ByteStreams.toByteArray(contentStream);
        contentStream.close();
        assertArrayEquals(testContent, readContent);
        assertTrue(cachedFileTouchTime.to(TimeUnit.MILLISECONDS) < Files.getLastModifiedTime(testFilePath).to(TimeUnit.MILLISECONDS));
    }

    private void cacheContent(byte[] content, LocalFileCacheStorage localFileCacheStorage) throws IOException {
        ExecutorService testExecutorService = null;
        TestFileKey testFileKey = new TestFileKey(testFilePath);
        CachedFileProxy<TestFileKey> cachedFileProxy = new CachedFileProxy<>(testFileKey, prepareFileProxy(content), localFileCacheStorage, testExecutorService);
        InputStream contentStream = cachedFileProxy.openContentStream();
        assertFalse(Files.exists(testFilePath));
        ByteStreams.toByteArray(contentStream);
        contentStream.close();
    }

    private int verifyRead(InputStream is, byte[] originalContent, int offset, byte[] readBuffer) throws IOException {
        int n = is.read(readBuffer);
        if (n == -1) {
            return n;
        }
        assertArrayEquals(Arrays.copyOfRange(originalContent, offset, offset + n), Arrays.copyOfRange(readBuffer, 0, n));
        return n;
    }

    private FileKeyToProxyMapper<TestFileKey> prepareFileProxy(byte[] testContent) throws FileNotFoundException {
        FileProxy fileProxy = Mockito.mock(FileProxy.class);
        Mockito.when(fileProxy.openContentStream()).thenReturn(new ByteArrayInputStream(testContent));
        Mockito.when(fileProxy.estimateSizeInBytes()).thenReturn((long) testContent.length);
        return (fileKey) -> fileProxy;
    }
}
