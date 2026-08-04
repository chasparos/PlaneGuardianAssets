package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;

import java.util.Objects;

/** Immutable structural output before material, engine, and export adapters consume it. */
public record TreeStructuralProduct(ProtoMeshSnapshot trunk, ReproducibilityFingerprint fingerprint) {
    public TreeStructuralProduct {
        Objects.requireNonNull(trunk, "trunk");
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
