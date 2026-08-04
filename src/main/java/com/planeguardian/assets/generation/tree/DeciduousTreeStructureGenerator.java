package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.curves.CubicHermiteCurve;
import com.planeguardian.assets.generation.determinism.DeterministicRandom;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.determinism.NumericQuantizer;
import com.planeguardian.assets.generation.geometry.tube.CrossSectionProfile;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeGenerator;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeRequest;
import com.planeguardian.assets.generation.topology.ProtoMeshFingerprints;

import java.util.Set;

/** Composes shared spline and tube tooling for the Great Tree's deterministic trunk. */
public final class DeciduousTreeStructureGenerator {
    public static final String TRUNK_SPLINE_STREAM = "tree.trunkSpline";
    private static final NumericQuantizer FINGERPRINT_QUANTIZER = new NumericQuantizer(1.0e-6);

    private DeciduousTreeStructureGenerator() {
    }

    public static TreeStructuralProduct generateTrunk(TreeStructure structure, long visualSeed) {
        if (structure == null) {
            throw new IllegalArgumentException("structure must not be null");
        }
        DeterministicRandom random = NamedRandomStreams.open(visualSeed, TRUNK_SPLINE_STREAM);
        double height = structure.heightMetres();
        double lateralX = structure.leanX() * height;
        double lateralZ = structure.leanZ() * height;
        double curveX = (random.nextDouble() * 2 - 1) * structure.curvature() * height;
        double curveZ = (random.nextDouble() * 2 - 1) * structure.curvature() * height;
        CubicHermiteCurve centerline = new CubicHermiteCurve(
                Vector3.ZERO,
                new Vector3(curveX, height * 0.9, curveZ),
                new Vector3(lateralX, height, lateralZ),
                new Vector3(-curveX * 0.5, height * 0.7, -curveZ * 0.5));
        var tube = SplineTubeGenerator.generate(new SplineTubeRequest(
                centerline,
                structure.trunkRingCount(),
                structure.trunkVerticesPerRing(),
                Math.max(64, structure.trunkRingCount() * 4),
                new Vector3(1, 0, 0),
                fraction -> structure.baseRadiusMetres()
                        * StrictMath.pow(1 - fraction * 0.78, structure.taperExponent()),
                CrossSectionProfile.circular(),
                fraction -> structure.twistRadians() * fraction,
                Set.of("tree.trunk")));
        return new TreeStructuralProduct(tube.mesh(),
                ProtoMeshFingerprints.compute(tube.mesh(), FINGERPRINT_QUANTIZER));
    }
}
