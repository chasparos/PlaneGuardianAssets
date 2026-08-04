package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.surface.RenderMesh;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;

import java.util.Objects;

/** One named engine-neutral foliage shell in a generated crown. */
public record TreeCrownPart(StableId id, StableId role, ProtoMeshSnapshot mesh, RenderMesh renderMesh) {
    public TreeCrownPart {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(mesh, "mesh");
        Objects.requireNonNull(renderMesh, "renderMesh");
    }

    public TreeCrownPart(StableId id, StableId role, ProtoMeshSnapshot mesh) {
        this(id, role, mesh, TreeRenderProducts.process(mesh));
    }
}
