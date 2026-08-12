package com.hivemc.chunker.conversion.intermediate.column.chunk;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkCoordPairTests {
    @Test
    public void testAllRegionPositionsHaveUnique10BitIndexes() {
        Set<Integer> indexes = new HashSet<>();

        for (int localX = 0; localX < 32; localX++) {
            for (int localZ = 0; localZ < 32; localZ++) {
                indexes.add(new ChunkCoordPair(localX, localZ).to10BitIndex());
            }
        }

        assertEquals(1024, indexes.size());
        assertEquals(0, indexes.stream().mapToInt(Integer::intValue).min().orElseThrow());
        assertEquals(1023, indexes.stream().mapToInt(Integer::intValue).max().orElseThrow());
    }
}
