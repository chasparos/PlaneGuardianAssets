package com.planeguardian.assets.generation.surface;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.topology.FaceId;
import com.planeguardian.assets.generation.topology.LoopId;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.Objects;
import java.util.Optional;

public record RenderVertex(
        VertexId sourceVertexId,
        LoopId sourceLoopId,
        FaceId sourceFaceId,
        Vector3 position,
        Vector3 normal,
        Optional<Vector2> textureCoordinate,
        Optional<Vector4> tangent) {
    public RenderVertex {
        Objects.requireNonNull(sourceVertexId, "sourceVertexId");
        Objects.requireNonNull(sourceLoopId, "sourceLoopId");
        Objects.requireNonNull(sourceFaceId, "sourceFaceId");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(normal, "normal");
        Objects.requireNonNull(textureCoordinate, "textureCoordinate");
        Objects.requireNonNull(tangent, "tangent");
    }
}
