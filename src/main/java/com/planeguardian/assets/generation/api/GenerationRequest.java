package com.planeguardian.assets.generation.api;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/** Immutable engine-neutral input to a procedural asset provider. */
public record GenerationRequest(
        StableId assetId,
        StableId generatorId,
        long visualSeed,
        GenerationVersions versions,
        RenderTier renderTier,
        SortedMap<String, String> parameters) {

    public GenerationRequest {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(generatorId, "generatorId");
        Objects.requireNonNull(versions, "versions");
        Objects.requireNonNull(renderTier, "renderTier");
        Objects.requireNonNull(parameters, "parameters");
        TreeMap<String, String> copy = new TreeMap<>();
        parameters.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "parameter key"),
                Objects.requireNonNull(value, "parameter value")));
        parameters = Collections.unmodifiableSortedMap(copy);
    }
}
