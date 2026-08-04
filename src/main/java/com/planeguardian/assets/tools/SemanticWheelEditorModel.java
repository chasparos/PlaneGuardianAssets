package com.planeguardian.assets.tools;

import com.planeguardian.assets.generation.api.RenderTier;
import com.planeguardian.assets.generation.tree.DeciduousTreeStructureGenerator;
import com.planeguardian.assets.generation.tree.ResolvedTreeFeatures;
import com.planeguardian.assets.generation.tree.TreeComposition;
import com.planeguardian.assets.generation.tree.TreeCrownProduct;
import com.planeguardian.assets.generation.tree.TreeLodSettings;
import com.planeguardian.assets.generation.tree.TreeSemanticAdapter;
import com.planeguardian.assets.generation.tree.TreeSemanticInputs;
import com.planeguardian.assets.generation.tree.TreeSemanticProfile;
import com.planeguardian.assets.generation.tree.TreeStructuralProduct;
import com.planeguardian.assets.generation.tree.TreeStructure;

import java.util.Objects;

/** Regenerates the engine-neutral tree products selected by editable semantic-wheel source values. */
public final class SemanticWheelEditorModel {
    public Regeneration regenerate(TreeSemanticProfile profile, long seed, RenderTier tier) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(tier, "tier");
        TreeStructure structure = TreeStructure.defaults();
        TreeComposition defaults = structure.composition();
        ResolvedTreeFeatures resolved = TreeSemanticAdapter.resolve(
                TreeSemanticInputs.intrinsicOnly(profile), defaults.crown());
        TreeComposition composition = new TreeComposition(defaults.schemaVersion(), defaults.branchLevels(),
                defaults.roots(), lodFor(tier), defaults.maximumComponents(), resolved.crown(), defaults.features());
        TreeStructuralProduct structural = DeciduousTreeStructureGenerator.generate(structure, composition, seed);
        TreeCrownProduct crown = DeciduousTreeStructureGenerator.generateCrown(structure, composition, seed);
        return new Regeneration(profile, seed, tier, resolved, structural.fingerprint().hex(), crown.fingerprint().hex(),
                structural.parts().size(), crown.parts().size());
    }

    private static TreeLodSettings lodFor(RenderTier tier) {
        return switch (tier) {
            case PREVIEW -> new TreeLodSettings(0, 1);
            case GAMEPLAY -> new TreeLodSettings(1, 1);
            case DISTANT -> new TreeLodSettings(2, 0);
        };
    }

    /** Immutable read-only result consumed by the desktop inspection view. */
    public record Regeneration(TreeSemanticProfile profile, long seed, RenderTier tier,
                               ResolvedTreeFeatures resolved, String structuralFingerprint,
                               String crownFingerprint, int structuralPartCount, int crownPartCount) {
    }
}
