package com.hivemc.chunker.conversion.intermediate.level.map;

import com.hivemc.chunker.conversion.intermediate.world.Dimension;
import com.hivemc.chunker.nbt.tags.collection.CompoundTag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChunkerMapTests {
    @Test
    public void testLazyPayloadIsLoadedOnceAndCanBeReleased() {
        AtomicInteger loads = new AtomicInteger();
        byte[] bytes = {1, 2, 3, 4};
        CompoundTag originalNBT = new CompoundTag();
        ChunkerMap map = new ChunkerMap(
                12,
                34,
                128,
                128,
                (byte) 2,
                Dimension.OVERWORLD,
                100,
                -200,
                true,
                false,
                () -> {
                    loads.incrementAndGet();
                    return new ChunkerMap.Payload(bytes, originalNBT);
                }
        );

        assertEquals(34, map.getId());
        assertEquals(0, loads.get());

        assertArrayEquals(bytes, map.getBytes());
        assertSame(originalNBT, map.getOriginalNBT());
        assertEquals(1, loads.get());

        map.releasePayload();
        assertNull(map.getBytes());
        assertNull(map.getOriginalNBT());
        assertEquals(1, loads.get());
    }

    @Test
    public void testImageOverrideReplacesLazyPixelsAndPreservesOriginalNbt(@TempDir Path tempDirectory) throws IOException {
        AtomicInteger loads = new AtomicInteger();
        CompoundTag originalNBT = new CompoundTag();
        ChunkerMap map = new ChunkerMap(
                12,
                34,
                1,
                1,
                (byte) 0,
                Dimension.OVERWORLD,
                0,
                0,
                false,
                false,
                () -> {
                    loads.incrementAndGet();
                    return new ChunkerMap.Payload(new byte[]{1, 2, 3, 4}, originalNBT);
                }
        );

        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFF0A141E);
        Path imagePath = tempDirectory.resolve("map.png");
        ImageIO.write(image, "png", imagePath.toFile());

        map.loadImage(imagePath.toFile());

        assertArrayEquals(new byte[]{10, 20, 30, (byte) 255}, map.getBytes());
        assertSame(originalNBT, map.getOriginalNBT());
        assertEquals(1, loads.get());
    }

    @Test
    public void testLazyPayloadIsPublishedOnceToConcurrentReaders() throws Exception {
        int readerCount = 8;
        AtomicInteger loads = new AtomicInteger();
        byte[] expected = {5, 6, 7, 8};
        ChunkerMap map = new ChunkerMap(
                12,
                34,
                1,
                1,
                (byte) 0,
                Dimension.OVERWORLD,
                0,
                0,
                false,
                false,
                () -> {
                    loads.incrementAndGet();
                    return new ChunkerMap.Payload(expected, null);
                }
        );
        CountDownLatch readersReady = new CountDownLatch(readerCount);
        CountDownLatch startReaders = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(readerCount);

        try {
            List<Future<byte[]>> results = new ArrayList<>(readerCount);
            for (int i = 0; i < readerCount; i++) {
                results.add(executor.submit(() -> {
                    readersReady.countDown();
                    startReaders.await();
                    return map.getBytes();
                }));
            }

            assertTrue(readersReady.await(10, TimeUnit.SECONDS));
            startReaders.countDown();
            for (Future<byte[]> result : results) {
                assertArrayEquals(expected, result.get(10, TimeUnit.SECONDS));
            }
            assertEquals(1, loads.get());
        } finally {
            startReaders.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
