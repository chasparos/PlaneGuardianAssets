package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.StableId;

/** Codec boundary for converting raw generated texture pixels to portable artifacts. */
public interface TextureEncoder {
    EncodedTextureArtifact encode(GeneratedTextureProduct product, StableId artifactId, String relativePath);
}
