package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Map;
import java.util.Objects;

/** Deterministic bounded semantic feature admissions separate from feature geometry. */
public record TreeFeatureProduct(Map<StableId, TreeFeaturePlacement> placements,
                                 ReproducibilityFingerprint fingerprint) {
    public TreeFeatureProduct {
        placements = Map.copyOf(placements);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
