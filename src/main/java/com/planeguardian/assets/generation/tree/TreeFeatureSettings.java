package com.planeguardian.assets.generation.tree;

/** Bounded direct controls for semantic tree feature admission. */
public record TreeFeatureSettings(
        int maximumMoss,
        int maximumVines,
        int maximumFlowers,
        int maximumFruit,
        int maximumFungi,
        int maximumFeatures) {
    public TreeFeatureSettings {
        count(maximumMoss, "maximumMoss");
        count(maximumVines, "maximumVines");
        count(maximumFlowers, "maximumFlowers");
        count(maximumFruit, "maximumFruit");
        if (maximumFeatures < 0 || maximumFeatures > 96) {
            throw new IllegalArgumentException("maximumFeatures must be in [0, 96]");
        }
    }

    public static TreeFeatureSettings defaults() {
        return new TreeFeatureSettings(8, 6, 12, 10, 6, 24);
    }

    public int maximumFor(TreeFeatureGroup group) {
        return switch (group) {
            case MOSS -> maximumMoss;
            case VINES -> maximumVines;
            case FLOWERS -> maximumFlowers;
            case FRUIT -> maximumFruit;
            case FUNGI -> maximumFungi;
        };
    }

    private static void count(int value, String name) {
        if (value < 0 || value > 32) {
            throw new IllegalArgumentException(name + " must be in [0, 32]");
        }
    }
}
