package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Map;
import java.util.Objects;

/** Immutable crown output separate from structural topology and renderer adapters. */
public record TreeCrownProduct(Map<StableId, TreeCrownPart> parts, ReproducibilityFingerprint fingerprint) {
    public TreeCrownProduct {
        parts = Map.copyOf(parts);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
