package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;

/** Trusted compiled provider for reusable VFX plugin configurations. */
public interface VfxProvider {
    StableId providerId();
    ContractVersion providerVersion();
    ContractVersion parameterSchemaVersion();
    boolean supports(StableId pluginId);
    VfxConfiguration configure(VfxGenerationRequest request);
}
