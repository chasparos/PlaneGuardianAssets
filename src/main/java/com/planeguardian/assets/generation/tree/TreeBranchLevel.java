package com.planeguardian.assets.generation.tree;

/** One recursive branch level's bounded direct structural controls. */
public record TreeBranchLevel(
        int maximumChildren,
        double attachmentStart,
        double attachmentEnd,
        double lengthRatio,
        double radiusRatio,
        double elevation,
        int ringCount,
        int verticesPerRing) {
    public TreeBranchLevel {
        if (maximumChildren < 1 || maximumChildren > 32) throw new IllegalArgumentException("maximumChildren must be in [1, 32]");
        range(attachmentStart, 0, 1, "attachmentStart");
        range(attachmentEnd, attachmentStart, 1, "attachmentEnd");
        range(lengthRatio, 0.05, 1, "lengthRatio");
        range(radiusRatio, 0.02, 0.8, "radiusRatio");
        range(elevation, -0.5, 1, "elevation");
        if (ringCount < 2 || ringCount > 64) throw new IllegalArgumentException("ringCount must be in [2, 64]");
        if (verticesPerRing < 8 || verticesPerRing > 32) throw new IllegalArgumentException("verticesPerRing must be in [8, 32]");
    }

    private static void range(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be in [" + minimum + ", " + maximum + "]");
        }
    }
}
