package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.math.VectorMath;

import java.util.Arrays;
import java.util.Objects;

/** Deterministic sampled inverse mapping from distance to curve parameter. */
public final class ArcLengthTable {
    private final ParametricCurve3 curve;
    private final double[] cumulativeLengths;

    public ArcLengthTable(ParametricCurve3 curve, int sampleCount) {
        this.curve = Objects.requireNonNull(curve, "curve");
        if (sampleCount < 2) throw new IllegalArgumentException("Arc-length table needs at least two samples");
        cumulativeLengths = new double[sampleCount];
        var previous = curve.position(0);
        for (int index = 1; index < sampleCount; index++) {
            double parameter = index / (double) (sampleCount - 1);
            var current = curve.position(parameter);
            cumulativeLengths[index] = cumulativeLengths[index - 1] + VectorMath.distance(previous, current);
            previous = current;
        }
        if (totalLength() <= 1.0e-12) throw new IllegalArgumentException("Curve must have measurable length");
    }

    public double totalLength() { return cumulativeLengths[cumulativeLengths.length - 1]; }

    public double parameterAtDistance(double distance) {
        if (!Double.isFinite(distance) || distance < 0 || distance > totalLength()) {
            throw new IllegalArgumentException("Distance must be in the sampled curve range");
        }
        if (distance == 0) return 0;
        if (distance == totalLength()) return 1;
        int found = Arrays.binarySearch(cumulativeLengths, distance);
        if (found >= 0) return found / (double) (cumulativeLengths.length - 1);
        int upper = -found - 1;
        int lower = upper - 1;
        double local = (distance - cumulativeLengths[lower])
                / (cumulativeLengths[upper] - cumulativeLengths[lower]);
        return (lower + local) / (cumulativeLengths.length - 1);
    }

    public double parameterAtFraction(double fraction) {
        if (!Double.isFinite(fraction) || fraction < 0 || fraction > 1) {
            throw new IllegalArgumentException("Arc fraction must be in [0, 1]");
        }
        return parameterAtDistance(fraction * totalLength());
    }

    public ParametricCurve3 curve() { return curve; }
    public int sampleCount() { return cumulativeLengths.length; }
}
