package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Map;
import java.util.Objects;

/** Immutable mesh products derived from independently admitted semantic tree features. */
public record TreeFeatureGeometryProduct(Map<StableId, TreeFeatureGeometryPart> parts,
                                         ReproducibilityFingerprint fingerprint) {
    public TreeFeatureGeometryProduct {
        parts = Map.copyOf(parts);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
