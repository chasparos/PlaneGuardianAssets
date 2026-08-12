package com.planeguardian.assets.assetgenerator.sample.generation;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Deterministic engine-neutral geometry for the minimal registered sample generator.
 * Composed entirely from {@code ProtoMesh} ring/cap construction, matching the shared
 * geometry toolkit; no jME type is used at this stage.
 */
public final class SampleShapeGenerator {
    private static final int CYLINDER_SIDES = 16;
    private static final int BOX_SIDES = 4;

    private SampleShapeGenerator() {
    }

    public static SampleShapeProduct generate(boolean cylinder, double scale, long seed) {
        if (!Double.isFinite(scale) || scale <= 0) throw new IllegalArgumentException("scale must be positive");
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        int sides = cylinder ? CYLINDER_SIDES : BOX_SIDES;
        double angleOffset = cylinder ? 0 : Math.PI / 4;
        double radius = scale;
        double halfHeight = scale;
        List<VertexId> bottom = ring(builder, sides, radius, -halfHeight, angleOffset);
        List<VertexId> top = ring(builder, sides, radius, halfHeight, angleOffset);
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            builder.addFace(List.of(bottom.get(next), bottom.get(i), top.get(i), top.get(next)),
                    List.of(uv(1, 0), uv(0, 0), uv(0, 1), uv(1, 1)), Set.of("sample.side"));
        }
        builder.addFace(bottom, ringUvs(sides), Set.of("sample.cap.bottom"));
        builder.addFace(reversed(top), ringUvs(sides), Set.of("sample.cap.top"));
        var snapshot = builder.snapshot();
        ReproducibilityFingerprint fingerprint = new FingerprintBuilder()
                .addString(cylinder ? "cylinder" : "box").addString(Double.toHexString(scale)).addLong(seed).build();
        return new SampleShapeProduct(cylinder, snapshot, fingerprint);
    }

    private static List<VertexId> ring(ProtoMeshBuilder builder, int sides, double radius, double y, double angleOffset) {
        List<VertexId> vertices = new ArrayList<>(sides);
        for (int i = 0; i < sides; i++) {
            double angle = angleOffset + i * Math.PI * 2 / sides;
            vertices.add(builder.addVertex(new Vector3(Math.cos(angle) * radius, y, Math.sin(angle) * radius)));
        }
        return vertices;
    }

    private static List<VertexId> reversed(List<VertexId> ring) {
        List<VertexId> copy = new ArrayList<>(ring);
        java.util.Collections.reverse(copy);
        return copy;
    }

    private static List<CornerAttributes> ringUvs(int count) {
        List<CornerAttributes> attributes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2 / count;
            attributes.add(new CornerAttributes(
                    Optional.of(new Vector2(0.5 + Math.cos(angle) * 0.5, 0.5 + Math.sin(angle) * 0.5)),
                    Optional.empty(), java.util.Map.of()));
        }
        return attributes;
    }

    private static CornerAttributes uv(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), java.util.Map.of());
    }
}
