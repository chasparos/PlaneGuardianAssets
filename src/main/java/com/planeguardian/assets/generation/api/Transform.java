package com.planeguardian.assets.generation.api;

import java.util.Objects;

public record Transform(Vector3 translation, Rotation rotation, Vector3 scale) {
    public static final Transform IDENTITY = new Transform(Vector3.ZERO, Rotation.IDENTITY, Vector3.ONE);

    public Transform {
        Objects.requireNonNull(translation, "translation");
        Objects.requireNonNull(rotation, "rotation");
        Objects.requireNonNull(scale, "scale");
        if (scale.x() <= 0 || scale.y() <= 0 || scale.z() <= 0) {
            throw new IllegalArgumentException("Generated transforms require positive scale");
        }
    }
}
