package com.planeguardian.assets.generation.geometry.patch;

import com.planeguardian.assets.generation.api.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuadSurfacePatchGeneratorTest {
    @Test
    void generatesDeterministicQuadGridWithBoundariesAndUvs() {
        SurfacePatchRequest request = new SurfacePatchRequest(
                (u, v) -> new Vector3(4 * u, 3 * v, u * v), 4, 3, Set.of("patch"));

        SurfacePatchResult first = QuadSurfacePatchGenerator.generate(request);
        SurfacePatchResult second = QuadSurfacePatchGenerator.generate(request);

        assertEquals(20, first.mesh().vertices().size());
        assertEquals(12, first.mesh().faces().size());
        assertEquals(14, first.mesh().boundaryEdges().size());
        assertEquals(4, first.minimumU().size());
        assertEquals(5, first.minimumV().size());
        assertTrue(first.mesh().faces().values().stream().allMatch(face -> face.loops().size() == 4));
        assertTrue(first.mesh().faces().values().stream().allMatch(face -> face.semanticGroups().contains("patch")));
        assertTrue(first.mesh().loops().values().stream().allMatch(loop -> loop.attributes().textureCoordinate().isPresent()));
        assertEquals(first.mesh().vertices(), second.mesh().vertices());
        assertEquals(first.mesh().faces(), second.mesh().faces());
        assertTrue(first.mesh().isValid());
    }
}
