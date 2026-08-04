package com.planeguardian.assets.generation.adapters.gltf;

import com.planeguardian.assets.generation.surface.RenderMesh;

import java.util.Arrays;
import java.util.Objects;

public final class GltfPrimitiveAdapter {
    private GltfPrimitiveAdapter() {
    }

    public static GltfPrimitiveData convert(RenderMesh source) {
        Objects.requireNonNull(source, "source");
        float[] positions = new float[source.vertices().size() * 3];
        float[] normals = new float[source.vertices().size() * 3];
        float[] tangents = source.hasTangents() ? new float[source.vertices().size() * 4] : new float[0];
        float[] textureCoordinates = source.hasTextureCoordinates() ? new float[source.vertices().size() * 2] : new float[0];
        float[] minimum = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY};
        float[] maximum = {Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (int index = 0; index < source.vertices().size(); index++) {
            var vertex = source.vertices().get(index);
            write3(positions, index * 3, vertex.position().x(), vertex.position().y(), vertex.position().z(), "position");
            write3(normals, index * 3, vertex.normal().x(), vertex.normal().y(), vertex.normal().z(), "normal");
            for (int axis = 0; axis < 3; axis++) {
                minimum[axis] = StrictMath.min(minimum[axis], positions[index * 3 + axis]);
                maximum[axis] = StrictMath.max(maximum[axis], positions[index * 3 + axis]);
            }
            if (tangents.length > 0) {
                var tangent = vertex.tangent().orElseThrow();
                write3(tangents, index * 4, tangent.x(), tangent.y(), tangent.z(), "tangent");
                tangents[index * 4 + 3] = checkedFloat(tangent.w(), "tangent handedness");
            }
            if (textureCoordinates.length > 0) {
                var uv = vertex.textureCoordinate().orElseThrow();
                textureCoordinates[index * 2] = checkedFloat(uv.x(), "texture coordinate");
                textureCoordinates[index * 2 + 1] = checkedFloat(uv.y(), "texture coordinate");
            }
        }
        if (source.vertices().isEmpty()) {
            Arrays.fill(minimum, 0);
            Arrays.fill(maximum, 0);
        }
        return new GltfPrimitiveData(positions, normals, tangents, textureCoordinates,
                source.indices(), minimum, maximum);
    }

    private static void write3(float[] target, int offset, double x, double y, double z, String name) {
        target[offset] = checkedFloat(x, name);
        target[offset + 1] = checkedFloat(y, name);
        target[offset + 2] = checkedFloat(z, name);
    }

    private static float checkedFloat(double value, String name) {
        float converted = (float) value;
        if (!Float.isFinite(converted)) throw new IllegalArgumentException(name + " cannot be represented by a glTF float");
        return converted;
    }
}
