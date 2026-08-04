package com.planeguardian.assets.generation.surface;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;
import com.planeguardian.assets.generation.triangulation.ProtoMeshTriangulator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeshSurfaceProcessorTest {
    @Test
    void uvMappedTriangleProducesExpectedNormalTangentAndHandedness() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(0, 0, 0));
        VertexId b = builder.addVertex(new Vector3(1, 0, 0));
        VertexId c = builder.addVertex(new Vector3(0, 1, 0));
        builder.addFace(List.of(a, b, c), List.of(
                corner(0, 0), corner(1, 0), corner(0, 1)), Set.of());

        RenderMesh mesh = MeshSurfaceProcessor.process(
                ProtoMeshTriangulator.triangulate(builder.snapshot()),
                NormalPolicy.SMOOTH_BY_SOURCE_VERTEX, true);

        for (RenderVertex vertex : mesh.vertices()) {
            assertVector(vertex.normal(), 0, 0, 1);
            Vector4 tangent = vertex.tangent().orElseThrow();
            assertEquals(1, tangent.x(), 1.0e-12);
            assertEquals(0, tangent.y(), 1.0e-12);
            assertEquals(0, tangent.z(), 1.0e-12);
            assertEquals(1, tangent.w(), 0);
        }
    }

    @Test
    void smoothingPolicyCrossesFacesOnlyWhenRequested() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId origin = builder.addVertex(Vector3.ZERO);
        VertexId x = builder.addVertex(new Vector3(1, 0, 0));
        VertexId y = builder.addVertex(new Vector3(0, 1, 0));
        VertexId z = builder.addVertex(new Vector3(0, 0, 1));
        builder.addFace(List.of(origin, x, y));
        builder.addFace(List.of(origin, y, z));
        var triangles = ProtoMeshTriangulator.triangulate(builder.snapshot());

        RenderMesh flat = MeshSurfaceProcessor.process(triangles, NormalPolicy.FLAT_BY_FACE, false);
        RenderMesh smooth = MeshSurfaceProcessor.process(triangles, NormalPolicy.SMOOTH_BY_SOURCE_VERTEX, false);

        List<RenderVertex> flatOrigin = flat.vertices().stream().filter(v -> v.sourceVertexId().equals(origin)).toList();
        assertVector(flatOrigin.get(0).normal(), 0, 0, 1);
        assertVector(flatOrigin.get(1).normal(), 1, 0, 0);
        double inverseSqrtTwo = 1.0 / StrictMath.sqrt(2);
        smooth.vertices().stream().filter(v -> v.sourceVertexId().equals(origin)).forEach(v ->
                assertVector(v.normal(), inverseSqrtTwo, 0, inverseSqrtTwo));
    }

    @Test
    void authoredPolicyAndTangentsRequireCompleteInputs() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(Vector3.ZERO);
        VertexId b = builder.addVertex(new Vector3(1, 0, 0));
        VertexId c = builder.addVertex(new Vector3(0, 1, 0));
        builder.addFace(List.of(a, b, c));
        var triangles = ProtoMeshTriangulator.triangulate(builder.snapshot());

        assertThrows(IllegalArgumentException.class,
                () -> MeshSurfaceProcessor.process(triangles, NormalPolicy.PRESERVE_AUTHORED, false));
        assertThrows(IllegalArgumentException.class,
                () -> MeshSurfaceProcessor.process(triangles, NormalPolicy.FLAT_BY_FACE, true));
    }

    private static CornerAttributes corner(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), Map.of());
    }

    private static void assertVector(Vector3 actual, double x, double y, double z) {
        assertEquals(x, actual.x(), 1.0e-12);
        assertEquals(y, actual.y(), 1.0e-12);
        assertEquals(z, actual.z(), 1.0e-12);
        assertTrue(Double.isFinite(actual.x() + actual.y() + actual.z()));
    }
}
