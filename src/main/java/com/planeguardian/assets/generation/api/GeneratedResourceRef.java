package com.planeguardian.assets.generation.api;

import java.util.Objects;

/** Reference to a generated or library-owned resource, independent of storage format. */
public record GeneratedResourceRef(StableId resourceId, ResourceKind kind, ContractVersion version) {
    public GeneratedResourceRef {
        Objects.requireNonNull(resourceId, "resourceId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(version, "version");
    }
}
