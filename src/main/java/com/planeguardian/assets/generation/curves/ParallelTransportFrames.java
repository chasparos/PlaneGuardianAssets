package com.planeguardian.assets.generation.curves;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.math.VectorMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/** Minimal-twist frames with an explicit absolute roll profile over arc length. */
public final class ParallelTransportFrames {
    private ParallelTransportFrames() {
    }

    public static List<CurveFrame> sample(
            ArcLengthTable lengths,
            int frameCount,
            Vector3 initialNormalHint,
            DoubleUnaryOperator rollByArcFraction) {
        Objects.requireNonNull(lengths, "lengths");
        Objects.requireNonNull(initialNormalHint, "initialNormalHint");
        Objects.requireNonNull(rollByArcFraction, "rollByArcFraction");
        if (frameCount < 2) throw new IllegalArgumentException("At least two frames are required");
        ParametricCurve3 curve = lengths.curve();
        List<CurveFrame> result = new ArrayList<>(frameCount);
        Vector3 previousTangent = VectorMath.normalize(curve.derivative(0));
        Vector3 transportedNormal = initialNormal(previousTangent, initialNormalHint);
        for (int index = 0; index < frameCount; index++) {
            double fraction = index / (double) (frameCount - 1);
            double parameter = lengths.parameterAtFraction(fraction);
            Vector3 tangent = VectorMath.normalize(curve.derivative(parameter));
            if (index > 0) transportedNormal = transport(transportedNormal, previousTangent, tangent);
            transportedNormal = VectorMath.normalize(VectorMath.reject(transportedNormal, tangent));
            double roll = rollByArcFraction.applyAsDouble(fraction);
            if (!Double.isFinite(roll)) throw new IllegalArgumentException("Roll profile must return finite radians");
            Vector3 normal = VectorMath.normalize(VectorMath.rotateAroundUnitAxis(transportedNormal, tangent, roll));
            Vector3 binormal = VectorMath.normalize(VectorMath.cross(tangent, normal));
            result.add(new CurveFrame(fraction, parameter, curve.position(parameter), tangent, normal, binormal, roll));
            previousTangent = tangent;
        }
        return List.copyOf(result);
    }

    private static Vector3 initialNormal(Vector3 tangent, Vector3 hint) {
        Vector3 rejected = VectorMath.reject(hint, tangent);
        if (VectorMath.lengthSquared(rejected) > 1.0e-20) return VectorMath.normalize(rejected);
        Vector3 fallback = leastAlignedAxis(tangent);
        return VectorMath.normalize(VectorMath.reject(fallback, tangent));
    }

    private static Vector3 transport(Vector3 normal, Vector3 fromTangent, Vector3 toTangent) {
        Vector3 axis = VectorMath.cross(fromTangent, toTangent);
        double axisLengthSquared = VectorMath.lengthSquared(axis);
        double cosine = VectorMath.clamp(VectorMath.dot(fromTangent, toTangent), -1, 1);
        if (axisLengthSquared <= 1.0e-20) {
            if (cosine >= 0) return normal;
            return VectorMath.rotateAroundUnitAxis(normal, VectorMath.normalize(VectorMath.cross(fromTangent, leastAlignedAxis(fromTangent))), StrictMath.PI);
        }
        Vector3 unitAxis = VectorMath.scale(axis, 1.0 / StrictMath.sqrt(axisLengthSquared));
        double angle = StrictMath.atan2(StrictMath.sqrt(axisLengthSquared), cosine);
        return VectorMath.rotateAroundUnitAxis(normal, unitAxis, angle);
    }

    private static Vector3 leastAlignedAxis(Vector3 tangent) {
        double x = StrictMath.abs(tangent.x());
        double y = StrictMath.abs(tangent.y());
        double z = StrictMath.abs(tangent.z());
        if (x <= y && x <= z) return new Vector3(1, 0, 0);
        if (y <= z) return new Vector3(0, 1, 0);
        return new Vector3(0, 0, 1);
    }
}
