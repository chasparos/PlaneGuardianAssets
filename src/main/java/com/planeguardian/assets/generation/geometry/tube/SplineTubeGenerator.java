package com.planeguardian.assets.generation.geometry.tube;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.curves.ArcLengthTable;
import com.planeguardian.assets.generation.curves.CurveFrame;
import com.planeguardian.assets.generation.curves.ParallelTransportFrames;
import com.planeguardian.assets.generation.math.VectorMath;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reusable quad-side loft around arc-length-spaced, parallel-transported rings. */
public final class SplineTubeGenerator {
    private SplineTubeGenerator() {
    }

    public static SplineTubeResult generate(SplineTubeRequest request) {
        ArcLengthTable lengths = new ArcLengthTable(request.centerline(), request.arcLengthSamples());
        List<CurveFrame> frames = ParallelTransportFrames.sample(
                lengths, request.ringCount(), request.initialNormalHint(), request.rollRadiansByArcFraction());
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        List<List<VertexId>> rings = new ArrayList<>(frames.size());
        double[] radii = new double[frames.size()];

        for (int ringIndex = 0; ringIndex < frames.size(); ringIndex++) {
            CurveFrame frame = frames.get(ringIndex);
            double radius = request.radiusProfile().radiusAt(frame.arcFraction());
            requirePositiveFinite(radius, "Radius profile");
            radii[ringIndex] = radius;
            List<VertexId> ring = new ArrayList<>(request.verticesPerRing());
            for (int vertexIndex = 0; vertexIndex < request.verticesPerRing(); vertexIndex++) {
                double angleFraction = vertexIndex / (double) request.verticesPerRing();
                double angle = angleFraction * StrictMath.PI * 2;
                double multiplier = request.crossSectionProfile().multiplierAt(frame.arcFraction(), angleFraction);
                requirePositiveFinite(multiplier, "Cross-section profile");
                Vector3 radial = VectorMath.add(
                        VectorMath.scale(frame.normal(), StrictMath.cos(angle)),
                        VectorMath.scale(frame.binormal(), StrictMath.sin(angle)));
                ring.add(builder.addVertex(VectorMath.add(
                        frame.position(), VectorMath.scale(radial, radius * multiplier))));
            }
            rings.add(List.copyOf(ring));
        }

        for (int ringIndex = 0; ringIndex < rings.size() - 1; ringIndex++) {
            double v0 = frames.get(ringIndex).arcFraction();
            double v1 = frames.get(ringIndex + 1).arcFraction();
            for (int vertexIndex = 0; vertexIndex < request.verticesPerRing(); vertexIndex++) {
                int nextVertex = (vertexIndex + 1) % request.verticesPerRing();
                double u0 = vertexIndex / (double) request.verticesPerRing();
                double u1 = (vertexIndex + 1) / (double) request.verticesPerRing();
                builder.addFace(List.of(
                                rings.get(ringIndex).get(vertexIndex),
                                rings.get(ringIndex).get(nextVertex),
                                rings.get(ringIndex + 1).get(nextVertex),
                                rings.get(ringIndex + 1).get(vertexIndex)),
                        List.of(corner(u0, v0), corner(u1, v0), corner(u1, v1), corner(u0, v1)),
                        request.semanticGroups());
            }
        }

        TubeEnd start = new TubeEnd(rings.get(0), frames.get(0), radii[0]);
        int last = rings.size() - 1;
        TubeEnd end = new TubeEnd(rings.get(last), frames.get(last), radii[last]);
        return new SplineTubeResult(builder.snapshot(), rings, start, end);
    }

    private static CornerAttributes corner(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(),
                Map.of("tube.arc_fraction", v, "tube.angle_fraction", u));
    }

    private static void requirePositiveFinite(double value, String profileName) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(profileName + " must return finite positive values");
        }
    }
}
