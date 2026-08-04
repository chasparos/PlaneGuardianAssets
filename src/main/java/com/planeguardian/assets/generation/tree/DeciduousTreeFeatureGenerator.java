package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.determinism.NumericQuantizer;

import java.util.LinkedHashMap;
import java.util.Map;

/** Admits bounded independent tree feature anchors from already-resolved semantic suitability. */
public final class DeciduousTreeFeatureGenerator {
    private static final NumericQuantizer FINGERPRINT_QUANTIZER = new NumericQuantizer(1e-6);

    private DeciduousTreeFeatureGenerator() {
    }

    public static TreeFeatureProduct generate(TreeStructure structure, TreeFeatureSettings settings,
                                              ResolvedTreeFeatures features, long visualSeed) {
        if (structure == null || settings == null || features == null) {
            throw new IllegalArgumentException("structure, settings, and features must not be null");
        }
        Map<StableId, TreeFeaturePlacement> placements = new LinkedHashMap<>();
        for (TreeFeatureGroup group : TreeFeatureGroup.values()) {
            int admitted = Math.min(settings.maximumFor(group),
                    (int) StrictMath.round(settings.maximumFor(group) * group.suitability(features)));
            for (int index = 0; index < admitted && placements.size() < settings.maximumFeatures(); index++) {
                StableId id = new StableId("tree.feature." + group.name().toLowerCase() + "." + index);
                var random = NamedRandomStreams.open(visualSeed, id.value());
                placements.put(id, new TreeFeaturePlacement(id, group, anchor(group, structure, random.nextDouble(),
                        random.nextDouble())));
            }
        }
        return new TreeFeatureProduct(placements, fingerprint(placements));
    }

    private static Vector3 anchor(TreeFeatureGroup group, TreeStructure structure, double azimuth, double height) {
        double angle = StrictMath.PI * 2 * azimuth;
        double radius = structure.baseRadiusMetres() * switch (group) {
            case MOSS, FUNGI -> .75;
            case VINES -> 1.05;
            case FLOWERS, FRUIT -> 2.5;
        };
        double y = structure.heightMetres() * switch (group) {
            case MOSS -> .05 + height * .25;
            case VINES -> .12 + height * .55;
            case FLOWERS, FRUIT -> .58 + height * .28;
            case FUNGI -> .01 + height * .08;
        };
        return new Vector3(StrictMath.cos(angle) * radius, y, StrictMath.sin(angle) * radius);
    }

    private static ReproducibilityFingerprint fingerprint(Map<StableId, TreeFeaturePlacement> placements) {
        FingerprintBuilder builder = new FingerprintBuilder().addString("TreeFeatureProduct/1");
        placements.forEach((id, placement) -> builder.addId(id).addId(placement.group().role())
                .addQuantized(placement.anchor().x(), FINGERPRINT_QUANTIZER)
                .addQuantized(placement.anchor().y(), FINGERPRINT_QUANTIZER)
                .addQuantized(placement.anchor().z(), FINGERPRINT_QUANTIZER));
        return builder.build();
    }
}
