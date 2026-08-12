package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.RenderTier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreePresentationContractsTest {
    @Test
    void presentationControlsAndWindResponseAreBoundedAndDeterministic() {
        TreePresentationSettings settings = TreePresentationSettings.defaults();

        TreeWindResponse first = TreeWindResponse.forPart(new com.planeguardian.assets.generation.api.StableId("tree.crown.cluster.0"),
                settings, 42);
        TreeWindResponse repeated = TreeWindResponse.forPart(first.partId(), settings, 42);

        assertEquals(RenderTier.GAMEPLAY, settings.renderTier());
        assertEquals(first, repeated);
        assertTrue(first.weight() > 0);
        assertThrows(IllegalArgumentException.class, () -> new TreePresentationSettings(RenderTier.PREVIEW, 0, 0, 0, 0, 0, 1, 0));
    }

    @Test
    void manifestAndProposalOnlyExposeValidatedDirectInputs() {
        TreeParameterManifest manifest = TreeParameterManifest.current();
        TreeParameterProposal proposal = new TreeParameterProposal(manifest.schemaVersion(),
                Map.of("tree.motion.wind-amplitude", .8, "tree.render-tier", 1d),
                Map.of("vitality", .7, "water", .2), "great oak");

        assertEquals(60, manifest.parameters().size());
        assertEquals(.8, proposal.parameterValues().get("tree.motion.wind-amplitude"));
        assertThrows(IllegalArgumentException.class, () -> new TreeParameterProposal(manifest.schemaVersion(),
                Map.of("tree.crown.resolved-coverage", .9), Map.of(), "invalid derived output"));
        assertThrows(IllegalArgumentException.class, () -> new TreeParameterProposal(manifest.schemaVersion(),
                Map.of(), Map.of("water", -.1), "invalid element"));
    }
}
