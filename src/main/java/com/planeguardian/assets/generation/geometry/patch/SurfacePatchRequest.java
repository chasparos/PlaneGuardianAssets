package com.planeguardian.assets.generation.geometry.patch;

import com.planeguardian.assets.generation.surfaces.ParametricSurface3;

import java.util.Objects;
import java.util.Set;

public record SurfacePatchRequest(
        ParametricSurface3 surface,
        int segmentsU,
        int segmentsV,
        Set<String> semanticGroups) {
    public SurfacePatchRequest {
        Objects.requireNonNull(surface, "surface");
        if (segmentsU < 1 || segmentsV < 1) throw new IllegalArgumentException("Patch segment counts must be positive");
        semanticGroups = Set.copyOf(semanticGroups);
    }
}
