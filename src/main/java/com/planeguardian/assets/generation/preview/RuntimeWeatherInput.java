package com.planeguardian.assets.generation.preview;

import com.planeguardian.assets.generation.api.Vector3;

import java.util.Objects;

/** Mutable-scene weather input, intentionally separate from generated semantic state. */
public record RuntimeWeatherInput(Vector3 windDirection, double intensity, double elapsedSeconds) {
    public RuntimeWeatherInput {
        Objects.requireNonNull(windDirection, "windDirection");
        if (!Double.isFinite(intensity) || intensity < 0 || intensity > 1) {
            throw new IllegalArgumentException("intensity must be in [0, 1]");
        }
        if (!Double.isFinite(elapsedSeconds) || elapsedSeconds < 0) {
            throw new IllegalArgumentException("elapsedSeconds must be finite and non-negative");
        }
        double length = Math.sqrt(windDirection.x() * windDirection.x()
                + windDirection.y() * windDirection.y() + windDirection.z() * windDirection.z());
        if (intensity > 0 && length == 0) {
            throw new IllegalArgumentException("windDirection must be non-zero when intensity is positive");
        }
        windDirection = length == 0 ? Vector3.ZERO
                : new Vector3(windDirection.x() / length, windDirection.y() / length, windDirection.z() / length);
    }

    public static RuntimeWeatherInput calm() {
        return new RuntimeWeatherInput(Vector3.ZERO, 0, 0);
    }
}
