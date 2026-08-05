package com.planeguardian.assets.generation.geometry.tube;

@FunctionalInterface
public interface CrossSectionProfile {
    /** Positive radial multiplier at arc and angular fractions in [0, 1]. */
    double multiplierAt(double arcFraction, double angleFraction);

    static CrossSectionProfile circular() {
        return (arcFraction, angleFraction) -> 1.0;
    }

    static CrossSectionProfile faceted(int facetCount, double sharpness) {
        if (facetCount < 3) throw new IllegalArgumentException("facetCount must be at least 3");
        if (!Double.isFinite(sharpness) || sharpness < 0 || sharpness > 1) throw new IllegalArgumentException("sharpness must be in [0, 1]");
        return (arcFraction, angleFraction) -> {
            double phase = angleFraction * facetCount;
            double edge = Math.abs((phase - Math.floor(phase)) - 0.5) * 2;
            return 1.0 - edge * sharpness * 0.35;
        };
    }
}
