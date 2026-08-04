package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;

import java.util.List;

/** Rational B-spline evaluated by homogeneous De Boor interpolation. */
public final class NurbsCurve implements ParametricCurve3 {
    private final int degree;
    private final List<Vector3> controlPoints;
    private final double[] weights;
    private final double[] knots;

    public NurbsCurve(int degree, List<Vector3> controlPoints, double[] weights, double[] knots) {
        if (degree < 1) throw new IllegalArgumentException("NURBS degree must be positive");
        this.controlPoints = List.copyOf(controlPoints);
        if (this.controlPoints.size() < degree + 1) throw new IllegalArgumentException("Not enough control points for degree");
        if (weights.length != this.controlPoints.size()) throw new IllegalArgumentException("One weight is required per control point");
        if (knots.length != this.controlPoints.size() + degree + 1) throw new IllegalArgumentException("Invalid knot vector length");
        this.weights = weights.clone();
        this.knots = knots.clone();
        this.degree = degree;
        for (double weight : this.weights) {
            if (!Double.isFinite(weight) || weight <= 0) throw new IllegalArgumentException("NURBS weights must be finite and positive");
        }
        for (int index = 0; index < this.knots.length; index++) {
            if (!Double.isFinite(this.knots[index]) || index > 0 && this.knots[index] < this.knots[index - 1]) {
                throw new IllegalArgumentException("Knot vector must be finite and nondecreasing");
            }
        }
        if (domainEnd() <= domainStart()) throw new IllegalArgumentException("NURBS domain must have positive length");
    }

    @Override
    public Vector3 position(double parameter) {
        ParametricCurve3.requireParameter(parameter);
        double u = domainStart() + parameter * (domainEnd() - domainStart());
        int span = findSpan(u);
        double[][] values = new double[degree + 1][4];
        for (int j = 0; j <= degree; j++) {
            int pointIndex = span - degree + j;
            Vector3 point = controlPoints.get(pointIndex);
            double weight = weights[pointIndex];
            values[j] = new double[]{point.x() * weight, point.y() * weight, point.z() * weight, weight};
        }
        for (int level = 1; level <= degree; level++) {
            for (int j = degree; j >= level; j--) {
                int pointIndex = span - degree + j;
                double denominator = knots[pointIndex + degree - level + 1] - knots[pointIndex];
                double alpha = denominator == 0 ? 0 : (u - knots[pointIndex]) / denominator;
                for (int component = 0; component < 4; component++) {
                    values[j][component] = (1 - alpha) * values[j - 1][component] + alpha * values[j][component];
                }
            }
        }
        double[] result = values[degree];
        if (StrictMath.abs(result[3]) <= 1.0e-15) throw new IllegalStateException("NURBS evaluation produced zero homogeneous weight");
        return new Vector3(result[0] / result[3], result[1] / result[3], result[2] / result[3]);
    }

    private int findSpan(double u) {
        int lastPoint = controlPoints.size() - 1;
        if (u >= domainEnd()) return lastPoint;
        int low = degree;
        int high = lastPoint + 1;
        int middle = (low + high) >>> 1;
        while (u < knots[middle] || u >= knots[middle + 1]) {
            if (u < knots[middle]) high = middle; else low = middle;
            middle = (low + high) >>> 1;
        }
        return middle;
    }

    private double domainStart() { return knots[degree]; }
    private double domainEnd() { return knots[controlPoints.size()]; }
}
