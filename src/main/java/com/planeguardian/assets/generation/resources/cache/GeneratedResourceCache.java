package com.planeguardian.assets.generation.resources.cache;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.resources.texture.EncodedTextureArtifact;

import java.io.IOException;
import java.util.Optional;

/** Persistence boundary for generated artifacts keyed by their validated generation identity. */
public interface GeneratedResourceCache {
    Optional<CachedResourceArtifact> find(ReproducibilityFingerprint generationFingerprint) throws IOException;

    CachedResourceArtifact store(ReproducibilityFingerprint generationFingerprint,
                                 EncodedTextureArtifact encodedArtifact) throws IOException;
}
