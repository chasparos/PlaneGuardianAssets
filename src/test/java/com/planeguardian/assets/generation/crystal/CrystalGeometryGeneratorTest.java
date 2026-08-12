package com.planeguardian.assets.generation.crystal;

import com.planeguardian.assets.generation.api.Vector3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CrystalGeometryGeneratorTest {
    @Test
    void geometryIsDeterministicAndStructurallyValid() {
        CrystalParameters parameters = CrystalParameters.defaults();
        var first = CrystalGeometryGenerator.generate(parameters, 42);
        var second = CrystalGeometryGenerator.generate(parameters, 42);
        assertEquals(first.fingerprint(), second.fingerprint());
        assertTrue(first.parts().values().stream().allMatch(part -> part.mesh().isValid()));
        assertTrue(first.parts().values().stream().allMatch(part -> part.renderMesh().hasTangents() && part.renderMesh().triangleCount() > 0));
        assertTrue(first.parts().containsKey(new com.planeguardian.assets.generation.api.StableId("crystal.0")));
    }

    @Test
    void levitationSkipsSolidHost() {
        CrystalParameters parameters = new CrystalParameters(0.35, 0.18, 6, 5, 2, 1, CrystalParameters.SettingKind.LEVITATION, new com.planeguardian.assets.generation.api.StableId("palette.gem.quartz-clear"), java.util.Optional.empty());
        var product = CrystalGeometryGenerator.generate(parameters, 9);
        assertTrue(product.parts().keySet().stream().noneMatch(id -> id.value().contains("host")));
    }

    @Test
    void crystalFormsUseCanonicalSideCounts() {
        for (int requested : new int[]{4, 6, 8}) {
            CrystalParameters parameters = new CrystalParameters(0.35, 0.18, requested, 5, 1, 1,
                    CrystalParameters.SettingKind.LEVITATION, new com.planeguardian.assets.generation.api.StableId("palette.gem.quartz-clear"),
                    java.util.Optional.empty());
            var mesh = CrystalGeometryGenerator.generate(parameters, requested).parts().get(
                    new com.planeguardian.assets.generation.api.StableId("crystal.0")).mesh();
            assertEquals(3 * requested + 1, mesh.vertices().size());
            assertTrue(mesh.faces().size() >= requested * 3);
        }
    }

    @Test
    void crystalFacesPointAwayFromTheCrystalCenter() {
        var mesh = CrystalGeometryGenerator.generate(CrystalParameters.defaults(), 42).parts().get(
                new com.planeguardian.assets.generation.api.StableId("crystal.0")).mesh();
        Vector3 center = new Vector3(0, 0.5, 0);

        for (var face : mesh.faces().values()) {
            var positions = face.loops().stream()
                    .map(loopId -> mesh.vertices().get(mesh.loops().get(loopId).vertexId()).position())
                    .toList();
            Vector3 a = positions.get(0);
            Vector3 b = positions.get(1);
            Vector3 c = positions.get(2);
            Vector3 normal = cross(subtract(b, a), subtract(c, a));
            Vector3 centroid = positions.stream().reduce(Vector3.ZERO, CrystalGeometryGeneratorTest::add);
            centroid = scale(centroid, 1.0 / positions.size());

            assertTrue(dot(normal, subtract(centroid, center)) > 0,
                    () -> "face points inward: " + face.id());
        }
    }

    private static Vector3 add(Vector3 a, Vector3 b) {
        return new Vector3(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    private static Vector3 subtract(Vector3 a, Vector3 b) {
        return new Vector3(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    private static Vector3 scale(Vector3 value, double factor) {
        return new Vector3(value.x() * factor, value.y() * factor, value.z() * factor);
    }

    private static Vector3 cross(Vector3 a, Vector3 b) {
        return new Vector3(a.y() * b.z() - a.z() * b.y(),
                a.z() * b.x() - a.x() * b.z(),
                a.x() * b.y() - a.y() * b.x());
    }

    private static double dot(Vector3 a, Vector3 b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
    }
}
