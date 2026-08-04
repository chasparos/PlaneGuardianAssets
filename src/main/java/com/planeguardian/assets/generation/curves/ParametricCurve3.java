package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.math.VectorMath;

/** Three-dimensional curve over the normalized domain [0, 1]. */
public interface ParametricCurve3 {
    Vector3 position(double parameter);

    default Vector3 derivative(double parameter) {
        requireParameter(parameter);
        double step = 1.0e-6;
        if (parameter <= step) {
            return VectorMath.scale(VectorMath.subtract(position(parameter + step), position(parameter)), 1.0 / step);
        }
        if (parameter >= 1.0 - step) {
            return VectorMath.scale(VectorMath.subtract(position(parameter), position(parameter - step)), 1.0 / step);
        }
        return VectorMath.scale(VectorMath.subtract(position(parameter + step), position(parameter - step)), 0.5 / step);
    }

    static void requireParameter(double parameter) {
        if (!Double.isFinite(parameter) || parameter < 0 || parameter > 1) {
            throw new IllegalArgumentException("Curve parameter must be finite and in [0, 1]");
        }
    }
}
