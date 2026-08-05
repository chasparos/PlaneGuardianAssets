package com.planeguardian.assets.runtime;

import com.planeguardian.assets.generation.api.Vector3;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/** Mutable scene inputs deliberately excluded from semantic identity. */
public record EnvironmentState(double elapsedSeconds, Vector3 windDirection, double windIntensity,
                               SortedMap<String, Double> weather) {
    public EnvironmentState {
        if (!Double.isFinite(elapsedSeconds) || elapsedSeconds < 0) throw new IllegalArgumentException("elapsedSeconds must be finite and non-negative");
        if (windDirection == null) throw new NullPointerException("windDirection");
        if (!Double.isFinite(windIntensity) || windIntensity < 0) throw new IllegalArgumentException("windIntensity must be finite and non-negative");
        TreeMap<String, Double> copy = new TreeMap<>();
        weather.forEach((key, value) -> {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("weather ID must not be blank");
            if (value == null || !Double.isFinite(value)) throw new IllegalArgumentException("weather value must be finite");
            copy.put(key, value);
        });
        weather = Collections.unmodifiableSortedMap(copy);
    }
}
