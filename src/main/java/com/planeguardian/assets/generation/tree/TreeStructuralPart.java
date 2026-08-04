package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;

import java.util.Objects;

/** Named engine-neutral spline-tube component with a stable semantic role. */
public record TreeStructuralPart(StableId id, StableId role, ProtoMeshSnapshot mesh, boolean hostContact) {
    public TreeStructuralPart {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(mesh, "mesh");
    }
}
