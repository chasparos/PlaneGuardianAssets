package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.StableId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeciduousTreeFeatureGeometryGeneratorTest {
    @Test
    void admittedFeaturesProduceDeterministicIndependentEngineNeutralMeshes() {
        TreeFeatureProduct admissions = DeciduousTreeFeatureGenerator.generate(TreeStructure.defaults(),
                new TreeFeatureSettings(3, 3, 3, 3, 3, 7),
                new ResolvedTreeFeatures(TreeCrownSettings.defaults(), 1, 1, 1, 1, 1, List.of()), 71);

        TreeFeatureGeometryProduct first = DeciduousTreeFeatureGeometryGenerator.generate(admissions, 71);
        TreeFeatureGeometryProduct repeated = DeciduousTreeFeatureGeometryGenerator.generate(admissions, 71);

        assertEquals(first.fingerprint(), repeated.fingerprint());
        assertEquals(admissions.placements().keySet(), first.parts().keySet());
        assertTrue(first.parts().entrySet().stream().allMatch(entry ->
                entry.getKey().equals(entry.getValue().id())
                        && entry.getValue().role().value().startsWith("tree.")
                        && entry.getValue().mesh().isValid()
                        && entry.getValue().renderMesh().hasTextureCoordinates()
                        && entry.getValue().renderMesh().hasTangents()));
    }

    @Test
    void emptyAdmissionsProduceNoGeometry() {
        TreeFeatureGeometryProduct product = DeciduousTreeFeatureGeometryGenerator.generate(
                new TreeFeatureProduct(java.util.Map.of(),
                        new com.planeguardian.assets.generation.api.ReproducibilityFingerprint(
                                new byte[32])), 1);

        assertTrue(product.parts().isEmpty());
    }
}
