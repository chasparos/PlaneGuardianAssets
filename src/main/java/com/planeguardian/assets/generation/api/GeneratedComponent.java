package com.planeguardian.assets.generation.api;

import java.util.List;
import java.util.Objects;

/** One semantic component in a generated asset description. */
public record GeneratedComponent(
        StableId componentId,
        StableId role,
        Transform transform,
        List<GeneratedResourceRef> resources) {

    public GeneratedComponent {
        Objects.requireNonNull(componentId, "componentId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(transform, "transform");
        resources = List.copyOf(resources);
    }
}
