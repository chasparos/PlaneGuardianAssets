package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.topology.VertexId;
import com.planeguardian.assets.generation.triangulation.ProtoMeshTriangulator;
import com.planeguardian.assets.generation.surface.MeshSurfaceProcessor;
import com.planeguardian.assets.generation.surface.NormalPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JmeMeshAdapterTest {
    @Test
    void convertsTriangulatedPositionsIndicesNormalsAndUvs() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(0, 0, 0));
        VertexId b = builder.addVertex(new Vector3(1, 0, 0));
        VertexId c = builder.addVertex(new Vector3(0, 1, 0));
        CornerAttributes attributes = new CornerAttributes(
                Optional.of(new Vector2(0, 0)), Optional.of(new Vector3(0, 0, 1)), Map.of());
        builder.addFace(List.of(a, b, c), List.of(attributes, attributes, attributes), Set.of());

        Mesh mesh = JmeMeshAdapter.convert(ProtoMeshTriangulator.triangulate(builder.snapshot()));

        assertEquals(3, mesh.getVertexCount());
        assertEquals(1, mesh.getTriangleCount());
        assertNotNull(mesh.getBuffer(VertexBuffer.Type.Position));
        assertNotNull(mesh.getBuffer(VertexBuffer.Type.Index));
        assertNotNull(mesh.getBuffer(VertexBuffer.Type.Normal));
        assertNotNull(mesh.getBuffer(VertexBuffer.Type.TexCoord));
    }

    @Test
    void rejectsFiniteDoublesThatWouldOverflowGpuFloats() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(Double.MAX_VALUE, 0, 0));
        VertexId b = builder.addVertex(new Vector3(0, 1, 0));
        VertexId c = builder.addVertex(new Vector3(0, 0, 1));
        builder.addFace(List.of(a, b, c));

        assertThrows(IllegalArgumentException.class,
                () -> JmeMeshAdapter.convert(ProtoMeshTriangulator.triangulate(builder.snapshot())));
    }

    @Test
    void processedMeshCarriesTangentsIntoJme() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(0, 0, 0));
        VertexId b = builder.addVertex(new Vector3(1, 0, 0));
        VertexId c = builder.addVertex(new Vector3(0, 1, 0));
        CornerAttributes uv0 = new CornerAttributes(Optional.of(new Vector2(0, 0)), Optional.empty(), Map.of());
        CornerAttributes uv1 = new CornerAttributes(Optional.of(new Vector2(1, 0)), Optional.empty(), Map.of());
        CornerAttributes uv2 = new CornerAttributes(Optional.of(new Vector2(0, 1)), Optional.empty(), Map.of());
        builder.addFace(List.of(a, b, c), List.of(uv0, uv1, uv2), Set.of());
        var renderMesh = MeshSurfaceProcessor.process(
                ProtoMeshTriangulator.triangulate(builder.snapshot()), NormalPolicy.FLAT_BY_FACE, true);

        Mesh mesh = JmeMeshAdapter.convert(renderMesh);

        assertNotNull(mesh.getBuffer(VertexBuffer.Type.Tangent));
        assertEquals(4, mesh.getBuffer(VertexBuffer.Type.Tangent).getNumComponents());
    }
}
