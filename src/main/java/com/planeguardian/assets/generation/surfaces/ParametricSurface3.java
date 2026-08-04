package com.planeguardian.assets.generation.surfaces;

import com.planeguardian.assets.generation.api.Vector3;

/** Engine-neutral surface over the normalized unit-square parameter domain. */
@FunctionalInterface
public interface ParametricSurface3 {
    Vector3 position(double u, double v);

    static void requireParameters(double u, double v) {
        if (!Double.isFinite(u) || !Double.isFinite(v) || u < 0 || u > 1 || v < 0 || v > 1) {
            throw new IllegalArgumentException("Surface parameters must be finite and in [0, 1]");
        }
    }
}
