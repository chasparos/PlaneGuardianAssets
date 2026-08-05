package com.planeguardian.assets.tools.generator;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/** Generic workbench input passed to a registered authoring provider. */
public record AuthoringGenerationRequest(String assetName, long visualSeed,
                                         SortedMap<String, String> directParameters,
                                         com.planeguardian.assets.generation.semantics.SemanticProfile sourceSemantics,
                                         java.util.Set<String> explicitOverrides) {
    public AuthoringGenerationRequest {
        if (assetName == null || assetName.isBlank()) throw new IllegalArgumentException("assetName must not be blank");
        directParameters = Collections.unmodifiableSortedMap(new TreeMap<>(directParameters));
        if (sourceSemantics == null) throw new NullPointerException("sourceSemantics");
        explicitOverrides = java.util.Set.copyOf(explicitOverrides);
    }
}
