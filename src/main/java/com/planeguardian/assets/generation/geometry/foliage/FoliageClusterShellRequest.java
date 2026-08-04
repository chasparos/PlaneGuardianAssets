package com.planeguardian.assets.generation.geometry.foliage;

import com.planeguardian.assets.generation.api.Vector3;

import java.util.Objects;
import java.util.Set;

/** Bounded engine-neutral ellipsoidal shell controls for foliage and other clustered masses. */
public record FoliageClusterShellRequest(
        Vector3 center,
        Vector3 radii,
        int latitudeBands,
        int radialSegments,
        Set<String> semanticGroups) {
    public FoliageClusterShellRequest {
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(radii, "radii");
        if (radii.x() <= 0 || radii.y() <= 0 || radii.z() <= 0) {
            throw new IllegalArgumentException("radii must be positive");
        }
        if (latitudeBands < 2 || latitudeBands > 32) {
            throw new IllegalArgumentException("latitudeBands must be in [2, 32]");
        }
        if (radialSegments < 8 || radialSegments > 64) {
            throw new IllegalArgumentException("radialSegments must be in [8, 64]");
        }
        semanticGroups = Set.copyOf(semanticGroups);
        if (semanticGroups.isEmpty()) {
            throw new IllegalArgumentException("semanticGroups must not be empty");
        }
    }
}
