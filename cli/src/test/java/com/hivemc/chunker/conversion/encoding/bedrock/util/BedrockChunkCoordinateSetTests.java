package com.hivemc.chunker.conversion.encoding.bedrock.util;

import com.hivemc.chunker.conversion.intermediate.column.chunk.ChunkCoordPair;
import com.hivemc.chunker.conversion.intermediate.column.chunk.RegionCoordPair;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BedrockChunkCoordinateSetTests {
    @Test
    public void testAllCoordinatesInNegativeRegion() {
        RegionCoordPair region = new RegionCoordPair(-3, 7);
        BedrockChunkCoordinateSet chunks = new BedrockChunkCoordinateSet(region);
        Set<ChunkCoordPair> expected = new HashSet<>();

        for (int localX = 0; localX < 32; localX++) {
            for (int localZ = 0; localZ < 32; localZ++) {
                ChunkCoordPair position = region.getChunk(localX, localZ);
                assertTrue(chunks.add(position.chunkX(), position.chunkZ()));
                assertTrue(expected.add(position));
            }
        }

        assertEquals(1024, chunks.size());
        assertEquals(expected, new HashSet<>(chunks));
        assertTrue(chunks.contains(region.getChunk(31, 31)));
        assertFalse(chunks.add(region.getChunk(0, 0)));
        ChunkCoordPair outside = new ChunkCoordPair((region.regionX() + 1) << 5, region.regionZ() << 5);
        assertThrowsExactly(IllegalArgumentException.class, () -> chunks.add(outside));
    }
}
