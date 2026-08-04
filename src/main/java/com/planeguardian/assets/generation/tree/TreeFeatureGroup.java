package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.StableId;

/** Stable independently admitted tree feature surfaces. */
public enum TreeFeatureGroup {
    MOSS("tree.moss"),
    VINES("tree.vines"),
    FLOWERS("tree.flowers"),
    FRUIT("tree.fruit"),
    FUNGI("tree.fungi");

    private final StableId role;

    TreeFeatureGroup(String role) {
        this.role = new StableId(role);
    }

    public StableId role() {
        return role;
    }

    public double suitability(ResolvedTreeFeatures features) {
        return switch (this) {
            case MOSS -> features.mossCoverage();
            case VINES -> features.vineCoverage();
            case FLOWERS -> features.flowerDensity();
            case FRUIT -> features.fruitDensity();
            case FUNGI -> features.fungalCoverage();
        };
    }
}
