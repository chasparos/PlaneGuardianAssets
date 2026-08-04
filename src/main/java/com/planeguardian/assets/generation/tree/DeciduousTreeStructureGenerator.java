package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.curves.CubicHermiteCurve;
import com.planeguardian.assets.generation.determinism.DeterministicRandom;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.determinism.NumericQuantizer;
import com.planeguardian.assets.generation.geometry.tube.CrossSectionProfile;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeGenerator;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeRequest;
import com.planeguardian.assets.generation.topology.ProtoMeshFingerprints;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Composes shared spline and tube tooling for the Great Tree's deterministic trunk. */
public final class DeciduousTreeStructureGenerator {
    public static final String TRUNK_SPLINE_STREAM = "tree.trunkSpline";
    private static final NumericQuantizer FINGERPRINT_QUANTIZER = new NumericQuantizer(1.0e-6);

    private DeciduousTreeStructureGenerator() {
    }

    public static TreeStructuralProduct generateTrunk(TreeStructure structure, long visualSeed) {
        return generate(structure, structure.composition(), visualSeed);
    }

    /** Generates bounded independently-addressable trunk, branch, and root tube components. */
    public static TreeStructuralProduct generate(TreeStructure structure, TreeComposition composition, long visualSeed) {
        if (structure == null) {
            throw new IllegalArgumentException("structure must not be null");
        }
        if (composition == null) {
            throw new IllegalArgumentException("composition must not be null");
        }
        var trunk = generateTrunkTube(structure, visualSeed);
        Map<StableId, TreeStructuralPart> parts = new LinkedHashMap<>();
        StableId trunkId = new StableId("tree.trunk");
        parts.put(trunkId, new TreeStructuralPart(trunkId, trunkId, trunk.mesh(), true));
        List<GeneratedSocket> sockets = new ArrayList<>();
        sockets.add(new GeneratedSocket(new StableId("tree.socket.root"), trunkId, Transform.IDENTITY));
        sockets.add(new GeneratedSocket(new StableId("tree.socket.trunk.tip"),
                new StableId("tree.trunk.tip"), Transform.IDENTITY));
        addBranches(structure, composition, visualSeed, parts, sockets);
        addRoots(structure, composition, visualSeed, parts, sockets);
        ReproducibilityFingerprint fingerprint = fingerprint(parts);
        return new TreeStructuralProduct(trunk.mesh(), parts, sockets, List.of(), fingerprint);
    }

    private static com.planeguardian.assets.generation.geometry.tube.SplineTubeResult generateTrunkTube(
            TreeStructure structure, long visualSeed) {
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
        return SplineTubeGenerator.generate(new SplineTubeRequest(
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
    }

    private static void addBranches(TreeStructure structure, TreeComposition composition, long seed,
                                    Map<StableId, TreeStructuralPart> parts, List<GeneratedSocket> sockets) {
        int allowedLevels = Math.min(composition.branchLevels().size(), composition.lod().branchLevelLimit());
        for (int level = 0; level < allowedLevels; level++) {
            TreeBranchLevel settings = composition.branchLevels().get(level);
            for (int index = 0; index < settings.maximumChildren() && parts.size() < composition.maximumComponents(); index++) {
                DeterministicRandom random = NamedRandomStreams.open(seed, "tree.branch." + level + "." + index);
                double fraction = settings.attachmentStart()
                        + (settings.attachmentEnd() - settings.attachmentStart()) * ((index + .5) / settings.maximumChildren());
                double angle = StrictMath.PI * 2 * index / settings.maximumChildren() + (random.nextDouble() - .5) * .18;
                double length = structure.heightMetres() * settings.lengthRatio() * (.85 + random.nextDouble() * .15);
                Vector3 start = new Vector3(structure.leanX() * structure.heightMetres() * fraction,
                        structure.heightMetres() * fraction, structure.leanZ() * structure.heightMetres() * fraction);
                Vector3 direction = new Vector3(StrictMath.cos(angle), settings.elevation(), StrictMath.sin(angle));
                Vector3 end = new Vector3(start.x() + direction.x() * length, start.y() + direction.y() * length,
                        start.z() + direction.z() * length);
                var tube = tube(new CubicHermiteCurve(start, direction, end,
                                new Vector3(direction.x(), direction.y() - .15, direction.z())),
                        settings.ringCount(), settings.verticesPerRing(),
                        structure.baseRadiusMetres() * settings.radiusRatio(), "tree.branch");
                StableId id = new StableId("tree.branch." + level + "." + index);
                parts.put(id, new TreeStructuralPart(id, new StableId("tree.branch"), tube.mesh(), false));
                sockets.add(new GeneratedSocket(new StableId("tree.socket.branch." + level + "." + index),
                        new StableId("tree.branch.tip"), Transform.IDENTITY));
            }
        }
    }

    private static void addRoots(TreeStructure structure, TreeComposition composition, long seed,
                                 Map<StableId, TreeStructuralPart> parts, List<GeneratedSocket> sockets) {
        TreeRootSettings settings = composition.roots();
        for (int index = 0; index < settings.rootCount() && parts.size() < composition.maximumComponents(); index++) {
            DeterministicRandom random = NamedRandomStreams.open(seed, "tree.root." + index);
            double angle = StrictMath.PI * 2 * index / settings.rootCount() + (random.nextDouble() - .5) * .15;
            double radius = structure.baseRadiusMetres() * settings.flareMultiplier();
            Vector3 start = new Vector3(StrictMath.cos(angle) * radius, 0, StrictMath.sin(angle) * radius);
            double length = structure.heightMetres() * settings.lengthRatio();
            Vector3 end = new Vector3(start.x() + StrictMath.cos(angle) * length,
                    -length * (1 - settings.exposedFraction()) * .2,
                    start.z() + StrictMath.sin(angle) * length);
            var tube = tube(new CubicHermiteCurve(start, new Vector3(StrictMath.cos(angle), -.1, StrictMath.sin(angle)),
                            end, new Vector3(StrictMath.cos(angle), -.25, StrictMath.sin(angle))),
                    settings.ringCount(), settings.verticesPerRing(), structure.baseRadiusMetres() * .42, "tree.root");
            StableId id = new StableId("tree.root." + index);
            parts.put(id, new TreeStructuralPart(id, new StableId("tree.root"), tube.mesh(), true));
            sockets.add(new GeneratedSocket(new StableId("tree.socket.root." + index),
                    new StableId("tree.root.tip"), Transform.IDENTITY));
        }
    }

    private static com.planeguardian.assets.generation.geometry.tube.SplineTubeResult tube(
            CubicHermiteCurve curve, int rings, int vertices, double radius, String group) {
        return SplineTubeGenerator.generate(new SplineTubeRequest(curve, rings, vertices, Math.max(64, rings * 4),
                new Vector3(1, 0, 0), fraction -> radius * (1 - fraction * .72), CrossSectionProfile.circular(),
                fraction -> 0, Set.of(group)));
    }

    private static ReproducibilityFingerprint fingerprint(Map<StableId, TreeStructuralPart> parts) {
        FingerprintBuilder builder = new FingerprintBuilder().addString("TreeStructuralProduct/1");
        parts.forEach((id, part) -> builder.addId(id).addString(
                ProtoMeshFingerprints.compute(part.mesh(), FINGERPRINT_QUANTIZER).hex()));
        return builder.build();
    }
}
