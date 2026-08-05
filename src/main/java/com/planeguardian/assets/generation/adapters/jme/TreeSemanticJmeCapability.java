package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.runtime.LoadedAsset;
import com.planeguardian.assets.runtime.SemanticReactive;
import java.util.List;

/** Applies resolved tree variants/material state to an already loaded jME subtree. */
public final class TreeSemanticJmeCapability implements SemanticReactive {
    private final Node root;
    private String appliedFingerprint = "";
    public TreeSemanticJmeCapability(Node root) { this.root = java.util.Objects.requireNonNull(root); }
    @Override public StableId capabilityId() { return new StableId("runtime.tree.semantics"); }

    @Override public LoadedAsset.SemanticApplicationResult applySemantics(ResolvedVisualProfile profile,
            LoadedAsset.SemanticContext context) {
        String fingerprint = profile.fingerprint().hex() + ":" + context.hostInfluence();
        if (fingerprint.equals(appliedFingerprint)) return new LoadedAsset.SemanticApplicationResult(false, List.of());
        double coverage = profile.channels().getOrDefault("tree.crown.coverage", 1d);
        double flowers = profile.channels().getOrDefault("tree.feature.flower-density", 0d);
        visit(root, spatial -> {
            String name = spatial.getName() == null ? "" : spatial.getName();
            if (name.contains("crown") || name.contains("foliage")) spatial.setCullHint(coverage <= .01 ? Spatial.CullHint.Always : Spatial.CullHint.Inherit);
            if (name.startsWith("vfx.") || name.startsWith("pollen.")) spatial.setCullHint(flowers <= .01 ? Spatial.CullHint.Always : Spatial.CullHint.Inherit);
            if (spatial instanceof Geometry geometry) {
                geometry.setUserData("pg.semanticFingerprint", profile.fingerprint().hex());
                geometry.setUserData("pg.crownCoverage", (float) coverage);
                if ((name.contains("crown") || name.contains("foliage"))
                        && geometry.getMaterial().getMaterialDef().getMaterialParam("BaseColor") != null) {
                    geometry.getMaterial().setColor("BaseColor", new com.jme3.math.ColorRGBA(
                            (float) (.18 + .12 * coverage), (float) (.24 + .48 * coverage),
                            (float) (.12 + .16 * coverage), 1));
                }
            }
        });
        root.setUserData("pg.semanticFingerprint", profile.fingerprint().hex());
        appliedFingerprint = fingerprint;
        return new LoadedAsset.SemanticApplicationResult(true, List.of());
    }

    private static void visit(Spatial spatial, java.util.function.Consumer<Spatial> action) {
        action.accept(spatial);
        if (spatial instanceof Node node) node.getChildren().forEach(child -> visit(child, action));
    }
}
