package com.planeguardian.assets.generation.triangulation;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.VertexId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtoMeshTriangulatorTest {
    @Test
    void concavePolygonTriangulatesDeterministicallyWithoutLosingCorners() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(0, 0, 0));
        VertexId b = builder.addVertex(new Vector3(2, 0, 0));
        VertexId c = builder.addVertex(new Vector3(1, 0.5, 0));
        VertexId d = builder.addVertex(new Vector3(2, 2, 0));
        VertexId e = builder.addVertex(new Vector3(0, 2, 0));
        builder.addFace(List.of(a, b, c, d, e));

        TriangulatedMesh first = ProtoMeshTriangulator.triangulate(builder.snapshot());
        TriangulatedMesh second = ProtoMeshTriangulator.triangulate(builder.snapshot());

        assertEquals(5, first.vertices().size());
        assertEquals(3, first.triangleCount());
        assertArrayEquals(first.indices(), second.indices());
        assertEquals(5, first.vertices().stream().map(TriangleVertex::sourceLoopId).distinct().count());
    }

    @Test
    void adjacentFacesKeepSeparateRenderVerticesForCornerSeams() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(0, 0, 0));
        VertexId b = builder.addVertex(new Vector3(1, 0, 0));
        VertexId c = builder.addVertex(new Vector3(1, 1, 0));
        VertexId d = builder.addVertex(new Vector3(0, 1, 0));
        builder.addFace(List.of(a, b, c));
        builder.addFace(List.of(a, c, d));

        TriangulatedMesh mesh = ProtoMeshTriangulator.triangulate(builder.snapshot());

        assertEquals(6, mesh.vertices().size());
        assertEquals(2, mesh.triangleCount());
    }

    @Test
    void yFacingPolygonRetainsItsThreeDimensionalWinding() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(0, 0, 0));
        VertexId b = builder.addVertex(new Vector3(0, 0, 1));
        VertexId c = builder.addVertex(new Vector3(1, 0, 1));
        VertexId d = builder.addVertex(new Vector3(1, 0, 0));
        builder.addFace(List.of(a, b, c, d));

        TriangulatedMesh mesh = ProtoMeshTriangulator.triangulate(builder.snapshot());
        int[] indices = mesh.indices();
        Vector3 first = mesh.vertices().get(indices[0]).position();
        Vector3 second = mesh.vertices().get(indices[1]).position();
        Vector3 third = mesh.vertices().get(indices[2]).position();
        Vector3 normal = new Vector3(0,
                (second.z() - first.z()) * (third.x() - first.x())
                        - (second.x() - first.x()) * (third.z() - first.z()),
                0);

        assertTrue(normal.y() > 0);
    }
}
