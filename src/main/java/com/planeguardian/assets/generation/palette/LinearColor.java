package com.planeguardian.assets.generation.palette;

/** Engine-neutral linear RGBA color. */
public record LinearColor(float r, float g, float b, float a) {
    public LinearColor {
        require(r, "r"); require(g, "g"); require(b, "b"); require(a, "a");
    }

    private static void require(float value, String name) {
        if (!Float.isFinite(value) || value < 0f || value > 1f) throw new IllegalArgumentException(name + " must be finite in [0, 1]");
    }
}
