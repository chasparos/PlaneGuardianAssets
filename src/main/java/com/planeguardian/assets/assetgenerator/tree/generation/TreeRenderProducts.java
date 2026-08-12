package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.surface.MeshSurfaceProcessor;
import com.planeguardian.assets.generation.surface.NormalPolicy;
import com.planeguardian.assets.generation.surface.RenderMesh;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.triangulation.ProtoMeshTriangulator;

import java.util.Objects;

/** Produces the canonical engine-neutral render representation for tree topology. */
final class TreeRenderProducts {
    private TreeRenderProducts() {
    }

    static RenderMesh process(ProtoMeshSnapshot mesh) {
        Objects.requireNonNull(mesh, "mesh");
        return MeshSurfaceProcessor.process(
                ProtoMeshTriangulator.triangulate(mesh), NormalPolicy.SMOOTH_BY_SOURCE_VERTEX, true);
    }
}
