package com.planeguardian.assets.generation.tree;

/** Bounded major-root and root-flare controls. */
public record TreeRootSettings(
        int rootCount, double flareMultiplier, double lengthRatio, double exposedFraction,
        double curvature, double gnarliness, double gnarlinessFrequency,
        int ringCount, int verticesPerRing) {
    public TreeRootSettings {
        if (rootCount < 4 || rootCount > 8) throw new IllegalArgumentException("rootCount must be in [4, 8]");
        range(flareMultiplier, 1, 3, "flareMultiplier");
        range(lengthRatio, 0.1, 2, "lengthRatio");
        range(exposedFraction, 0, 1, "exposedFraction");
        range(curvature, -1, 1, "curvature");
        range(gnarliness, 0, 0.5, "gnarliness");
        range(gnarlinessFrequency, 0, 12, "gnarlinessFrequency");
        if (ringCount < 2 || ringCount > 64) throw new IllegalArgumentException("ringCount must be in [2, 64]");
        if (verticesPerRing < 8 || verticesPerRing > 32) throw new IllegalArgumentException("verticesPerRing must be in [8, 32]");
    }

    public TreeRootSettings(int rootCount, double flareMultiplier, double lengthRatio, double exposedFraction,
                            int ringCount, int verticesPerRing) {
        this(rootCount, flareMultiplier, lengthRatio, exposedFraction, .25, .12, 2, ringCount, verticesPerRing);
    }

    private static void range(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) throw new IllegalArgumentException(name + " is out of range");
    }
}
