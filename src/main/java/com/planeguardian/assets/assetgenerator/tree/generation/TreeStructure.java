package com.planeguardian.assets.assetgenerator.tree.generation;

/** Direct, engine-neutral controls for the first spline-based Great Tree structural proof. */
public record TreeStructure(
        double heightMetres,
        double baseRadiusMetres,
        double taperExponent,
        double leanX,
        double leanZ,
        double curvature,
        double gnarliness,
        double gnarlinessFrequency,
        int splitCount,
        double splitStart,
        double splitDepartureAngle,
        double twistRadians,
        int trunkRingCount,
        int trunkVerticesPerRing) {

    public TreeStructure {
        requirePositiveFinite(heightMetres, "heightMetres");
        requirePositiveFinite(baseRadiusMetres, "baseRadiusMetres");
        requireFiniteInRange(taperExponent, 0, 4, "taperExponent");
        requireFiniteInRange(leanX, -0.5, 0.5, "leanX");
        requireFiniteInRange(leanZ, -0.5, 0.5, "leanZ");
        requireFiniteInRange(curvature, 0, 0.5, "curvature");
        requireFiniteInRange(gnarliness, 0, 0.5, "gnarliness");
        requireFiniteInRange(gnarlinessFrequency, 0, 12, "gnarlinessFrequency");
        if (splitCount < 0 || splitCount > 3) throw new IllegalArgumentException("splitCount must be in [0, 3]");
        requireFiniteInRange(splitStart, .15, .9, "splitStart");
        requireFiniteInRange(splitDepartureAngle, .05, 1.4, "splitDepartureAngle");
        requireFiniteInRange(twistRadians, -StrictMath.PI * 2, StrictMath.PI * 2, "twistRadians");
        if (trunkRingCount < 2 || trunkRingCount > 128) {
            throw new IllegalArgumentException("trunkRingCount must be in [2, 128]");
        }
        if (trunkVerticesPerRing < 8 || trunkVerticesPerRing > 64) {
            throw new IllegalArgumentException("trunkVerticesPerRing must be in [8, 64]");
        }
    }

    public TreeStructure(double heightMetres, double baseRadiusMetres, double taperExponent,
                         double leanX, double leanZ, double curvature, double twistRadians,
                         int trunkRingCount, int trunkVerticesPerRing) {
        this(heightMetres, baseRadiusMetres, taperExponent, leanX, leanZ, curvature,
                .035, 3, 0, .55, .5, twistRadians, trunkRingCount, trunkVerticesPerRing);
    }

    public static TreeStructure defaults() {
        return new TreeStructure(12, 0.45, 1.15, 0, 0, 0.08, .035, 3, 0, .55, .5, 0, 32, 12);
    }

    /** Versioned bounded controls introduced after the trunk-only proof. */
    public TreeComposition composition() {
        return TreeComposition.defaultsFor(this);
    }

    private static void requirePositiveFinite(double value, String name) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    private static void requireFiniteInRange(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be in [" + minimum + ", " + maximum + "]");
        }
    }
}
