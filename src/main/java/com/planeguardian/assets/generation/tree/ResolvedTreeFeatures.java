package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.Contribution;

import java.util.List;
import java.util.Objects;

/** Bounded resolved crown and feature suitability with inspectable semantic contributions. */
public record ResolvedTreeFeatures(
        TreeCrownSettings crown,
        double mossCoverage,
        double flowerDensity,
        double fruitDensity,
        List<Contribution> contributions) {
    public ResolvedTreeFeatures {
        Objects.requireNonNull(crown, "crown");
        unit(mossCoverage, "mossCoverage");
        unit(flowerDensity, "flowerDensity");
        unit(fruitDensity, "fruitDensity");
        contributions = List.copyOf(contributions);
    }

    private static void unit(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException(name + " must be in [0, 1]");
        }
    }
}
