package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable structural output before material, engine, and export adapters consume it. */
public record TreeStructuralProduct(
        ProtoMeshSnapshot trunk,
        Map<StableId, TreeStructuralPart> parts,
        List<GeneratedSocket> sockets,
        List<String> diagnostics,
        ReproducibilityFingerprint fingerprint) {
    public TreeStructuralProduct {
        Objects.requireNonNull(trunk, "trunk");
        parts = Map.copyOf(parts);
        sockets = List.copyOf(sockets);
        diagnostics = List.copyOf(diagnostics);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }

    public TreeStructuralProduct(ProtoMeshSnapshot trunk, ReproducibilityFingerprint fingerprint) {
        this(trunk, Map.of(), List.of(), List.of(), fingerprint);
    }
}
