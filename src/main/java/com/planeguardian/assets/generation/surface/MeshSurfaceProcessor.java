package com.planeguardian.assets.generation.surface;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.triangulation.TriangleVertex;
import com.planeguardian.assets.generation.triangulation.TriangulatedMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Deterministic normal and tangent generation shared by preview and export. */
public final class MeshSurfaceProcessor {
    private static final double EPSILON_SQUARED = 1.0e-24;

    private MeshSurfaceProcessor() {
    }

    public static RenderMesh process(
            TriangulatedMesh source, NormalPolicy normalPolicy, boolean generateTangents) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(normalPolicy, "normalPolicy");
        List<Vector3> normals = normals(source, normalPolicy);
        List<Optional<Vector4>> tangents = generateTangents
                ? tangents(source, normals)
                : java.util.Collections.nCopies(source.vertices().size(), Optional.empty());
        List<RenderVertex> vertices = new ArrayList<>(source.vertices().size());
        for (int index = 0; index < source.vertices().size(); index++) {
            TriangleVertex vertex = source.vertices().get(index);
            vertices.add(new RenderVertex(
                    vertex.sourceVertexId(), vertex.sourceLoopId(), vertex.sourceFaceId(),
                    vertex.position(), normals.get(index), vertex.attributes().textureCoordinate(), tangents.get(index)));
        }
        return new RenderMesh(vertices, source.indices());
    }

    private static List<Vector3> normals(TriangulatedMesh source, NormalPolicy policy) {
        if (policy == NormalPolicy.PRESERVE_AUTHORED) {
            return source.vertices().stream().map(vertex -> vertex.attributes().normal()
                    .orElseThrow(() -> new IllegalArgumentException("Every corner needs an authored normal")))
                    .map(MeshSurfaceProcessor::normalize).toList();
        }
        Map<Object, MutableVector> accumulated = new HashMap<>();
        int[] indices = source.indices();
        for (int triangle = 0; triangle < indices.length; triangle += 3) {
            TriangleVertex a = source.vertices().get(indices[triangle]);
            TriangleVertex b = source.vertices().get(indices[triangle + 1]);
            TriangleVertex c = source.vertices().get(indices[triangle + 2]);
            Vector3 faceNormal = cross(subtract(b.position(), a.position()), subtract(c.position(), a.position()));
            requireMagnitude(faceNormal, "Cannot shade a zero-area triangle");
            for (int offset = 0; offset < 3; offset++) {
                TriangleVertex vertex = source.vertices().get(indices[triangle + offset]);
                Object key = policy == NormalPolicy.FLAT_BY_FACE ? vertex.sourceFaceId() : vertex.sourceVertexId();
                accumulated.computeIfAbsent(key, ignored -> new MutableVector()).add(faceNormal);
            }
        }
        return source.vertices().stream().map(vertex -> normalize(accumulated.get(
                policy == NormalPolicy.FLAT_BY_FACE ? vertex.sourceFaceId() : vertex.sourceVertexId()).value)).toList();
    }

    private static List<Optional<Vector4>> tangents(TriangulatedMesh source, List<Vector3> normals) {
        if (source.vertices().stream().anyMatch(vertex -> vertex.attributes().textureCoordinate().isEmpty())) {
            throw new IllegalArgumentException("Tangent generation requires a UV at every corner");
        }
        MutableVector[] tangentSums = vectors(source.vertices().size());
        MutableVector[] bitangentSums = vectors(source.vertices().size());
        int[] indices = source.indices();
        for (int triangle = 0; triangle < indices.length; triangle += 3) {
            int ia = indices[triangle];
            int ib = indices[triangle + 1];
            int ic = indices[triangle + 2];
            TriangleVertex a = source.vertices().get(ia);
            TriangleVertex b = source.vertices().get(ib);
            TriangleVertex c = source.vertices().get(ic);
            var uvA = a.attributes().textureCoordinate().orElseThrow();
            var uvB = b.attributes().textureCoordinate().orElseThrow();
            var uvC = c.attributes().textureCoordinate().orElseThrow();
            Vector3 edgeOne = subtract(b.position(), a.position());
            Vector3 edgeTwo = subtract(c.position(), a.position());
            double duOne = uvB.x() - uvA.x();
            double dvOne = uvB.y() - uvA.y();
            double duTwo = uvC.x() - uvA.x();
            double dvTwo = uvC.y() - uvA.y();
            double determinant = duOne * dvTwo - duTwo * dvOne;
            if (StrictMath.abs(determinant) <= 1.0e-12) {
                throw new IllegalArgumentException("Tangent generation encountered a degenerate UV triangle");
            }
            double reciprocal = 1.0 / determinant;
            Vector3 tangent = scale(subtract(scale(edgeOne, dvTwo), scale(edgeTwo, dvOne)), reciprocal);
            Vector3 bitangent = scale(subtract(scale(edgeTwo, duOne), scale(edgeOne, duTwo)), reciprocal);
            for (int index : new int[]{ia, ib, ic}) {
                tangentSums[index].add(tangent);
                bitangentSums[index].add(bitangent);
            }
        }
        List<Optional<Vector4>> result = new ArrayList<>(source.vertices().size());
        for (int index = 0; index < source.vertices().size(); index++) {
            Vector3 normal = normals.get(index);
            Vector3 tangent = normalize(subtract(tangentSums[index].value, scale(normal, dot(normal, tangentSums[index].value))));
            double handedness = dot(cross(normal, tangent), bitangentSums[index].value) < 0 ? -1.0 : 1.0;
            result.add(Optional.of(new Vector4(tangent.x(), tangent.y(), tangent.z(), handedness)));
        }
        return List.copyOf(result);
    }

    private static MutableVector[] vectors(int count) {
        MutableVector[] result = new MutableVector[count];
        for (int index = 0; index < count; index++) result[index] = new MutableVector();
        return result;
    }

    private static Vector3 subtract(Vector3 a, Vector3 b) { return new Vector3(a.x() - b.x(), a.y() - b.y(), a.z() - b.z()); }
    private static Vector3 scale(Vector3 value, double factor) { return new Vector3(value.x() * factor, value.y() * factor, value.z() * factor); }
    private static double dot(Vector3 a, Vector3 b) { return a.x() * b.x() + a.y() * b.y() + a.z() * b.z(); }
    private static Vector3 cross(Vector3 a, Vector3 b) { return new Vector3(a.y() * b.z() - a.z() * b.y(), a.z() * b.x() - a.x() * b.z(), a.x() * b.y() - a.y() * b.x()); }

    private static Vector3 normalize(Vector3 value) {
        double lengthSquared = dot(value, value);
        requireMagnitude(value, "Cannot normalize a zero-length vector");
        return scale(value, 1.0 / StrictMath.sqrt(lengthSquared));
    }

    private static void requireMagnitude(Vector3 value, String message) {
        if (dot(value, value) <= EPSILON_SQUARED) throw new IllegalArgumentException(message);
    }

    private static final class MutableVector {
        private Vector3 value = Vector3.ZERO;
        private void add(Vector3 addition) { value = new Vector3(value.x() + addition.x(), value.y() + addition.y(), value.z() + addition.z()); }
    }
}
