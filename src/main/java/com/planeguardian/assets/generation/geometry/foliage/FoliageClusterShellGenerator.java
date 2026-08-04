package com.planeguardian.assets.generation.geometry.foliage;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.ProtoMeshSnapshot;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Generates a closed UV-mapped ellipsoidal shell without asset-family or renderer dependencies. */
public final class FoliageClusterShellGenerator {
    private FoliageClusterShellGenerator() {
    }

    public static ProtoMeshSnapshot generate(FoliageClusterShellRequest request) {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId top = builder.addVertex(new Vector3(request.center().x(),
                request.center().y() + request.radii().y(), request.center().z()));
        List<List<VertexId>> rings = new ArrayList<>(request.latitudeBands() - 1);
        for (int latitude = 1; latitude < request.latitudeBands(); latitude++) {
            double v = (double) latitude / request.latitudeBands();
            double phi = StrictMath.PI * v;
            double horizontal = StrictMath.sin(phi);
            List<VertexId> ring = new ArrayList<>(request.radialSegments());
            for (int segment = 0; segment < request.radialSegments(); segment++) {
                double theta = StrictMath.PI * 2 * segment / request.radialSegments();
                ring.add(builder.addVertex(new Vector3(
                        request.center().x() + request.radii().x() * horizontal * StrictMath.cos(theta),
                        request.center().y() + request.radii().y() * StrictMath.cos(phi),
                        request.center().z() + request.radii().z() * horizontal * StrictMath.sin(theta))));
            }
            rings.add(List.copyOf(ring));
        }
        VertexId bottom = builder.addVertex(new Vector3(request.center().x(),
                request.center().y() - request.radii().y(), request.center().z()));

        List<VertexId> first = rings.get(0);
        for (int segment = 0; segment < request.radialSegments(); segment++) {
            int next = (segment + 1) % request.radialSegments();
            builder.addFace(List.of(top, first.get(next), first.get(segment)),
                    triangleAttributes(segment, next, 0, 1.0 / request.latitudeBands(), request.radialSegments()),
                    request.semanticGroups());
        }
        for (int latitude = 0; latitude < rings.size() - 1; latitude++) {
            List<VertexId> lower = rings.get(latitude);
            List<VertexId> upper = rings.get(latitude + 1);
            double v0 = (double) (latitude + 1) / request.latitudeBands();
            double v1 = (double) (latitude + 2) / request.latitudeBands();
            for (int segment = 0; segment < request.radialSegments(); segment++) {
                int next = (segment + 1) % request.radialSegments();
                builder.addFace(List.of(lower.get(segment), lower.get(next), upper.get(next), upper.get(segment)),
                        quadAttributes(segment, next, v0, v1, request.radialSegments()), request.semanticGroups());
            }
        }
        List<VertexId> last = rings.get(rings.size() - 1);
        for (int segment = 0; segment < request.radialSegments(); segment++) {
            int next = (segment + 1) % request.radialSegments();
            builder.addFace(List.of(bottom, last.get(segment), last.get(next)),
                    triangleAttributes(segment, next, 1,
                            (double) (request.latitudeBands() - 1) / request.latitudeBands(),
                            request.radialSegments()),
                    request.semanticGroups());
        }
        ProtoMeshSnapshot shell = builder.snapshot();
        if (!shell.isValid()) {
            throw new IllegalStateException("Foliage shell produced invalid topology: " + shell.issues());
        }
        return shell;
    }

    private static List<CornerAttributes> quadAttributes(
            int segment, int next, double v0, double v1, int radialSegments) {
        double u0 = (double) segment / radialSegments;
        double u1 = next == 0 ? 1 : (double) next / radialSegments;
        return List.of(attribute(u0, v0), attribute(u1, v0), attribute(u1, v1), attribute(u0, v1));
    }

    private static List<CornerAttributes> triangleAttributes(
            int segment, int next, double poleV, double ringV, int radialSegments) {
        double u0 = (double) segment / radialSegments;
        double u1 = next == 0 ? 1 : (double) next / radialSegments;
        return List.of(attribute((u0 + u1) * .5, poleV), attribute(u1, ringV), attribute(u0, ringV));
    }

    private static CornerAttributes attribute(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), Map.of());
    }
}
