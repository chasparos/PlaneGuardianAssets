package com.planeguardian.assets.generation.surface;

/** Four-component tangent where w is the bitangent handedness sign. */
public record Vector4(double x, double y, double z, double w) {
    public Vector4 {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || !Double.isFinite(w)) {
            throw new IllegalArgumentException("Vector components must be finite");
        }
        if (w != -1.0 && w != 1.0) throw new IllegalArgumentException("Tangent handedness must be -1 or 1");
    }
}
