package com.planeguardian.assets.assetgenerator.sample.generation;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.surface.MeshSurfaceProcessor;
import com.planeguardian.assets.generation.surface.NormalPolicy;
import com.planeguardian.assets.generation.surface.RenderMesh;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.triangulation.ProtoMeshTriangulator;

import java.util.Objects;

/** Engine-neutral geometry produced for the registered sample shape generator. */
public record SampleShapeProduct(
        boolean cylinder,
        ProtoMeshSnapshot mesh,
        RenderMesh renderMesh,
        ReproducibilityFingerprint fingerprint) {
    public SampleShapeProduct {
        Objects.requireNonNull(mesh, "mesh");
        Objects.requireNonNull(renderMesh, "renderMesh");
        Objects.requireNonNull(fingerprint, "fingerprint");
    }

    public SampleShapeProduct(boolean cylinder, ProtoMeshSnapshot mesh, ReproducibilityFingerprint fingerprint) {
        this(cylinder, mesh, process(mesh), fingerprint);
    }

    private static RenderMesh process(ProtoMeshSnapshot mesh) {
        return MeshSurfaceProcessor.process(
                ProtoMeshTriangulator.triangulate(mesh), NormalPolicy.FLAT_BY_FACE, false);
    }
}
