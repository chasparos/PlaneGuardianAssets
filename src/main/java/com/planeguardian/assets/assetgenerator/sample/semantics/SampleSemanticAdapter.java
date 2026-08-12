package com.planeguardian.assets.assetgenerator.sample.semantics;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.AssetSemanticAdapter;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Maps the high-quality side of the Quality wheel to the cylinder shape. */
public final class SampleSemanticAdapter implements AssetSemanticAdapter {
    public static final String SHAPE_PARAMETER = "sample.cylinder";

    @Override public StableId adapterId() { return new StableId("adapter.sample-quality-shape"); }
    @Override public ContractVersion adapterVersion() { return new ContractVersion(1, 0); }
    @Override public StableId assetFamily() { return new StableId("asset-family.sample"); }

    @Override
    public ResolvedVisualProfile resolve(SemanticProfile source, ResolutionContext context) {
        SemanticWheelValue quality = source.wheels().getOrDefault(
                new StableId("game.quality"), SemanticWheelValue.centered(0));
        double cylinder = quality.x() * quality.salience() >= 0 ? 1d : 0d;
        TreeMap<String, Double> channels = new TreeMap<>(Map.of(SHAPE_PARAMETER, cylinder));
        return new ResolvedVisualProfile(adapterVersion(), channels, List.of(), fingerprint(channels));
    }

    private static ReproducibilityFingerprint fingerprint(Map<String, Double> channels) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            channels.forEach((key, value) -> digest.update(
                    (key + "=" + Double.toHexString(value) + "\n").getBytes(StandardCharsets.UTF_8)));
            return new ReproducibilityFingerprint(digest.digest());
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
