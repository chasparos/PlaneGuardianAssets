package com.planeguardian.assets.generation.tree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeciduousTreeFeatureGeneratorTest {
    @Test
    void featureAdmissionsAreDeterministicIndependentAndBounded() {
        TreeStructure structure = TreeStructure.defaults();
        TreeFeatureSettings settings = new TreeFeatureSettings(3, 3, 3, 3, 3, 7);
        ResolvedTreeFeatures fullySuitable = new ResolvedTreeFeatures(TreeCrownSettings.defaults(),
                1, 1, 1, 1, 1, List.of());

        TreeFeatureProduct first = DeciduousTreeFeatureGenerator.generate(structure, settings, fullySuitable, 71);
        TreeFeatureProduct repeated = DeciduousTreeFeatureGenerator.generate(structure, settings, fullySuitable, 71);

        assertEquals(first.fingerprint(), repeated.fingerprint());
        assertEquals(7, first.placements().size());
        assertTrue(first.placements().containsKey(new com.planeguardian.assets.generation.api.StableId("tree.feature.moss.0")));
        assertTrue(first.placements().values().stream().allMatch(placement ->
                placement.id().value().startsWith("tree.feature.")
                        && placement.group().role().value().startsWith("tree.")));
        assertTrue(first.placements().values().stream().allMatch(placement ->
                placement.anchor().y() >= 0 && placement.anchor().y() <= structure.heightMetres()));
    }

    @Test
    void suppressedSuitabilityAdmitsNoFeatures() {
        ResolvedTreeFeatures suppressed = new ResolvedTreeFeatures(TreeCrownSettings.defaults(),
                0, 0, 0, 0, 0, List.of());

        TreeFeatureProduct product = DeciduousTreeFeatureGenerator.generate(TreeStructure.defaults(),
                TreeFeatureSettings.defaults(), suppressed, 72);

        assertTrue(product.placements().isEmpty());
    }
}
