package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;

import java.util.Objects;

public record CubicHermiteCurve(Vector3 start, Vector3 startTangent, Vector3 end, Vector3 endTangent)
        implements ParametricCurve3 {
    public CubicHermiteCurve {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(startTangent, "startTangent");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(endTangent, "endTangent");
    }

    @Override
    public Vector3 position(double parameter) {
        ParametricCurve3.requireParameter(parameter);
        double t2 = parameter * parameter;
        double t3 = t2 * parameter;
        double h00 = 2 * t3 - 3 * t2 + 1;
        double h10 = t3 - 2 * t2 + parameter;
        double h01 = -2 * t3 + 3 * t2;
        double h11 = t3 - t2;
        return combine(h00, start, h10, startTangent, h01, end, h11, endTangent);
    }

    @Override
    public Vector3 derivative(double parameter) {
        ParametricCurve3.requireParameter(parameter);
        double t2 = parameter * parameter;
        return combine(6 * t2 - 6 * parameter, start,
                3 * t2 - 4 * parameter + 1, startTangent,
                -6 * t2 + 6 * parameter, end,
                3 * t2 - 2 * parameter, endTangent);
    }

    private static Vector3 combine(double a, Vector3 av, double b, Vector3 bv,
                                   double c, Vector3 cv, double d, Vector3 dv) {
        return new Vector3(
                a * av.x() + b * bv.x() + c * cv.x() + d * dv.x(),
                a * av.y() + b * bv.y() + c * cv.y() + d * dv.y(),
                a * av.z() + b * bv.z() + c * cv.z() + d * dv.z());
    }
}
