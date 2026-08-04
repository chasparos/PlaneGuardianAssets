package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.math.VectorMath;

import java.util.List;

/** Multi-segment Catmull–Rom spline with uniform, centripetal, or chordal knots. */
public final class CatmullRomSpline implements ParametricCurve3 {
    public enum Parameterization {
        UNIFORM(0), CENTRIPETAL(0.5), CHORDAL(1);
        private final double alpha;
        Parameterization(double alpha) { this.alpha = alpha; }
    }

    private final List<Vector3> points;
    private final Parameterization parameterization;

    public CatmullRomSpline(List<Vector3> points, Parameterization parameterization) {
        this.points = List.copyOf(points);
        if (this.points.size() < 4) throw new IllegalArgumentException("Catmull-Rom needs at least four control points");
        this.parameterization = java.util.Objects.requireNonNull(parameterization, "parameterization");
        if (parameterization != Parameterization.UNIFORM) {
            for (int index = 1; index < points.size(); index++) {
                if (VectorMath.distance(points.get(index - 1), points.get(index)) <= 1.0e-12) {
                    throw new IllegalArgumentException("Non-uniform Catmull-Rom control points must be distinct");
                }
            }
        }
    }

    @Override
    public Vector3 position(double parameter) {
        ParametricCurve3.requireParameter(parameter);
        int segmentCount = points.size() - 3;
        double scaled = parameter * segmentCount;
        int segment = parameter == 1 ? segmentCount - 1 : (int) StrictMath.floor(scaled);
        double local = parameter == 1 ? 1 : scaled - segment;
        Vector3 p0 = points.get(segment);
        Vector3 p1 = points.get(segment + 1);
        Vector3 p2 = points.get(segment + 2);
        Vector3 p3 = points.get(segment + 3);
        double t0 = 0;
        double t1 = knot(t0, p0, p1);
        double t2 = knot(t1, p1, p2);
        double t3 = knot(t2, p2, p3);
        double t = t1 + local * (t2 - t1);
        Vector3 a1 = interpolate(p0, p1, t0, t1, t);
        Vector3 a2 = interpolate(p1, p2, t1, t2, t);
        Vector3 a3 = interpolate(p2, p3, t2, t3, t);
        Vector3 b1 = interpolate(a1, a2, t0, t2, t);
        Vector3 b2 = interpolate(a2, a3, t1, t3, t);
        return interpolate(b1, b2, t1, t2, t);
    }

    private double knot(double previous, Vector3 a, Vector3 b) {
        if (parameterization == Parameterization.UNIFORM) return previous + 1;
        return previous + StrictMath.pow(VectorMath.distance(a, b), parameterization.alpha);
    }

    private static Vector3 interpolate(Vector3 a, Vector3 b, double ta, double tb, double t) {
        double ratio = (t - ta) / (tb - ta);
        return VectorMath.add(VectorMath.scale(a, 1 - ratio), VectorMath.scale(b, ratio));
    }
}
