package com.planeguardian.assets.generation.geometry.tube;

import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.List;
import java.util.Objects;

public record SplineTubeResult(
        ProtoMeshSnapshot mesh,
        List<List<VertexId>> rings,
        TubeEnd start,
        TubeEnd end) {
    public SplineTubeResult {
        Objects.requireNonNull(mesh, "mesh");
        rings = rings.stream().map(List::copyOf).toList();
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
    }
}
