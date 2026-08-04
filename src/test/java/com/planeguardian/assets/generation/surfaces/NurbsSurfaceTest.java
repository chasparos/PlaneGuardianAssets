package com.planeguardian.assets.generation.surfaces;

import com.planeguardian.assets.generation.api.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NurbsSurfaceTest {
    @Test
    void bilinearPatchReproducesPlaneAndCorners() {
        NurbsSurface surface = bilinear(new double[][]{{1, 1}, {1, 1}});

        assertEquals(new Vector3(0, 0, 0), surface.position(0, 0));
        assertEquals(new Vector3(2, 3, 0), surface.position(1, 1));
        assertEquals(new Vector3(0.5, 2.25, 0), surface.position(0.25, 0.75));
    }

    @Test
    void rationalWeightsPullSurfaceTowardWeightedControlPoint() {
        NurbsSurface uniform = bilinear(new double[][]{{1, 1}, {1, 1}});
        NurbsSurface weighted = bilinear(new double[][]{{1, 1}, {1, 8}});

        Vector3 ordinaryCenter = uniform.position(0.5, 0.5);
        Vector3 weightedCenter = weighted.position(0.5, 0.5);

        assertEquals(new Vector3(1, 1.5, 0), ordinaryCenter);
        org.junit.jupiter.api.Assertions.assertTrue(weightedCenter.x() > ordinaryCenter.x());
        org.junit.jupiter.api.Assertions.assertTrue(weightedCenter.y() > ordinaryCenter.y());
    }

    @Test
    void validatesDomainAndGridShape() {
        NurbsSurface surface = bilinear(new double[][]{{1, 1}, {1, 1}});
        assertThrows(IllegalArgumentException.class, () -> surface.position(-0.1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new NurbsSurface(1, 1,
                List.of(List.of(Vector3.ZERO, Vector3.ZERO), List.of(Vector3.ZERO)),
                new double[][]{{1, 1}, {1, 1}}, knots(), knots()));
    }

    private static NurbsSurface bilinear(double[][] weights) {
        return new NurbsSurface(1, 1, List.of(
                List.of(new Vector3(0, 0, 0), new Vector3(2, 0, 0)),
                List.of(new Vector3(0, 3, 0), new Vector3(2, 3, 0))),
                weights, knots(), knots());
    }

    private static double[] knots() {
        return new double[]{0, 0, 1, 1};
    }
}
