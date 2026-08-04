package com.planeguardian.assets.generation.adapters.gltf;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.surface.MeshSurfaceProcessor;
import com.planeguardian.assets.generation.surface.NormalPolicy;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GltfPrimitiveAdapterTest {
    @Test
    void producesAccessorReadyArraysAndPositionBounds() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        VertexId a = builder.addVertex(new Vector3(-2, 0, 3));
        VertexId b = builder.addVertex(new Vector3(4, 0, 3));
        VertexId c = builder.addVertex(new Vector3(-2, 5, 3));
        builder.addFace(List.of(a, b, c), List.of(
                corner(0, 0), corner(1, 0), corner(0, 1)), Set.of());
        var renderMesh = MeshSurfaceProcessor.process(
                ProtoMeshTriangulator.triangulate(builder.snapshot()),
                NormalPolicy.FLAT_BY_FACE, true);

        GltfPrimitiveData primitive = GltfPrimitiveAdapter.convert(renderMesh);

        assertEquals(9, primitive.positions().length);
        assertEquals(9, primitive.normals().length);
        assertEquals(12, primitive.tangents().length);
        assertEquals(6, primitive.textureCoordinates().length);
        assertEquals(3, primitive.indices().length);
        assertArrayEquals(new float[]{-2, 0, 3}, primitive.positionMinimum());
        assertArrayEquals(new float[]{4, 5, 3}, primitive.positionMaximum());
        assertTrue(primitive.hasTangents());
    }

    @Test
    void returnedArraysAreDefensiveCopies() {
        GltfPrimitiveData data = new GltfPrimitiveData(
                new float[]{1, 2, 3}, new float[]{0, 1, 0}, new float[0],
                new float[0], new int[]{0, 0, 0}, new float[]{1, 2, 3}, new float[]{1, 2, 3});
        float[] positions = data.positions();
        positions[0] = 99;
        assertEquals(1, data.positions()[0]);
    }

    private static CornerAttributes corner(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), Map.of());
    }
}
