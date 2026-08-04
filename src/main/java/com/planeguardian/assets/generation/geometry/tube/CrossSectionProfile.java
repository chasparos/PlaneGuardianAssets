package com.planeguardian.assets.generation.geometry.tube;

@FunctionalInterface
public interface CrossSectionProfile {
    /** Positive radial multiplier at arc and angular fractions in [0, 1]. */
    double multiplierAt(double arcFraction, double angleFraction);

    static CrossSectionProfile circular() {
        return (arcFraction, angleFraction) -> 1.0;
    }
}
