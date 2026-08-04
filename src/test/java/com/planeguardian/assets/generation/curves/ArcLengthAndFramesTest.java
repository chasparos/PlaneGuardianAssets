package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.math.VectorMath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArcLengthAndFramesTest {
    @Test
    void straightCurveHasExactSampledLengthAndInverseParameters() {
        CubicHermiteCurve line = straightZLine();
        ArcLengthTable lengths = new ArcLengthTable(line, 65);

        assertEquals(4, lengths.totalLength(), 1.0e-12);
        assertEquals(0.25, lengths.parameterAtFraction(0.25), 1.0e-12);
        assertEquals(0.75, lengths.parameterAtDistance(3), 1.0e-12);
    }

    @Test
    void rollProfileTwistsFrameWithoutChangingCenterlineOrTangent() {
        ArcLengthTable lengths = new ArcLengthTable(straightZLine(), 33);
        List<CurveFrame> frames = ParallelTransportFrames.sample(
                lengths, 5, new Vector3(1, 0, 0), fraction -> fraction * StrictMath.PI * 2);

        CurveFrame quarterTurn = frames.get(1);
        assertVector(quarterTurn.position(), 0, 0, 1);
        assertVector(quarterTurn.tangent(), 0, 0, 1);
        assertVector(quarterTurn.normal(), 0, 1, 0);
        assertVector(quarterTurn.binormal(), -1, 0, 0);
        assertVector(frames.get(4).normal(), 1, 0, 0);
    }

    @Test
    void transportedFramesRemainOrthonormalAlongCurvedSpline() {
        CatmullRomSpline curve = new CatmullRomSpline(List.of(
                new Vector3(-1, 0, 0), new Vector3(0, 0, 0),
                new Vector3(1, 1, 0), new Vector3(1, 2, 1),
                new Vector3(0, 3, 2)), CatmullRomSpline.Parameterization.CENTRIPETAL);
        List<CurveFrame> frames = ParallelTransportFrames.sample(
                new ArcLengthTable(curve, 257), 32, new Vector3(0, 1, 0), fraction -> 0);

        for (CurveFrame frame : frames) {
            assertEquals(1, VectorMath.length(frame.tangent()), 1.0e-9);
            assertEquals(1, VectorMath.length(frame.normal()), 1.0e-9);
            assertEquals(1, VectorMath.length(frame.binormal()), 1.0e-9);
            assertEquals(0, VectorMath.dot(frame.tangent(), frame.normal()), 1.0e-9);
            assertEquals(0, VectorMath.dot(frame.tangent(), frame.binormal()), 1.0e-9);
            assertEquals(0, VectorMath.dot(frame.normal(), frame.binormal()), 1.0e-9);
            assertTrue(Double.isFinite(frame.parameter()));
        }
    }

    private static CubicHermiteCurve straightZLine() {
        return new CubicHermiteCurve(
                Vector3.ZERO, new Vector3(0, 0, 4),
                new Vector3(0, 0, 4), new Vector3(0, 0, 4));
    }

    private static void assertVector(Vector3 actual, double x, double y, double z) {
        assertEquals(x, actual.x(), 1.0e-9);
        assertEquals(y, actual.y(), 1.0e-9);
        assertEquals(z, actual.z(), 1.0e-9);
    }
}
