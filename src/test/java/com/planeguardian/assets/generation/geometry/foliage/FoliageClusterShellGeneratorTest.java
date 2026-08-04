package com.planeguardian.assets.generation.geometry.foliage;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.topology.ProtoMeshFingerprints;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoliageClusterShellGeneratorTest {
    @Test
    void producesDeterministicClosedUvMappedShell() {
        FoliageClusterShellRequest request = new FoliageClusterShellRequest(
                new Vector3(2, 3, 4), new Vector3(3, 2, 1), 4, 8, Set.of("foliage.cluster"));

        var first = FoliageClusterShellGenerator.generate(request);
        var second = FoliageClusterShellGenerator.generate(request);

        assertTrue(first.isValid());
        assertEquals(26, first.vertices().size());
        assertEquals(32, first.faces().size());
        assertEquals(ProtoMeshFingerprints.compute(first, new com.planeguardian.assets.generation.determinism.NumericQuantizer(1e-6)),
                ProtoMeshFingerprints.compute(second, new com.planeguardian.assets.generation.determinism.NumericQuantizer(1e-6)));
        assertTrue(first.loops().values().stream().allMatch(loop -> loop.attributes().textureCoordinate().isPresent()));
    }

    @Test
    void rejectsInvalidBoundsAndResolution() {
        assertThrows(IllegalArgumentException.class, () -> new FoliageClusterShellRequest(
                Vector3.ZERO, new Vector3(1, 0, 1), 2, 8, Set.of("foliage")));
        assertThrows(IllegalArgumentException.class, () -> new FoliageClusterShellRequest(
                Vector3.ZERO, Vector3.ONE, 2, 7, Set.of("foliage")));
    }
}
