package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.curves.CubicHermiteCurve;
import com.planeguardian.assets.generation.curves.HarmonicPerturbedCurve;
import com.planeguardian.assets.generation.curves.ParametricCurve3;
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

    /** Generates the independent foliage product selected by the composition's direct crown controls. */
    public static TreeCrownProduct generateCrown(TreeStructure structure, TreeComposition composition, long visualSeed) {
        if (composition == null) {
            throw new IllegalArgumentException("composition must not be null");
        }
        return DeciduousTreeCrownGenerator.generate(structure, composition.crown(), visualSeed);
    }

    /** Generates independent bounded semantic feature anchors selected by resolved suitability. */
    public static TreeFeatureProduct generateFeatures(TreeStructure structure, TreeComposition composition,
                                                      ResolvedTreeFeatures features, long visualSeed) {
        if (composition == null) {
            throw new IllegalArgumentException("composition must not be null");
        }
        return DeciduousTreeFeatureGenerator.generate(structure, composition.features(), features, visualSeed);
    }

    /** Generates independent engine-neutral mesh products from already admitted feature anchors. */
    public static TreeFeatureGeometryProduct generateFeatureGeometry(TreeFeatureProduct features, long visualSeed) {
        return DeciduousTreeFeatureGeometryGenerator.generate(features, visualSeed);
    }

    /** Generates bounded independently-addressable trunk, branch, and root tube components. */
    public static TreeStructuralProduct generate(TreeStructure structure, TreeComposition composition, long visualSeed) {
        if (structure == null) {
            throw new IllegalArgumentException("structure must not be null");
        }
        if (composition == null) {
            throw new IllegalArgumentException("composition must not be null");
        }
        ParametricCurve3 trunkCurve = generateTrunkCurve(structure, visualSeed);
        var trunk = generateTrunkTube(structure, trunkCurve);
        Map<StableId, TreeStructuralPart> parts = new LinkedHashMap<>();
        StableId trunkId = new StableId("tree.trunk");
        parts.put(trunkId, new TreeStructuralPart(trunkId, trunkId, trunk.mesh(), true));
        List<GeneratedSocket> sockets = new ArrayList<>();
        sockets.add(new GeneratedSocket(new StableId("tree.socket.root"), trunkId, Transform.IDENTITY));
        sockets.add(new GeneratedSocket(new StableId("tree.socket.trunk.tip"),
                new StableId("tree.trunk.tip"), Transform.IDENTITY));
        List<BranchParent> branchParents = new ArrayList<>();
        branchParents.add(new BranchParent("trunk", trunkCurve, structure.heightMetres(),
                structure.baseRadiusMetres(), structure.baseRadiusMetres() * .22));
        branchParents.addAll(addTrunkSplits(structure, composition, visualSeed, trunkCurve, parts, sockets));
        addBranches(structure, composition, visualSeed, parts, sockets, branchParents);
        addRoots(structure, composition, visualSeed, parts, sockets);
        addHollow(structure, composition, parts, sockets);
        ReproducibilityFingerprint fingerprint = fingerprint(parts);
        return new TreeStructuralProduct(trunk.mesh(), parts, sockets, List.of(), fingerprint);
    }

    private static ParametricCurve3 generateTrunkCurve(TreeStructure structure, long visualSeed) {
        DeterministicRandom random = NamedRandomStreams.open(visualSeed, TRUNK_SPLINE_STREAM);
        double height = structure.heightMetres();
        double lateralX = structure.leanX() * height;
        double lateralZ = structure.leanZ() * height;
        double curveX = (random.nextDouble() * 2 - 1) * structure.curvature() * height;
        double curveZ = (random.nextDouble() * 2 - 1) * structure.curvature() * height;
        ParametricCurve3 centerline = new CubicHermiteCurve(
                Vector3.ZERO,
                new Vector3(curveX, height * 0.9, curveZ),
                new Vector3(lateralX, height, lateralZ),
                new Vector3(-curveX * 0.5, height * 0.7, -curveZ * 0.5));
        centerline = new HarmonicPerturbedCurve(centerline, new Vector3(1, 0, 0), new Vector3(0, 0, 1),
                height * structure.gnarliness(), structure.gnarlinessFrequency(), random.nextDouble() * StrictMath.PI * 2);
        return centerline;
    }

    private static com.planeguardian.assets.generation.geometry.tube.SplineTubeResult generateTrunkTube(
            TreeStructure structure, ParametricCurve3 centerline) {
        int rings = safeRingCount(structure.trunkRingCount(), centerline, structure.baseRadiusMetres(), 128);
        return SplineTubeGenerator.generate(new SplineTubeRequest(centerline, rings,
                structure.trunkVerticesPerRing(),
                Math.max(128, rings * 4),
                new Vector3(1, 0, 0),
                fraction -> structure.baseRadiusMetres()
                        * StrictMath.pow(1 - fraction * 0.78, structure.taperExponent()),
                CrossSectionProfile.circular(),
                fraction -> structure.twistRadians() * fraction,
                Set.of("tree.trunk")));
    }

    private static void addBranches(TreeStructure structure, TreeComposition composition, long seed,
                                    Map<StableId, TreeStructuralPart> parts, List<GeneratedSocket> sockets,
                                    List<BranchParent> initialParents) {
        int allowedLevels = Math.min(composition.branchLevels().size(), composition.lod().branchLevelLimit());
        List<BranchParent> parents = List.copyOf(initialParents);
        for (int level = 0; level < allowedLevels; level++) {
            TreeBranchLevel settings = composition.branchLevels().get(level);
            List<BranchParent> children = new ArrayList<>();
            for (BranchParent parent : parents) {
                for (int index = 0; index < settings.maximumChildren()
                        && parts.size() < composition.maximumComponents(); index++) {
                    String path = parent.path() + "." + index;
                    DeterministicRandom random = NamedRandomStreams.open(seed, "tree.branch." + level + "." + path);
                    double fraction = settings.attachmentStart()
                            + (settings.attachmentEnd() - settings.attachmentStart())
                            * ((index + .5) / settings.maximumChildren());
                    Vector3 start = parent.curve().position(fraction);
                    Vector3 parentTangent = normalize(parent.curve().derivative(fraction));
                    double azimuth = index * 2.399963229728653 + (random.nextDouble() - .5) * .65;
                    Vector3 radialA = perpendicular(parentTangent);
                    Vector3 radialB = normalize(cross(parentTangent, radialA));
                    Vector3 radial = add(scale(radialA, StrictMath.cos(azimuth)), scale(radialB, StrictMath.sin(azimuth)));
                    double departure = settings.departureAngleMin()
                            + (settings.departureAngleMax() - settings.departureAngleMin()) * random.nextDouble();
                    Vector3 direction = normalize(add(scale(parentTangent, StrictMath.cos(departure)),
                            scale(radial, StrictMath.sin(departure))));
                    direction = normalize(add(direction, scale(new Vector3(0, 1, 0), settings.elevation() * .18)));
                    double length = parent.length() * settings.lengthRatio()
                            * StrictMath.pow(.72, level) * (.85 + random.nextDouble() * .15);
                    Vector3 end = add(start, scale(direction, length));
                    double radius = parent.radiusAt(fraction) * settings.radiusRatio();
                    Vector3 endDirection = normalize(add(direction, scale(parentTangent, settings.curvature())));
                    ParametricCurve3 curve = new CubicHermiteCurve(start, scale(direction, length * .35), end,
                                    scale(endDirection, length * .35));
                    curve = new HarmonicPerturbedCurve(curve, radialA, radialB, radius * settings.gnarliness() * 4,
                            settings.gnarlinessFrequency(), random.nextDouble() * StrictMath.PI * 2);
                    var tube = tube(curve,
                            settings.ringCount(), settings.verticesPerRing(), radius, "tree.branch");
                    StableId id = new StableId("tree.branch." + level + "." + path);
                    parts.put(id, new TreeStructuralPart(id, new StableId("tree.branch"), tube.mesh(), false));
                    sockets.add(new GeneratedSocket(new StableId("tree.socket.branch." + level + "." + path),
                            new StableId("tree.branch.tip"), Transform.IDENTITY));
                    children.add(new BranchParent(path, curve, length, radius, radius * .28));
                }
            }
            parents = List.copyOf(children);
            if (parents.isEmpty()) return;
        }
    }

    private static List<BranchParent> addTrunkSplits(TreeStructure structure, TreeComposition composition, long seed,
                                                      ParametricCurve3 trunk, Map<StableId, TreeStructuralPart> parts,
                                                      List<GeneratedSocket> sockets) {
        List<BranchParent> leaders = new ArrayList<>();
        for (int index = 0; index < structure.splitCount() && parts.size() < composition.maximumComponents(); index++) {
            DeterministicRandom random = NamedRandomStreams.open(seed, "tree.trunk.split." + index);
            double fraction = Math.min(.9, structure.splitStart() + index * .12);
            Vector3 start = trunk.position(fraction);
            Vector3 tangent = normalize(trunk.derivative(fraction));
            Vector3 radialA = perpendicular(tangent);
            Vector3 radialB = normalize(cross(tangent, radialA));
            double azimuth = index * 2.399963229728653 + random.nextDouble();
            Vector3 radial = add(scale(radialA, StrictMath.cos(azimuth)), scale(radialB, StrictMath.sin(azimuth)));
            Vector3 direction = normalize(add(scale(tangent, StrictMath.cos(structure.splitDepartureAngle())),
                    scale(radial, StrictMath.sin(structure.splitDepartureAngle()))));
            double length = structure.heightMetres() * (1 - fraction) * (.9 + random.nextDouble() * .25);
            Vector3 end = add(start, scale(direction, length));
            double radius = structure.baseRadiusMetres()
                    * StrictMath.pow(1 - fraction * .78, structure.taperExponent()) * .72;
            ParametricCurve3 curve = new CubicHermiteCurve(start, scale(direction, length * .45), end,
                    scale(normalize(add(direction, scale(tangent, .35))), length * .35));
            curve = new HarmonicPerturbedCurve(curve, radialA, radialB, radius * structure.gnarliness() * 3,
                    structure.gnarlinessFrequency(), random.nextDouble() * StrictMath.PI * 2);
            var generated = tube(curve, Math.max(16, structure.trunkRingCount() / 2),
                    structure.trunkVerticesPerRing(), radius, "tree.trunk");
            StableId id = new StableId("tree.trunk.split." + index);
            parts.put(id, new TreeStructuralPart(id, new StableId("tree.trunk"), generated.mesh(), false));
            sockets.add(new GeneratedSocket(new StableId("tree.socket.trunk.split." + index), id, Transform.IDENTITY));
            leaders.add(new BranchParent("split." + index, curve, length, radius, radius * .28));
        }
        return leaders;
    }

    private static void addRoots(TreeStructure structure, TreeComposition composition, long seed,
                                 Map<StableId, TreeStructuralPart> parts, List<GeneratedSocket> sockets) {
        TreeRootSettings settings = composition.roots();
        for (int index = 0; index < settings.rootCount() && parts.size() < composition.maximumComponents(); index++) {
            DeterministicRandom random = NamedRandomStreams.open(seed, "tree.root." + index);
            double angle = StrictMath.PI * 2 * index / settings.rootCount() + (random.nextDouble() - .5) * .15;
            // Begin inside the lower trunk so the independently generated tube
            // overlaps the flare instead of reading as a detached ground spike.
            double rootOriginRadius = structure.baseRadiusMetres() * .32;
            Vector3 start = new Vector3(StrictMath.cos(angle) * rootOriginRadius,
                    structure.baseRadiusMetres() * .28, StrictMath.sin(angle) * rootOriginRadius);
            double length = structure.heightMetres() * settings.lengthRatio();
            Vector3 end = new Vector3(start.x() + StrictMath.cos(angle) * length,
                    -length * (1 - settings.exposedFraction()) * (.12 + settings.curvature() * .2),
                    start.z() + StrictMath.sin(angle) * length);
            Vector3 outward = new Vector3(StrictMath.cos(angle), -.05 - settings.curvature() * .2, StrictMath.sin(angle));
            ParametricCurve3 curve = new CubicHermiteCurve(start, scale(outward, length * .8), end,
                    scale(new Vector3(outward.x(), -.25, outward.z()), length * .6));
            curve = new HarmonicPerturbedCurve(curve, new Vector3(0, 1, 0),
                    new Vector3(-outward.z(), 0, outward.x()), structure.baseRadiusMetres() * settings.gnarliness() * 2,
                    settings.gnarlinessFrequency(), random.nextDouble() * StrictMath.PI * 2);
            var tube = tube(curve, settings.ringCount(), settings.verticesPerRing(),
                    structure.baseRadiusMetres() * settings.flareMultiplier() * .42, "tree.root");
            StableId id = new StableId("tree.root." + index);
            parts.put(id, new TreeStructuralPart(id, new StableId("tree.root"), tube.mesh(), true));
            sockets.add(new GeneratedSocket(new StableId("tree.socket.root." + index),
                    new StableId("tree.root.tip"), Transform.IDENTITY));
        }
    }

    /**
     * Adds a bounded interior-facing tube surface after the ordinary components,
     * so it can never displace an already admitted branch or root under a budget.
     * Boolean subtraction remains deliberately outside the POC topology scope.
     */
    private static void addHollow(TreeStructure structure, TreeComposition composition,
                                  Map<StableId, TreeStructuralPart> parts, List<GeneratedSocket> sockets) {
        if (parts.size() >= composition.maximumComponents()) return;
        double height = structure.heightMetres();
        double radius = structure.baseRadiusMetres() * .38;
        Vector3 center = new Vector3(0, height * .12, -structure.baseRadiusMetres() * .44);
        Vector3 end = new Vector3(center.x(), height * .44, center.z());
        var tube = tube(new CubicHermiteCurve(center, new Vector3(0, height * .18, 0),
                        end, new Vector3(0, height * .18, 0)),
                Math.max(4, structure.trunkRingCount() / 3), structure.trunkVerticesPerRing(),
                radius, "tree.hollow");
        StableId id = new StableId("tree.hollow");
        parts.put(id, new TreeStructuralPart(id, id, tube.mesh(), false));
        sockets.add(new GeneratedSocket(new StableId("tree.socket.hollow"),
                id, new Transform(center, com.planeguardian.assets.generation.api.Rotation.IDENTITY, Vector3.ONE)));
    }

    private static com.planeguardian.assets.generation.geometry.tube.SplineTubeResult tube(
            ParametricCurve3 curve, int rings, int vertices, double radius, String group) {
        int safeRings = safeRingCount(rings, curve, radius, 64);
        return SplineTubeGenerator.generate(new SplineTubeRequest(curve, safeRings, vertices, Math.max(96, safeRings * 4),
                new Vector3(1, 0, 0), fraction -> radius * (1 - fraction * .72), CrossSectionProfile.circular(),
                fraction -> 0, Set.of(group)));
    }

    private static ReproducibilityFingerprint fingerprint(Map<StableId, TreeStructuralPart> parts) {
        FingerprintBuilder builder = new FingerprintBuilder().addString("TreeStructuralProduct/1");
        parts.forEach((id, part) -> builder.addId(id).addString(
                ProtoMeshFingerprints.compute(part.mesh(), FINGERPRINT_QUANTIZER).hex()));
        return builder.build();
    }

    private static Vector3 interpolate(Vector3 start, Vector3 end, double fraction) {
        return add(start, scale(subtract(end, start), fraction));
    }

    private static Vector3 add(Vector3 left, Vector3 right) {
        return new Vector3(left.x() + right.x(), left.y() + right.y(), left.z() + right.z());
    }

    private static Vector3 subtract(Vector3 left, Vector3 right) {
        return new Vector3(left.x() - right.x(), left.y() - right.y(), left.z() - right.z());
    }

    private static Vector3 scale(Vector3 value, double factor) {
        return new Vector3(value.x() * factor, value.y() * factor, value.z() * factor);
    }

    private static int safeRingCount(int requested, ParametricCurve3 curve, double radius, int maximum) {
        double chord = Math.max(.001, com.planeguardian.assets.generation.math.VectorMath.distance(
                curve.position(0), curve.position(1)));
        int thicknessSampling = (int) StrictMath.ceil(12 + radius / chord * 112);
        return Math.min(maximum, Math.max(requested, thicknessSampling));
    }

    private static Vector3 normalize(Vector3 value) {
        return com.planeguardian.assets.generation.math.VectorMath.normalize(value);
    }

    private static Vector3 cross(Vector3 a, Vector3 b) {
        return com.planeguardian.assets.generation.math.VectorMath.cross(a, b);
    }

    private static Vector3 perpendicular(Vector3 tangent) {
        Vector3 reference = StrictMath.abs(tangent.y()) < .9 ? new Vector3(0, 1, 0) : new Vector3(1, 0, 0);
        return normalize(cross(tangent, reference));
    }

    private record BranchParent(String path, ParametricCurve3 curve, double length,
                                double startRadius, double endRadius) {
        double radiusAt(double fraction) {
            return startRadius + (endRadius - startRadius) * fraction;
        }
    }
}
