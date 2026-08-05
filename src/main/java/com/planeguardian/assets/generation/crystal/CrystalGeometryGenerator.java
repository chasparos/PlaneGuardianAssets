package com.planeguardian.assets.generation.crystal;

import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.Rotation;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.curves.CubicHermiteCurve;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;
import com.planeguardian.assets.generation.geometry.operations.RingCapOperation;
import com.planeguardian.assets.generation.geometry.tube.CrossSectionProfile;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeGenerator;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeRequest;
import com.planeguardian.assets.generation.math.VectorMath;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.ProtoMeshEditTransaction;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.tree.TreeStructuralPart;

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
        Map<StableId, TreeStructuralPart> parts = new LinkedHashMap<>();
        var random = NamedRandomStreams.open(seed, "crystal.cluster");
        for (int index = 0; index < parameters.clusterMemberCount(); index++) {
            double radius = parameters.baseRadius() * parameters.sizeScale() * (0.82 + random.nextDouble() * 0.36);
            double height = radius * (2.6 + random.nextDouble() * 1.8);
            double x = index == 0 ? 0 : Math.cos(index * Math.PI * 2 / parameters.clusterMemberCount()) * radius * 1.4;
            double z = index == 0 ? 0 : Math.sin(index * Math.PI * 2 / parameters.clusterMemberCount()) * radius * 1.4;
            TreeStructuralPart crystal = new TreeStructuralPart(new StableId("crystal." + index), CRYSTAL_ROLE,
                    crystalMesh(parameters, radius, height, new Vector3(x, 0, z)), false);
            parts.put(crystal.id(), crystal);
        }
        if (parameters.settingKind() == CrystalParameters.SettingKind.NATURAL_ROCK) {
            TreeStructuralPart host = new TreeStructuralPart(new StableId("crystal.host.rock"), HOST_ROLE,
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
        int ringCount = parameters.facetRows();
        int verticesPerRing = Math.max(8, parameters.facetCount());
        var tube = SplineTubeGenerator.generate(new SplineTubeRequest(
                new CubicHermiteCurve(offset, VectorMath.add(offset, new Vector3(0, height, 0)), new Vector3(0, height, 0), new Vector3(0, height, 0)),
                ringCount, verticesPerRing, 48, new Vector3(1, 0, 0),
                fraction -> Math.max(radius * (1 - fraction * (1 - parameters.tipTaper())), radius * parameters.tipTaper()),
                CrossSectionProfile.faceted(verticesPerRing, 0.18), fraction -> 0, Set.of("crystal.body")));
        ProtoMeshEditTransaction edit = ProtoMeshEditTransaction.begin(tube.mesh());
        edit.apply(builder -> RingCapOperation.cap(builder, tube.start(), false, Set.of("crystal.base")));
        edit.apply(builder -> RingCapOperation.pointCap(builder, tube.end(), true, Math.max(radius * 0.9, 0.02), Set.of("crystal.tip")));
        return edit.commit();
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
