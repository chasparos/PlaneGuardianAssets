package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.math.VectorMath;

import java.util.Objects;

/** Endpoint-preserving, deterministic multi-axis gnarliness for any base curve. */
public record HarmonicPerturbedCurve(ParametricCurve3 base, Vector3 axisA, Vector3 axisB,
                                     double amplitude, double frequency, double phase) implements ParametricCurve3 {
    public HarmonicPerturbedCurve {
        Objects.requireNonNull(base, "base");
        axisA = VectorMath.normalize(Objects.requireNonNull(axisA, "axisA"));
        axisB = VectorMath.normalize(Objects.requireNonNull(axisB, "axisB"));
        if (!Double.isFinite(amplitude) || amplitude < 0) throw new IllegalArgumentException("amplitude must be finite and non-negative");
        if (!Double.isFinite(frequency) || frequency < 0) throw new IllegalArgumentException("frequency must be finite and non-negative");
        if (!Double.isFinite(phase)) throw new IllegalArgumentException("phase must be finite");
    }

    @Override public Vector3 position(double t) {
        ParametricCurve3.requireParameter(t);
        double envelope = StrictMath.sin(StrictMath.PI * t);
        double angle = StrictMath.PI * 2 * frequency * t + phase;
        Vector3 offset = VectorMath.add(VectorMath.scale(axisA, StrictMath.sin(angle)),
                VectorMath.scale(axisB, .55 * StrictMath.sin(angle * 1.73 + 1.1)));
        return VectorMath.add(base.position(t), VectorMath.scale(offset, amplitude * envelope));
    }
}
