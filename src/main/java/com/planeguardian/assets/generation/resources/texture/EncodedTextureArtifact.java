package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.resources.ResourceArtifact;

import java.util.Arrays;
import java.util.Objects;

/** Immutable encoded texture artifact and its validated storage-neutral metadata. */
public final class EncodedTextureArtifact {
    private final ResourceArtifact artifact;
    private final byte[] bytes;

    public EncodedTextureArtifact(ResourceArtifact artifact, byte[] bytes) {
        this.artifact = Objects.requireNonNull(artifact, "artifact");
        this.bytes = Objects.requireNonNull(bytes, "bytes").clone();
        if (artifact.byteLength() != bytes.length) {
            throw new IllegalArgumentException("Artifact byte length must match encoded bytes");
        }
        if (!PngTextureEncoder.contentFingerprint(bytes).equals(artifact.contentFingerprint())) {
            throw new IllegalArgumentException("Artifact content fingerprint must match encoded bytes");
        }
    }

    public ResourceArtifact artifact() {
        return artifact;
    }

    public byte[] bytes() {
        return bytes.clone();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof EncodedTextureArtifact that
                && artifact.equals(that.artifact)
                && Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return 31 * artifact.hashCode() + Arrays.hashCode(bytes);
    }
}
