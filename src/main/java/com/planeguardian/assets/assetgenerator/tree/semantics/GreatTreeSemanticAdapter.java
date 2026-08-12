package com.planeguardian.assets.assetgenerator.tree.semantics;

import com.planeguardian.assets.assetgenerator.tree.generation.ResolvedTreeFeatures;
import com.planeguardian.assets.assetgenerator.tree.generation.TreeCrownSettings;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.AssetSemanticAdapter;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/** Great Tree translation from generic Lore Map coordinates to tree visual channels. */
public final class GreatTreeSemanticAdapter implements AssetSemanticAdapter {
    @Override public StableId adapterId() { return new StableId("adapter.tree.deciduous-semantics"); }
    @Override public ContractVersion adapterVersion() { return new ContractVersion(1, 0); }
    @Override public StableId assetFamily() { return new StableId("asset-family.vegetation.tree"); }

    @Override public ResolvedVisualProfile resolve(SemanticProfile source, ResolutionContext context) {
        SemanticWheelValue ethos = wheel(source, "lore.ethos");
        SemanticWheelValue relation = wheel(source, "lore.world-relation");
        SemanticWheelValue elements = wheel(source, "lore.elemental");
        TreeSemanticProfile legacy = new TreeSemanticProfile(weighted(ethos.x(), ethos), weighted(ethos.y(), ethos),
                weighted(relation.x(), relation), weighted(relation.y(), relation),
                sector(elements, 3 * StrictMath.PI / 2), sector(elements, StrictMath.PI / 4));
        ResolvedTreeFeatures resolved = TreeSemanticAdapter.resolve(TreeSemanticInputs.intrinsicOnly(legacy), TreeCrownSettings.defaults());
        TreeMap<String, Double> channels = new TreeMap<>(Map.of(
                "tree.crown.coverage", resolved.crown().coverage(), "tree.moss.coverage", resolved.mossCoverage(),
                "tree.feature.vine-coverage", resolved.vineCoverage(), "tree.feature.flower-density", resolved.flowerDensity(),
                "tree.feature.fruit-density", resolved.fruitDensity(), "tree.feature.fungal-coverage", resolved.fungalCoverage()));
        return new ResolvedVisualProfile(adapterVersion(), channels, resolved.contributions(), fingerprint(channels));
    }

    private static SemanticWheelValue wheel(SemanticProfile profile, String id) {
        return profile.wheels().getOrDefault(new StableId(id), SemanticWheelValue.centered(0));
    }
    private static double weighted(double value, SemanticWheelValue wheel) { return value * wheel.salience(); }
    private static double sector(SemanticWheelValue wheel, double angle) {
        return Math.max(0, wheel.x() * StrictMath.cos(angle) + wheel.y() * StrictMath.sin(angle)) * wheel.salience();
    }
    private static ReproducibilityFingerprint fingerprint(Map<String, Double> channels) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            channels.forEach((key, value) -> digest.update((key + "=" + Double.toHexString(value) + "\n").getBytes(StandardCharsets.UTF_8)));
            return new ReproducibilityFingerprint(digest.digest());
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
