package com.planeguardian.assets.generation.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeSemanticAdapterTest {
    @Test
    void semanticGoldenCasesRemainBoundedAndExplainable() {
        TreeCrownSettings baseline = TreeCrownSettings.defaults();
        ResolvedTreeFeatures ordinary = resolve(TreeSemanticProfile.ordinary());
        ResolvedTreeFeatures opposed = resolve(new TreeSemanticProfile(1, -1, 1, -1, 0, 0));
        ResolvedTreeFeatures centered = resolve(new TreeSemanticProfile(0, 0, 0, 0, 0, 0));
        ResolvedTreeFeatures deathCreation = resolve(new TreeSemanticProfile(-1, 0, 0, 1, 0, 0));

        assertEquals(baseline.coverage(), ordinary.crown().coverage());
        assertEquals(ordinary.crown().coverage(), centered.crown().coverage());
        assertEquals(.72, opposed.crown().coverage());
        assertEquals(.36, deathCreation.crown().coverage());
        assertTrue(deathCreation.flowerDensity() > 0);
        assertTrue(deathCreation.contributions().stream()
                .filter(contribution -> contribution.targetParameter().equals("tree.crown.coverage")).count() == 7);
    }

    @Test
    void nativeLifeCrownIsNotReplacedByAnOffColorDeathHost() {
        TreeSemanticInputs inputs = new TreeSemanticInputs(
                new TreeSemanticProfile(1, 0, 0, 0, 0, 0),
                new TreeSemanticProfile(-1, 0, 0, 0, 0, 0), 1);

        ResolvedTreeFeatures resolved = TreeSemanticAdapter.resolve(inputs, TreeCrownSettings.defaults());

        assertEquals(.96, resolved.crown().coverage());
        assertTrue(resolved.crown().coverage() > .5);
    }

    private static ResolvedTreeFeatures resolve(TreeSemanticProfile profile) {
        return TreeSemanticAdapter.resolve(TreeSemanticInputs.intrinsicOnly(profile), TreeCrownSettings.defaults());
    }
}
