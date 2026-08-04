package com.planeguardian.assets.tools;

import com.planeguardian.assets.generation.api.RenderTier;
import com.planeguardian.assets.generation.tree.TreeSemanticProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticWheelEditorModelTest {
    private final SemanticWheelEditorModel model = new SemanticWheelEditorModel();

    @Test
    void regenerationIsDeterministicAndExposesReadOnlySemanticDiagnostics() {
        TreeSemanticProfile profile = new TreeSemanticProfile(1, 0, 1, 1, .8, 0);

        var first = model.regenerate(profile, 42, RenderTier.GAMEPLAY);
        var repeated = model.regenerate(profile, 42, RenderTier.GAMEPLAY);

        assertEquals(first.structuralFingerprint(), repeated.structuralFingerprint());
        assertEquals(first.crownFingerprint(), repeated.crownFingerprint());
        assertEquals(42, first.seed());
        assertEquals(RenderTier.GAMEPLAY, first.tier());
        assertTrue(first.resolved().vineCoverage() > 0);
        assertEquals(12, first.resolved().contributions().size());
    }

    @Test
    void sourceWheelsAndTierRegenerateIndependentTreeProducts() {
        var ordinary = model.regenerate(TreeSemanticProfile.ordinary(), 42, RenderTier.GAMEPLAY);
        var vital = model.regenerate(new TreeSemanticProfile(1, 0, 0, 1, 0, 0), 42, RenderTier.GAMEPLAY);
        var distant = model.regenerate(TreeSemanticProfile.ordinary(), 42, RenderTier.DISTANT);

        assertNotEquals(ordinary.crownFingerprint(), vital.crownFingerprint());
        assertTrue(vital.resolved().crown().coverage() > ordinary.resolved().crown().coverage());
        assertTrue(distant.structuralPartCount() < ordinary.structuralPartCount());
    }
}
