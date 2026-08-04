package com.planeguardian.assets.generation.api;

import java.util.List;
import java.util.Objects;

/** Engine-neutral description of a complete generated asset. */
public record GeneratedAsset(
        StableId assetId,
        List<GeneratedComponent> components,
        List<GeneratedSocket> sockets,
        List<Contribution> contributionTrace,
        List<GenerationDiagnostic> diagnostics,
        ReproducibilityFingerprint fingerprint) {

    public GeneratedAsset {
        Objects.requireNonNull(assetId, "assetId");
        components = List.copyOf(components);
        sockets = List.copyOf(sockets);
        contributionTrace = List.copyOf(contributionTrace);
        diagnostics = List.copyOf(diagnostics);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
