package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.crystal.CrystalGeometryGenerator;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.runtime.LoadedAsset;
import com.planeguardian.assets.runtime.SemanticReactive;
import java.util.List;

/** Selects pre-baked crystal geometry variants from resolved runtime semantics. */
public final class CrystalSemanticJmeCapability implements SemanticReactive {
    private final Node root;
    private String appliedFingerprint = "";

    public CrystalSemanticJmeCapability(Node root) {
        this.root = java.util.Objects.requireNonNull(root);
    }

    @Override public StableId capabilityId() {
        return new StableId("runtime.crystal.semantics");
    }

    @Override public LoadedAsset.SemanticApplicationResult applySemantics(ResolvedVisualProfile profile,
            LoadedAsset.SemanticContext context) {
        String fingerprint = profile.fingerprint().hex() + ":" + context.hostInfluence();
        if (fingerprint.equals(appliedFingerprint)) return new LoadedAsset.SemanticApplicationResult(false, List.of());
        int sides = selectedSides(profile.channels().getOrDefault("crystal.facet-count", 6d));
        for (Spatial child : root.getChildren()) {
            String name = child.getName() == null ? "" : child.getName();
            if (name.startsWith("crystal.variant.")) {
                child.setCullHint(name.equals("crystal.variant." + sides)
                        ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
            }
        }
        root.setUserData("pg.semanticFingerprint", profile.fingerprint().hex());
        root.setUserData("pg.crystal.variant-sides", sides);
        appliedFingerprint = fingerprint;
        return new LoadedAsset.SemanticApplicationResult(true, List.of());
    }

    private static int selectedSides(double requested) {
        return CrystalGeometryGenerator.canonicalSides((int) Math.round(requested));
    }
}
