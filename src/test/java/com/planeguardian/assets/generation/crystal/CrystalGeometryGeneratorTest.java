package com.planeguardian.assets.generation.crystal;

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
}
