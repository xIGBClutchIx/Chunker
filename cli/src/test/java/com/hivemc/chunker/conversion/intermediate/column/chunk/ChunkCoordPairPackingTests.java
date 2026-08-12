package com.hivemc.chunker.conversion.intermediate.column.chunk;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkCoordPairPackingTests {
    @Test
    public void testPackedCoordinatesRemainUniqueAcrossSignedValues() {
        Set<Long> coordinates = new HashSet<>();
        int[] values = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};

        for (int x : values) {
            for (int z : values) {
                coordinates.add(new ChunkCoordPair(x, z).toLong());
            }
        }

        assertEquals(values.length * values.length, coordinates.size());
    }
}
