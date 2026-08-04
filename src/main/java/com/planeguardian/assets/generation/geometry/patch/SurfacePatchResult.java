package com.planeguardian.assets.generation.geometry.patch;

import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.List;

public record SurfacePatchResult(
        ProtoMeshSnapshot mesh,
        List<List<VertexId>> vertexRows,
        List<VertexId> minimumU,
        List<VertexId> maximumU,
        List<VertexId> minimumV,
        List<VertexId> maximumV) {
    public SurfacePatchResult {
        vertexRows = vertexRows.stream().map(List::copyOf).toList();
        minimumU = List.copyOf(minimumU);
        maximumU = List.copyOf(maximumU);
        minimumV = List.copyOf(minimumV);
        maximumV = List.copyOf(maximumV);
    }
}
