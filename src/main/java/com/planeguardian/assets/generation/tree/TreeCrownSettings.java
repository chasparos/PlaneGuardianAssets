package com.planeguardian.assets.generation.tree;

/** Bounded direct controls for shared foliage-cluster crown composition. */
public record TreeCrownSettings(
        double coverage,
        int maximumClusters,
        double widthRatio,
        double heightRatio,
        double verticalOffsetRatio,
        int latitudeBands,
        int radialSegments) {
    public TreeCrownSettings {
        range(coverage, 0, 1, "coverage");
        if (maximumClusters < 0 || maximumClusters > 64) {
            throw new IllegalArgumentException("maximumClusters must be in [0, 64]");
        }
        range(widthRatio, .1, 2, "widthRatio");
        range(heightRatio, .1, 2, "heightRatio");
        range(verticalOffsetRatio, 0, 1.5, "verticalOffsetRatio");
        if (latitudeBands < 2 || latitudeBands > 16) {
            throw new IllegalArgumentException("latitudeBands must be in [2, 16]");
        }
        if (radialSegments < 8 || radialSegments > 32) {
            throw new IllegalArgumentException("radialSegments must be in [8, 32]");
        }
    }

    public static TreeCrownSettings defaults() {
        return new TreeCrownSettings(.72, 8, .62, .38, .62, 4, 8);
    }

    private static void range(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be in [" + minimum + ", " + maximum + "]");
        }
    }
}
