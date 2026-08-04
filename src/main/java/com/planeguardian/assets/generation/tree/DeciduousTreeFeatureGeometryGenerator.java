package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.determinism.NumericQuantizer;
import com.planeguardian.assets.generation.geometry.foliage.FoliageClusterShellGenerator;
import com.planeguardian.assets.generation.geometry.foliage.FoliageClusterShellRequest;
import com.planeguardian.assets.generation.topology.ProtoMeshFingerprints;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Converts admitted tree feature anchors to bounded, renderer-neutral mesh products. */
public final class DeciduousTreeFeatureGeometryGenerator {
    private static final NumericQuantizer FINGERPRINT_QUANTIZER = new NumericQuantizer(1e-6);

    private DeciduousTreeFeatureGeometryGenerator() {
    }

    public static TreeFeatureGeometryProduct generate(TreeFeatureProduct features, long visualSeed) {
        if (features == null) {
            throw new IllegalArgumentException("features must not be null");
        }
        Map<StableId, TreeFeatureGeometryPart> parts = new LinkedHashMap<>();
        features.placements().forEach((id, placement) -> {
            var random = NamedRandomStreams.open(visualSeed, id.value());
            double variation = .85 + random.nextDouble() * .3;
            Vector3 radii = radii(placement.group(), variation);
            var mesh = FoliageClusterShellGenerator.generate(new FoliageClusterShellRequest(
                    placement.anchor(), radii, 2, 8, Set.of(placement.group().role().value())));
            parts.put(id, new TreeFeatureGeometryPart(id, placement.group().role(), mesh));
        });
        return new TreeFeatureGeometryProduct(parts, fingerprint(parts));
    }

    private static Vector3 radii(TreeFeatureGroup group, double variation) {
        return switch (group) {
            case MOSS -> new Vector3(.24 * variation, .045 * variation, .24 * variation);
            case VINES -> new Vector3(.075 * variation, .42 * variation, .075 * variation);
            case FLOWERS -> new Vector3(.14 * variation, .11 * variation, .14 * variation);
            case FRUIT -> new Vector3(.12 * variation, .15 * variation, .12 * variation);
            case FUNGI -> new Vector3(.18 * variation, .065 * variation, .12 * variation);
        };
    }

    private static ReproducibilityFingerprint fingerprint(Map<StableId, TreeFeatureGeometryPart> parts) {
        FingerprintBuilder builder = new FingerprintBuilder().addString("TreeFeatureGeometryProduct/1");
        parts.forEach((id, part) -> builder.addId(id).addString(
                ProtoMeshFingerprints.compute(part.mesh(), FINGERPRINT_QUANTIZER).hex()));
        return builder.build();
    }
}
