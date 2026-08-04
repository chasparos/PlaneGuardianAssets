package com.planeguardian.assets.generation.adapters.gltf;

/** glTF accessor-ready primitive arrays; actual container serialization is separate. */
public final class GltfPrimitiveData {
    private final float[] positions;
    private final float[] normals;
    private final float[] tangents;
    private final float[] textureCoordinates;
    private final int[] indices;
    private final float[] positionMinimum;
    private final float[] positionMaximum;

    public GltfPrimitiveData(float[] positions, float[] normals, float[] tangents,
                             float[] textureCoordinates, int[] indices,
                             float[] positionMinimum, float[] positionMaximum) {
        this.positions = positions.clone();
        this.normals = normals.clone();
        this.tangents = tangents.clone();
        this.textureCoordinates = textureCoordinates.clone();
        this.indices = indices.clone();
        this.positionMinimum = positionMinimum.clone();
        this.positionMaximum = positionMaximum.clone();
    }

    public float[] positions() { return positions.clone(); }
    public float[] normals() { return normals.clone(); }
    public float[] tangents() { return tangents.clone(); }
    public float[] textureCoordinates() { return textureCoordinates.clone(); }
    public int[] indices() { return indices.clone(); }
    public float[] positionMinimum() { return positionMinimum.clone(); }
    public float[] positionMaximum() { return positionMaximum.clone(); }
    public boolean hasTangents() { return tangents.length > 0; }
    public boolean hasTextureCoordinates() { return textureCoordinates.length > 0; }
}
