package com.planeguardian.assets.generation.resources.cache;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.resources.texture.EncodedTextureArtifact;

import java.util.Objects;

/** A verified cache entry bound to its generated-resource identity. */
public record CachedResourceArtifact(
        ReproducibilityFingerprint generationFingerprint,
        EncodedTextureArtifact encodedArtifact) {
    public CachedResourceArtifact {
        Objects.requireNonNull(generationFingerprint, "generationFingerprint");
        Objects.requireNonNull(encodedArtifact, "encodedArtifact");
    }
}
