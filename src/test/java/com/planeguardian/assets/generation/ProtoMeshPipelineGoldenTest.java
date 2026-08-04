package com.planeguardian.assets.generation;

import com.planeguardian.assets.generation.adapters.gltf.GltfPrimitiveAdapter;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.determinism.NumericQuantizer;
import com.planeguardian.assets.generation.surface.MeshSurfaceProcessor;
import com.planeguardian.assets.generation.surface.NormalPolicy;
import com.planeguardian.assets.generation.topology.CornerAttributes;
import com.planeguardian.assets.generation.topology.ProtoMeshBuilder;
import com.planeguardian.assets.generation.topology.ProtoMeshFingerprints;
import com.planeguardian.assets.generation.topology.Vector2;
import com.planeguardian.assets.generation.triangulation.ProtoMeshTriangulator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pins the complete authoring-to-export geometry path against accidental drift. */
class ProtoMeshPipelineGoldenTest {
    @Test
    void canonicalQuadPipelineMatchesVersionOneGoldenData() {
        ProtoMeshBuilder builder = new ProtoMeshBuilder();
        var a = builder.addVertex(new Vector3(0, 0, 0));
        var b = builder.addVertex(new Vector3(2, 0, 0));
        var c = builder.addVertex(new Vector3(2, 1, 0));
        var d = builder.addVertex(new Vector3(0, 1, 0));
        builder.addFace(List.of(a, b, c, d), List.of(
                corner(0, 0), corner(1, 0), corner(1, 1), corner(0, 1)), Set.of("golden.surface"));

        var snapshot = builder.snapshot();
        String fingerprint = ProtoMeshFingerprints.compute(snapshot, new NumericQuantizer(0.001)).hex();
        var triangles = ProtoMeshTriangulator.triangulate(snapshot);
        var renderMesh = MeshSurfaceProcessor.process(triangles, NormalPolicy.SMOOTH_BY_SOURCE_VERTEX, true);
        var gltf = GltfPrimitiveAdapter.convert(renderMesh);

        assertEquals("73a525aca2f1c0906d6e0c4c2cd73915f9c547fefc0fbcd5d82a8fac5373306e", fingerprint);
        assertArrayEquals(new int[]{3, 0, 1, 1, 2, 3}, gltf.indices());
        assertArrayEquals(new float[]{0, 0, 0, 2, 0, 0, 2, 1, 0, 0, 1, 0}, gltf.positions());
        assertArrayEquals(new float[]{0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1}, gltf.normals());
        assertArrayEquals(new float[]{1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1}, gltf.tangents());
        assertArrayEquals(new float[]{0, 0, 1, 0, 1, 1, 0, 1}, gltf.textureCoordinates());
        assertArrayEquals(new float[]{0, 0, 0}, gltf.positionMinimum());
        assertArrayEquals(new float[]{2, 1, 0}, gltf.positionMaximum());
    }

    private static CornerAttributes corner(double u, double v) {
        return new CornerAttributes(Optional.of(new Vector2(u, v)), Optional.empty(), Map.of("mask", 0.5));
    }
}
