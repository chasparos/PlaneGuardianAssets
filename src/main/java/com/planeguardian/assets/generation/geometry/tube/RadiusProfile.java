package com.planeguardian.assets.generation.geometry.tube;

@FunctionalInterface
public interface RadiusProfile {
    /** Radius in metres at normalized arc length. */
    double radiusAt(double arcFraction);
}
