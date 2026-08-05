package com.planeguardian.assets.generation.authoring;

import com.planeguardian.assets.generation.api.GeneratedAsset;
import com.planeguardian.assets.generation.api.GenerationDiagnostic;
import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/** Immutable snapshot of one generic asset-authoring workflow. */
public record AuthoringSession(GeneratorDescriptor generator, long visualSeed,
        SortedMap<String, String> directParameters, SemanticProfile sourceSemantics,
        Optional<ResolvedVisualProfile> resolvedSemantics, Optional<GeneratedAsset> generatedAsset,
        PreviewState previewState, List<GenerationDiagnostic> diagnostics) {
    public AuthoringSession {
        Objects.requireNonNull(generator); directParameters = Collections.unmodifiableSortedMap(new TreeMap<>(directParameters));
        Objects.requireNonNull(sourceSemantics); Objects.requireNonNull(resolvedSemantics); Objects.requireNonNull(generatedAsset);
        Objects.requireNonNull(previewState); diagnostics = List.copyOf(diagnostics);
        if (previewState == PreviewState.READY && generatedAsset.isEmpty()) throw new IllegalArgumentException("Ready preview requires a generated asset");
    }
    public enum PreviewState { EMPTY, DIRTY, GENERATING, READY, FAILED }
}
