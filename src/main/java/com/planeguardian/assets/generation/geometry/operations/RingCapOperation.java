package com.planeguardian.assets.generation.geometry.operations;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.geometry.tube.TubeEnd;
import com.planeguardian.assets.generation.math.VectorMath;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.FaceId;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RingCapOperation {
    private RingCapOperation() {
    }

    /** Caps a tube end; outwardAlongFrameTangent selects winding. */
    public static FaceId cap(
            ProtoMeshBuilder builder, TubeEnd end,
            boolean outwardAlongFrameTangent, Set<String> semanticGroups) {
        List<VertexId> ring = end.ringVertices();
        List<VertexId> ordered = new ArrayList<>(ring);
        List<CornerAttributes> attributes = new ArrayList<>(ring.size());
        for (int index = 0; index < ring.size(); index++) attributes.add(capCorner(index, ring.size()));
        if (!outwardAlongFrameTangent) {
            java.util.Collections.reverse(ordered);
            java.util.Collections.reverse(attributes);
        }
        return builder.addFace(ordered, attributes, semanticGroups);
    }

    /** Closes a ring to a single apex point using triangle fan faces. */
    public static List<FaceId> pointCap(
            ProtoMeshBuilder builder, TubeEnd end, boolean outwardAlongFrameTangent,
            double apexDistance, Set<String> semanticGroups) {
        if (!Double.isFinite(apexDistance) || apexDistance <= 0) {
            throw new IllegalArgumentException("apexDistance must be positive");
        }
        List<VertexId> ordered = new ArrayList<>(end.ringVertices());
        if (!outwardAlongFrameTangent) java.util.Collections.reverse(ordered);
        Vector3 apexPosition = VectorMath.add(end.frame().position(),
                VectorMath.scale(end.frame().tangent(), outwardAlongFrameTangent ? apexDistance : -apexDistance));
        VertexId apex = builder.addVertex(apexPosition);
        List<FaceId> faces = new ArrayList<>(ordered.size());
        for (int index = 0; index < ordered.size(); index++) {
            VertexId a = ordered.get(index);
            VertexId b = ordered.get((index + 1) % ordered.size());
            faces.add(builder.addFace(List.of(a, b, apex), List.of(
                    capCorner(index, ordered.size()),
                    capCorner(index + 1, ordered.size()),
                    new CornerAttributes(Optional.of(new Vector2(0.5, 0.5)), Optional.empty(), Map.of())), semanticGroups));
        }
        return List.copyOf(faces);
    }

    private static CornerAttributes capCorner(int index, int count) {
        double angle = index * StrictMath.PI * 2 / count;
        return new CornerAttributes(Optional.of(new Vector2(
                0.5 + StrictMath.cos(angle) * 0.5,
                0.5 + StrictMath.sin(angle) * 0.5)), Optional.empty(), Map.of());
    }
}
