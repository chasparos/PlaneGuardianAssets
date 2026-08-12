package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.determinism.NumericQuantizer;
import com.planeguardian.assets.generation.organic.FoliageClusterShellGenerator;
import com.planeguardian.assets.generation.organic.FoliageClusterShellRequest;
import com.planeguardian.assets.generation.topology.ProtoMeshFingerprints;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Composes bounded shared foliage shells into a deterministic deciduous-tree crown. */
public final class DeciduousTreeCrownGenerator {
    private static final NumericQuantizer FINGERPRINT_QUANTIZER = new NumericQuantizer(1e-6);

    private DeciduousTreeCrownGenerator() {
    }

    public static TreeCrownProduct generate(TreeStructure structure, TreeCrownSettings settings, long visualSeed) {
        if (structure == null || settings == null) {
            throw new IllegalArgumentException("structure and settings must not be null");
        }
        Map<StableId, TreeCrownPart> parts = new LinkedHashMap<>();
        int admitted = (int) StrictMath.round(settings.maximumClusters() * settings.coverage());
        for (int index = 0; index < admitted; index++) {
            var random = NamedRandomStreams.open(visualSeed, "tree.crown.cluster." + index);
            double angle = StrictMath.PI * 2 * index / Math.max(1, admitted);
            double radial = settings.widthRatio() * structure.heightMetres() * (.18 + random.nextDouble() * .12);
            double clusterWidth = settings.widthRatio() * structure.heightMetres()
                    * (.16 + random.nextDouble() * .07);
            double clusterHeight = settings.heightRatio() * structure.heightMetres()
                    * (.16 + random.nextDouble() * .06);
            Vector3 center = new Vector3(
                    StrictMath.cos(angle) * radial,
                    structure.heightMetres() * (settings.verticalOffsetRatio() + random.nextDouble() * .16),
                    StrictMath.sin(angle) * radial);
            StableId id = new StableId("tree.crown.cluster." + index);
            var mesh = FoliageClusterShellGenerator.generate(new FoliageClusterShellRequest(
                    center, new Vector3(clusterWidth, clusterHeight, clusterWidth),
                    settings.latitudeBands(), settings.radialSegments(), Set.of("tree.foliage")));
            parts.put(id, new TreeCrownPart(id, new StableId("tree.foliage"), mesh));
        }
        return new TreeCrownProduct(parts, fingerprint(parts));
    }

    private static ReproducibilityFingerprint fingerprint(Map<StableId, TreeCrownPart> parts) {
        FingerprintBuilder builder = new FingerprintBuilder().addString("TreeCrownProduct/1");
        parts.forEach((id, part) -> builder.addId(id).addString(
                ProtoMeshFingerprints.compute(part.mesh(), FINGERPRINT_QUANTIZER).hex()));
        return builder.build();
    }
}
