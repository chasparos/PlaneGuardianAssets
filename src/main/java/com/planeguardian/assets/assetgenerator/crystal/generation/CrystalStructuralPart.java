package com.planeguardian.assets.assetgenerator.crystal.generation;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.surface.MeshSurfaceProcessor;
import com.planeguardian.assets.generation.surface.NormalPolicy;
import com.planeguardian.assets.generation.surface.RenderMesh;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.triangulation.ProtoMeshTriangulator;

import java.util.Objects;

/** Named engine-neutral crystal-family component with a stable semantic role. */
public record CrystalStructuralPart(
        StableId id,
        StableId role,
        ProtoMeshSnapshot mesh,
        RenderMesh renderMesh,
        boolean hostContact) {
    public CrystalStructuralPart {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(mesh, "mesh");
        Objects.requireNonNull(renderMesh, "renderMesh");
    }

    public CrystalStructuralPart(StableId id, StableId role, ProtoMeshSnapshot mesh, boolean hostContact) {
        this(id, role, mesh, process(mesh), hostContact);
    }

    private static RenderMesh process(ProtoMeshSnapshot mesh) {
        return MeshSurfaceProcessor.process(
                ProtoMeshTriangulator.triangulate(mesh), NormalPolicy.FLAT_BY_FACE, true);
    }
}
