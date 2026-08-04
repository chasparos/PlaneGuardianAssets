package com.planeguardian.assets.generation.geometry.patch;

import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Deterministically samples a normalized surface into a UV-mapped quad grid. */
public final class QuadSurfacePatchGenerator {
    private QuadSurfacePatchGenerator() {
    }

    public static SurfacePatchResult generate(SurfacePatchRequest request) {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        List<List<VertexId>> rows = new ArrayList<>(request.segmentsV() + 1);
        for (int vIndex = 0; vIndex <= request.segmentsV(); vIndex++) {
            double v = (double) vIndex / request.segmentsV();
            List<VertexId> row = new ArrayList<>(request.segmentsU() + 1);
            for (int uIndex = 0; uIndex <= request.segmentsU(); uIndex++) {
                double u = (double) uIndex / request.segmentsU();
                row.add(builder.addVertex(request.surface().position(u, v)));
            }
            rows.add(List.copyOf(row));
        }
        for (int vIndex = 0; vIndex < request.segmentsV(); vIndex++) {
            double v0 = (double) vIndex / request.segmentsV();
            double v1 = (double) (vIndex + 1) / request.segmentsV();
            for (int uIndex = 0; uIndex < request.segmentsU(); uIndex++) {
                double u0 = (double) uIndex / request.segmentsU();
                double u1 = (double) (uIndex + 1) / request.segmentsU();
                builder.addFace(List.of(
                                rows.get(vIndex).get(uIndex), rows.get(vIndex).get(uIndex + 1),
                                rows.get(vIndex + 1).get(uIndex + 1), rows.get(vIndex + 1).get(uIndex)),
                        List.of(attributes(u0, v0), attributes(u1, v0), attributes(u1, v1), attributes(u0, v1)),
                        request.semanticGroups());
            }
        }
        ProtoMeshSnapshot mesh = builder.snapshot();
        if (!mesh.isValid()) throw new IllegalStateException("Surface patch produced invalid topology: " + mesh.issues());
        return new SurfacePatchResult(mesh, rows,
                rows.stream().map(row -> row.get(0)).toList(),
                rows.stream().map(row -> row.get(row.size() - 1)).toList(),
                rows.get(0), rows.get(rows.size() - 1));
    }

    private static CornerAttributes attributes(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), Map.of());
    }
}
