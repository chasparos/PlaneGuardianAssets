package com.planeguardian.assets.generation.semantics;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/** Immutable source Lore Map values supplied to centralized semantic resolution. */
public record SemanticProfile(ContractVersion schemaVersion, SortedMap<StableId, SemanticWheelValue> wheels) {
    public SemanticProfile {
        Objects.requireNonNull(schemaVersion, "schemaVersion"); Objects.requireNonNull(wheels, "wheels");
        wheels = Collections.unmodifiableSortedMap(new TreeMap<>(wheels));
    }
}
