package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.surface.RenderMesh;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;

import java.util.Objects;

/** Named engine-neutral spline-tube component with a stable semantic role. */
public record TreeStructuralPart(
        StableId id,
        StableId role,
        ProtoMeshSnapshot mesh,
        RenderMesh renderMesh,
        boolean hostContact) {
    public TreeStructuralPart {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(mesh, "mesh");
        Objects.requireNonNull(renderMesh, "renderMesh");
    }

    public TreeStructuralPart(StableId id, StableId role, ProtoMeshSnapshot mesh, boolean hostContact) {
        this(id, role, mesh, TreeRenderProducts.process(mesh), hostContact);
    }
}
