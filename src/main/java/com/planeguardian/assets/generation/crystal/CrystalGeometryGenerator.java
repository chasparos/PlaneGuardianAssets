package com.planeguardian.assets.generation.crystal;

import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.Rotation;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.Vector2;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Deterministic engine-neutral faceted crystal and host geometry. */
public final class CrystalGeometryGenerator {
    public static final StableId CRYSTAL_ROLE = new StableId("role.gem.crystal");
    public static final StableId HOST_ROLE = new StableId("role.gem.host");
    public static final StableId BASE_SOCKET = new StableId("socket.gem.base-attachment");

    private CrystalGeometryGenerator() { }

    public static CrystalStructureProduct generate(CrystalParameters parameters, long seed) {
        Map<StableId, CrystalStructuralPart> parts = new LinkedHashMap<>();
        var random = NamedRandomStreams.open(seed, "crystal.cluster");
        for (int index = 0; index < parameters.clusterMemberCount(); index++) {
            double radius = parameters.baseRadius() * parameters.sizeScale() * (0.82 + random.nextDouble() * 0.36);
            double height = radius * (2.6 + random.nextDouble() * 1.8);
            double x = index == 0 ? 0 : Math.cos(index * Math.PI * 2 / parameters.clusterMemberCount()) * radius * 1.4;
            double z = index == 0 ? 0 : Math.sin(index * Math.PI * 2 / parameters.clusterMemberCount()) * radius * 1.4;
            CrystalStructuralPart crystal = new CrystalStructuralPart(new StableId("crystal." + index), CRYSTAL_ROLE,
                    crystalMesh(parameters, radius, height, new Vector3(x, 0, z)), false);
            parts.put(crystal.id(), crystal);
        }
        if (parameters.settingKind() == CrystalParameters.SettingKind.NATURAL_ROCK) {
            CrystalStructuralPart host = new CrystalStructuralPart(new StableId("crystal.host.rock"), HOST_ROLE,
                    rockHost(parameters, seed), true);
            parts.put(host.id(), host);
        }
        List<GeneratedSocket> sockets = List.of(new GeneratedSocket(BASE_SOCKET, CRYSTAL_ROLE,
                new Transform(Vector3.ZERO, Rotation.IDENTITY, Vector3.ONE)));
        FingerprintBuilder fingerprint = new FingerprintBuilder().addString(parameters.toString()).addLong(seed);
        parts.keySet().forEach(id -> fingerprint.addString(id.value()));
        return new CrystalStructureProduct(parts, sockets, fingerprint.build());
    }

    private static com.planeguardian.assets.generation.topology.ProtoMeshSnapshot crystalMesh(CrystalParameters parameters, double radius, double height, Vector3 offset) {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        int facets = sides(parameters.facetCount());
        List<com.planeguardian.assets.generation.topology.VertexId> base = ring(builder, facets, radius, offset.y(), offset, parameters.cutStyle());
        double waistScale = parameters.cutStyle() == CrystalParameters.CutStyle.CUSHION ? .86 : 1;
        List<com.planeguardian.assets.generation.topology.VertexId> waist =
                ring(builder, facets, radius * waistScale, offset.y() + height * .55, offset, parameters.cutStyle());
        double crownScale = parameters.cutStyle() == CrystalParameters.CutStyle.BRILLIANT
                ? parameters.tipTaper() * .75 : parameters.tipTaper();
        List<com.planeguardian.assets.generation.topology.VertexId> crown =
                ring(builder, facets, radius * Math.max(.08, crownScale), offset.y() + height * .78, offset, parameters.cutStyle());
        for (int i = 0; i < facets; i++) {
            int next = (i + 1) % facets;
            builder.addFace(List.of(base.get(i), base.get(next), waist.get(next), waist.get(i)),
                    List.of(uv(0, 0), uv(1, 0), uv(1, .55), uv(0, .55)), Set.of("crystal.facet"));
            builder.addFace(List.of(waist.get(i), waist.get(next), crown.get(next), crown.get(i)),
                    List.of(uv(0, .55), uv(1, .55), uv(1, .78), uv(0, .78)), Set.of("crystal.facet"));
        }
        Vector3 apex = new Vector3(offset.x(), offset.y() + height, offset.z());
        var apexId = builder.addVertex(apex);
        for (int i = 0; i < facets; i++) {
            int next = (i + 1) % facets;
            builder.addFace(List.of(crown.get(i), crown.get(next), apexId),
                    List.of(uv(0, .78), uv(1, .78), uv(.5, 1)), Set.of("crystal.facet"));
        }
        List<com.planeguardian.assets.generation.topology.VertexId> reversed = new ArrayList<>(base);
        java.util.Collections.reverse(reversed);
        builder.addFace(reversed, ringUvs(facets), Set.of("crystal.base"));
        return builder.snapshot();
    }

    private static List<com.planeguardian.assets.generation.topology.VertexId> ring(ProtoMeshBuilder builder, int facets,
            double radius, double y, Vector3 offset, CrystalParameters.CutStyle style) {
        List<com.planeguardian.assets.generation.topology.VertexId> vertices = new ArrayList<>(facets);
        for (int i = 0; i < facets; i++) {
            double angle = i * Math.PI * 2 / facets + (style == CrystalParameters.CutStyle.CUSHION ? Math.PI / facets : 0);
            double xScale = style == CrystalParameters.CutStyle.CUSHION && i % 2 == 0 ? 1.08 : 1;
            vertices.add(builder.addVertex(new Vector3(offset.x() + Math.cos(angle) * radius * xScale, y,
                    offset.z() + Math.sin(angle) * radius)));
        }
        return vertices;
    }

    public static int canonicalSides(int requested) {
        if (requested <= 5) return 4;
        if (requested <= 7) return 6;
        return 8;
    }

    private static int sides(int requested) {
        return canonicalSides(requested);
    }

    private static com.planeguardian.assets.generation.topology.ProtoMeshSnapshot rockHost(CrystalParameters parameters, long seed) {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        double r = parameters.baseRadius() * parameters.sizeScale() * 2.4;
        var random = NamedRandomStreams.open(seed, "crystal.host.rock");
        List<com.planeguardian.assets.generation.topology.VertexId> bottom = new ArrayList<>();
        List<com.planeguardian.assets.generation.topology.VertexId> top = new ArrayList<>();
        int facets = Math.max(8, parameters.facetCount());
        for (int i = 0; i < facets; i++) {
            double angle = i * Math.PI * 2 / facets;
            double jitter = 0.75 + random.nextDouble() * 0.35;
            bottom.add(builder.addVertex(new Vector3(Math.cos(angle) * r * jitter, -r * 0.35, Math.sin(angle) * r * jitter)));
            top.add(builder.addVertex(new Vector3(Math.cos(angle) * r * 0.7 * jitter, 0, Math.sin(angle) * r * 0.7 * jitter)));
        }
        for (int i = 0; i < facets; i++) {
            int next = (i + 1) % facets;
            builder.addFace(List.of(bottom.get(i), bottom.get(next), top.get(next), top.get(i)),
                    List.of(uv(0, 0), uv(1, 0), uv(1, 1), uv(0, 1)), Set.of("rock.host"));
        }
        List<com.planeguardian.assets.generation.topology.VertexId> reversedBottom = new ArrayList<>(bottom);
        java.util.Collections.reverse(reversedBottom);
        builder.addFace(reversedBottom, ringUvs(facets), Set.of("rock.host.bottom"));
        builder.addFace(top, ringUvs(facets), Set.of("rock.host.top"));
        return builder.snapshot();
    }

    private static List<CornerAttributes> ringUvs(int count) {
        List<CornerAttributes> attributes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2 / count;
            attributes.add(new CornerAttributes(Optional.of(new Vector2(0.5 + Math.cos(angle) * 0.5, 0.5 + Math.sin(angle) * 0.5)), Optional.empty(), Map.of()));
        }
        return attributes;
    }

    private static CornerAttributes uv(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), Map.of());
    }
}
