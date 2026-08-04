package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurveImplementationsTest {
    @Test
    void hermiteHonorsEndpointPositionsAndTangents() {
        CubicHermiteCurve curve = new CubicHermiteCurve(
                new Vector3(0, 0, 0), new Vector3(1, 2, 0),
                new Vector3(3, 0, 0), new Vector3(1, -2, 0));

        assertVector(curve.position(0), 0, 0, 0);
        assertVector(curve.position(1), 3, 0, 0);
        assertVector(curve.derivative(0), 1, 2, 0);
        assertVector(curve.derivative(1), 1, -2, 0);
        assertThrows(IllegalArgumentException.class, () -> curve.position(1.01));
    }

    @Test
    void catmullRomInterpolatesInteriorControlPointsForAllKnotPolicies() {
        List<Vector3> controls = List.of(
                new Vector3(-1, 0, 0), new Vector3(0, 0, 0),
                new Vector3(1, 1, 0), new Vector3(3, 1, 0),
                new Vector3(4, 0, 0));
        for (CatmullRomSpline.Parameterization policy : CatmullRomSpline.Parameterization.values()) {
            CatmullRomSpline curve = new CatmullRomSpline(controls, policy);
            assertVector(curve.position(0), 0, 0, 0);
            assertVector(curve.position(0.5), 1, 1, 0);
            assertVector(curve.position(1), 3, 1, 0);
        }
    }

    @Test
    void quadraticNurbsRepresentsWeightedQuarterCircle() {
        double diagonalWeight = StrictMath.sqrt(0.5);
        NurbsCurve curve = new NurbsCurve(2,
                List.of(new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0)),
                new double[]{1, diagonalWeight, 1},
                new double[]{0, 0, 0, 1, 1, 1});

        assertVector(curve.position(0), 1, 0, 0);
        assertVector(curve.position(0.5), diagonalWeight, diagonalWeight, 0);
        assertVector(curve.position(1), 0, 1, 0);
    }

    @Test
    void invalidNurbsDataIsRejectedDefensively() {
        List<Vector3> controls = List.of(Vector3.ZERO, Vector3.ONE);
        assertThrows(IllegalArgumentException.class,
                () -> new NurbsCurve(1, controls, new double[]{1, 0}, new double[]{0, 0, 1, 1}));
        assertThrows(IllegalArgumentException.class,
                () -> new NurbsCurve(1, controls, new double[]{1, 1}, new double[]{0, 1, 0, 1}));
    }

    private static void assertVector(Vector3 actual, double x, double y, double z) {
        assertEquals(x, actual.x(), 1.0e-9);
        assertEquals(y, actual.y(), 1.0e-9);
        assertEquals(z, actual.z(), 1.0e-9);
    }
}
