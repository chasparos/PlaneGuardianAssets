package com.planeguardian.assets.generation.surfaces;

import com.planeguardian.assets.generation.api.Vector3;

import java.util.List;
import java.util.Objects;

/** Rational tensor-product B-spline surface evaluated in homogeneous coordinates. */
public final class NurbsSurface implements ParametricSurface3 {
    private final int degreeU;
    private final int degreeV;
    private final List<List<Vector3>> controlRows;
    private final double[][] weights;
    private final double[] knotsU;
    private final double[] knotsV;

    public NurbsSurface(int degreeU, int degreeV, List<List<Vector3>> controlRows,
                        double[][] weights, double[] knotsU, double[] knotsV) {
        if (degreeU < 1 || degreeV < 1) throw new IllegalArgumentException("NURBS degrees must be positive");
        Objects.requireNonNull(controlRows, "controlRows");
        Objects.requireNonNull(weights, "weights");
        Objects.requireNonNull(knotsU, "knotsU");
        Objects.requireNonNull(knotsV, "knotsV");
        if (controlRows.isEmpty()) throw new IllegalArgumentException("Control grid must not be empty");
        this.controlRows = controlRows.stream().map(List::copyOf).toList();
        if (this.controlRows.size() < degreeV + 1) throw new IllegalArgumentException("Not enough V control rows");
        int columns = this.controlRows.get(0).size();
        if (columns < degreeU + 1) throw new IllegalArgumentException("Not enough U control columns");
        if (this.controlRows.stream().anyMatch(row -> row.size() != columns)) {
            throw new IllegalArgumentException("Control grid must be rectangular");
        }
        if (weights.length != this.controlRows.size()) throw new IllegalArgumentException("One weight row is required per control row");
        this.weights = new double[weights.length][];
        for (int row = 0; row < weights.length; row++) {
            if (weights[row].length != columns) throw new IllegalArgumentException("Weight grid must match control grid");
            this.weights[row] = weights[row].clone();
            for (double weight : this.weights[row]) {
                if (!Double.isFinite(weight) || weight <= 0) throw new IllegalArgumentException("NURBS weights must be finite and positive");
            }
        }
        this.degreeU = degreeU;
        this.degreeV = degreeV;
        this.knotsU = validateKnots(knotsU, columns + degreeU + 1, "U");
        this.knotsV = validateKnots(knotsV, this.controlRows.size() + degreeV + 1, "V");
        requirePositiveDomain(this.knotsU, degreeU, columns, "U");
        requirePositiveDomain(this.knotsV, degreeV, this.controlRows.size(), "V");
    }

    @Override
    public Vector3 position(double u, double v) {
        ParametricSurface3.requireParameters(u, v);
        double actualU = map(u, knotsU[degreeU], knotsU[controlRows.get(0).size()]);
        double actualV = map(v, knotsV[degreeV], knotsV[controlRows.size()]);
        double[][] rowValues = new double[controlRows.size()][4];
        for (int row = 0; row < controlRows.size(); row++) {
            double[][] homogeneous = new double[controlRows.get(row).size()][4];
            for (int column = 0; column < homogeneous.length; column++) {
                Vector3 point = controlRows.get(row).get(column);
                double weight = weights[row][column];
                homogeneous[column] = new double[]{point.x() * weight, point.y() * weight, point.z() * weight, weight};
            }
            rowValues[row] = deBoor(actualU, degreeU, knotsU, homogeneous);
        }
        double[] result = deBoor(actualV, degreeV, knotsV, rowValues);
        if (StrictMath.abs(result[3]) <= 1.0e-15) throw new IllegalStateException("NURBS surface produced zero homogeneous weight");
        return new Vector3(result[0] / result[3], result[1] / result[3], result[2] / result[3]);
    }

    private static double[] deBoor(double parameter, int degree, double[] knots, double[][] controls) {
        int span = findSpan(parameter, degree, knots, controls.length);
        double[][] values = new double[degree + 1][4];
        for (int index = 0; index <= degree; index++) values[index] = controls[span - degree + index].clone();
        for (int level = 1; level <= degree; level++) {
            for (int index = degree; index >= level; index--) {
                int controlIndex = span - degree + index;
                double denominator = knots[controlIndex + degree - level + 1] - knots[controlIndex];
                double alpha = denominator == 0 ? 0 : (parameter - knots[controlIndex]) / denominator;
                for (int component = 0; component < 4; component++) {
                    values[index][component] = (1 - alpha) * values[index - 1][component] + alpha * values[index][component];
                }
            }
        }
        return values[degree];
    }

    private static int findSpan(double parameter, int degree, double[] knots, int controlCount) {
        int last = controlCount - 1;
        if (parameter >= knots[controlCount]) return last;
        int low = degree;
        int high = controlCount;
        int middle = (low + high) >>> 1;
        while (parameter < knots[middle] || parameter >= knots[middle + 1]) {
            if (parameter < knots[middle]) high = middle; else low = middle;
            middle = (low + high) >>> 1;
        }
        return middle;
    }

    private static double[] validateKnots(double[] knots, int expected, String axis) {
        if (knots.length != expected) throw new IllegalArgumentException("Invalid " + axis + " knot vector length");
        double[] copy = knots.clone();
        for (int index = 0; index < copy.length; index++) {
            if (!Double.isFinite(copy[index]) || index > 0 && copy[index] < copy[index - 1]) {
                throw new IllegalArgumentException(axis + " knot vector must be finite and nondecreasing");
            }
        }
        return copy;
    }

    private static void requirePositiveDomain(double[] knots, int degree, int controls, String axis) {
        if (knots[controls] <= knots[degree]) throw new IllegalArgumentException(axis + " domain must have positive length");
    }

    private static double map(double normalized, double start, double end) {
        return start + normalized * (end - start);
    }
}
