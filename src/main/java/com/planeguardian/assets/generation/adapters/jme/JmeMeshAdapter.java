package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.planeguardian.assets.generation.triangulation.TriangleVertex;
import com.planeguardian.assets.generation.triangulation.TriangulatedMesh;
import com.planeguardian.assets.generation.surface.RenderMesh;

import java.util.Objects;

/** One-way render-boundary conversion; no jME type enters the generation core. */
public final class JmeMeshAdapter {
    private JmeMeshAdapter() {
    }

    public static Mesh convert(TriangulatedMesh source) {
        Objects.requireNonNull(source, "source");
        Mesh mesh = new Mesh();
        float[] positions = new float[source.vertices().size() * 3];
        for (int index = 0; index < source.vertices().size(); index++) {
            TriangleVertex vertex = source.vertices().get(index);
            positions[index * 3] = checkedFloat(vertex.position().x(), "position");
            positions[index * 3 + 1] = checkedFloat(vertex.position().y(), "position");
            positions[index * 3 + 2] = checkedFloat(vertex.position().z(), "position");
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, positions);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, source.indices());
        if (source.vertices().stream().allMatch(vertex -> vertex.attributes().normal().isPresent())) {
            float[] normals = new float[source.vertices().size() * 3];
            for (int index = 0; index < source.vertices().size(); index++) {
                var normal = source.vertices().get(index).attributes().normal().orElseThrow();
                normals[index * 3] = checkedFloat(normal.x(), "normal");
                normals[index * 3 + 1] = checkedFloat(normal.y(), "normal");
                normals[index * 3 + 2] = checkedFloat(normal.z(), "normal");
            }
            mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
        }
        if (source.vertices().stream().allMatch(vertex -> vertex.attributes().textureCoordinate().isPresent())) {
            float[] textureCoordinates = new float[source.vertices().size() * 2];
            for (int index = 0; index < source.vertices().size(); index++) {
                var uv = source.vertices().get(index).attributes().textureCoordinate().orElseThrow();
                textureCoordinates[index * 2] = checkedFloat(uv.x(), "texture coordinate");
                textureCoordinates[index * 2 + 1] = checkedFloat(uv.y(), "texture coordinate");
            }
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, textureCoordinates);
        }
        mesh.updateCounts();
        mesh.updateBound();
        mesh.setStatic();
        return mesh;
    }

    public static Mesh convert(RenderMesh source) {
        Objects.requireNonNull(source, "source");
        Mesh mesh = new Mesh();
        float[] positions = new float[source.vertices().size() * 3];
        float[] normals = new float[source.vertices().size() * 3];
        float[] tangents = source.hasTangents() ? new float[source.vertices().size() * 4] : new float[0];
        float[] textureCoordinates = source.hasTextureCoordinates() ? new float[source.vertices().size() * 2] : new float[0];
        for (int index = 0; index < source.vertices().size(); index++) {
            var vertex = source.vertices().get(index);
            positions[index * 3] = checkedFloat(vertex.position().x(), "position");
            positions[index * 3 + 1] = checkedFloat(vertex.position().y(), "position");
            positions[index * 3 + 2] = checkedFloat(vertex.position().z(), "position");
            normals[index * 3] = checkedFloat(vertex.normal().x(), "normal");
            normals[index * 3 + 1] = checkedFloat(vertex.normal().y(), "normal");
            normals[index * 3 + 2] = checkedFloat(vertex.normal().z(), "normal");
            if (tangents.length > 0) {
                var tangent = vertex.tangent().orElseThrow();
                tangents[index * 4] = checkedFloat(tangent.x(), "tangent");
                tangents[index * 4 + 1] = checkedFloat(tangent.y(), "tangent");
                tangents[index * 4 + 2] = checkedFloat(tangent.z(), "tangent");
                tangents[index * 4 + 3] = checkedFloat(tangent.w(), "tangent handedness");
            }
            if (textureCoordinates.length > 0) {
                var uv = vertex.textureCoordinate().orElseThrow();
                textureCoordinates[index * 2] = checkedFloat(uv.x(), "texture coordinate");
                textureCoordinates[index * 2 + 1] = checkedFloat(uv.y(), "texture coordinate");
            }
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, positions);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, source.indices());
        if (tangents.length > 0) mesh.setBuffer(VertexBuffer.Type.Tangent, 4, tangents);
        if (textureCoordinates.length > 0) mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, textureCoordinates);
        mesh.updateCounts();
        mesh.updateBound();
        mesh.setStatic();
        return mesh;
    }

    private static float checkedFloat(double value, String attribute) {
        float converted = (float) value;
        if (!Float.isFinite(converted)) {
            throw new IllegalArgumentException(attribute + " cannot be represented by a GPU float: " + value);
        }
        return converted;
    }
}
