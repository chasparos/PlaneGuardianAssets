package com.planeguardian.assets.generation.tree;

/** Resolved, engine-neutral semantic channels relevant to the first tree crown pass. */
public record TreeSemanticProfile(
        double vitality,
        double regularity,
        double transformation,
        double genesis,
        double water,
        double fire) {
    public TreeSemanticProfile {
        signed(vitality, "vitality");
        signed(regularity, "regularity");
        signed(transformation, "transformation");
        signed(genesis, "genesis");
        unit(water, "water");
        unit(fire, "fire");
    }

    public static TreeSemanticProfile ordinary() {
        return new TreeSemanticProfile(0, 0, 0, 0, 0, 0);
    }

    private static void signed(double value, String name) {
        if (!Double.isFinite(value) || value < -1 || value > 1) {
            throw new IllegalArgumentException(name + " must be in [-1, 1]");
        }
    }

    private static void unit(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException(name + " must be in [0, 1]");
        }
    }
}
