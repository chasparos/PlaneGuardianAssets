package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;

/** Reusable texture provider contract; persistence and image encoding are separate boundaries. */
public interface GeneratedTextureProvider {
    StableId providerId();
    ContractVersion providerVersion();
    ContractVersion parameterSchemaVersion();
    GeneratedTexture describe(TextureGenerationRequest request);
}
