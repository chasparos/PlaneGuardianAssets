package com.planeguardian.assets.generation.geometry.tube;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.curves.CubicHermiteCurve;
import com.planeguardian.assets.generation.math.VectorMath;
import com.planeguardian.assets.generation.topology.ProtoFace;
import com.planeguardian.assets.generation.topology.ProtoLoop;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplineTubeGeneratorTest {
    @Test
    void openTubeProducesOnlyQuadSidesAndTwoBoundaryRings() {
        SplineTubeResult result = SplineTubeGenerator.generate(request(
                3, 8, fraction -> 1, (arc, angle) -> 1, fraction -> 0));

        assertEquals(24, result.mesh().vertices().size());
        assertEquals(16, result.mesh().faces().size());
        assertEquals(40, result.mesh().edges().size());
        assertEquals(16, result.mesh().boundaryEdges().size());
        assertTrue(result.mesh().faces().values().stream().allMatch(face -> face.loops().size() == 4));
        assertTrue(result.mesh().isValid());
        assertEquals(Set.of("tube.test"), result.mesh().faces().firstEntry().getValue().semanticGroups());
    }

    @Test
    void radiusCrossSectionAndRollProfilesRemainIndependent() {
        SplineTubeResult result = SplineTubeGenerator.generate(request(
                3, 8, fraction -> 1 + fraction,
                (arc, angle) -> angle == 0 ? 1.5 : 1,
                fraction -> fraction * StrictMath.PI / 2));

        Vector3 startVertex = result.mesh().vertices().get(result.rings().get(0).get(0)).position();
        Vector3 endVertex = result.mesh().vertices().get(result.rings().get(2).get(0)).position();
        assertVector(startVertex, 1.5, 0, 0);
        assertVector(endVertex, 0, 3, 4);
        assertEquals(1, result.start().nominalRadius());
        assertEquals(2, result.end().nominalRadius());
        assertEquals(StrictMath.PI / 2, result.end().frame().rollRadians(), 1.0e-12);
    }

    @Test
    void circumferentialUvSeamUsesZeroAndOneOnSharedGeometry() {
        SplineTubeResult result = SplineTubeGenerator.generate(request(
                2, 8, fraction -> 1, CrossSectionProfile.circular(), fraction -> 0));
        ProtoFace seamFace = result.mesh().faces().lastEntry().getValue();
        List<ProtoLoop> loops = seamFace.loops().stream().map(result.mesh().loops()::get).toList();

        assertEquals(0.875, loops.get(0).attributes().textureCoordinate().orElseThrow().x());
        assertEquals(1.0, loops.get(1).attributes().textureCoordinate().orElseThrow().x());
        assertEquals(result.rings().get(0).get(0), loops.get(1).vertexId());
        assertEquals(1.0, loops.get(2).attributes().scalarLayers().get("tube.angle_fraction"));
    }

    @Test
    void endpointMetadataAndRingCollectionsAreImmutable() {
        SplineTubeResult result = SplineTubeGenerator.generate(request(
                2, 8, fraction -> 1, CrossSectionProfile.circular(), fraction -> 0));
        assertEquals(result.rings().get(0), result.start().ringVertices());
        assertEquals(result.rings().get(1), result.end().ringVertices());
        assertThrows(UnsupportedOperationException.class, () -> result.rings().get(0).clear());
        assertThrows(UnsupportedOperationException.class, () -> result.start().ringVertices().clear());
    }

    @Test
    void profilesAndMinimumRingResolutionAreValidated() {
        assertThrows(IllegalArgumentException.class, () -> request(
                2, 7, fraction -> 1, CrossSectionProfile.circular(), fraction -> 0));
        assertThrows(IllegalArgumentException.class, () -> SplineTubeGenerator.generate(request(
                2, 8, fraction -> 0, CrossSectionProfile.circular(), fraction -> 0)));
        assertThrows(IllegalArgumentException.class, () -> SplineTubeGenerator.generate(request(
                2, 8, fraction -> 1, (arc, angle) -> Double.NaN, fraction -> 0)));
    }

    private static SplineTubeRequest request(
            int rings, int vertices, RadiusProfile radius,
            CrossSectionProfile crossSection, java.util.function.DoubleUnaryOperator roll) {
        CubicHermiteCurve centerline = new CubicHermiteCurve(
                Vector3.ZERO, new Vector3(0, 0, 4),
                new Vector3(0, 0, 4), new Vector3(0, 0, 4));
        return new SplineTubeRequest(centerline, rings, vertices, 65,
                new Vector3(1, 0, 0), radius, crossSection, roll, Set.of("tube.test"));
    }

    private static void assertVector(Vector3 actual, double x, double y, double z) {
        assertEquals(x, actual.x(), 1.0e-9);
        assertEquals(y, actual.y(), 1.0e-9);
        assertEquals(z, actual.z(), 1.0e-9);
        assertTrue(VectorMath.lengthSquared(actual) >= 0);
    }
}
