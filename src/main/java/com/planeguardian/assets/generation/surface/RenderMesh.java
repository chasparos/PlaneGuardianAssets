package com.planeguardian.assets.generation.surface;

import java.util.List;

/** Fully processed engine-neutral triangle mesh consumed by output adapters. */
public final class RenderMesh {
    private final List<RenderVertex> vertices;
    private final int[] indices;

    public RenderMesh(List<RenderVertex> vertices, int[] indices) {
        this.vertices = List.copyOf(vertices);
        this.indices = indices.clone();
        if (indices.length % 3 != 0) throw new IllegalArgumentException("Indices must form triangles");
        for (int index : indices) {
            if (index < 0 || index >= vertices.size()) throw new IllegalArgumentException("Index out of range");
        }
    }

    public List<RenderVertex> vertices() { return vertices; }
    public int[] indices() { return indices.clone(); }
    public int triangleCount() { return indices.length / 3; }
    public boolean hasTextureCoordinates() { return vertices.stream().allMatch(vertex -> vertex.textureCoordinate().isPresent()); }
    public boolean hasTangents() { return vertices.stream().allMatch(vertex -> vertex.tangent().isPresent()); }
}
