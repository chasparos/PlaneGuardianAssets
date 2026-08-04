package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Objects;

/** Validated engine-neutral configuration of one reusable VFX plugin. */
public record VfxConfiguration(
        GeneratedResourceRef resource,
        StableId providerId,
        ContractVersion providerVersion,
        ContractVersion parameterSchemaVersion,
        VfxGenerationRequest request,
        ReproducibilityFingerprint fingerprint) {
    public VfxConfiguration {
        Objects.requireNonNull(resource, "resource");
        if (resource.kind() != ResourceKind.VFX) throw new IllegalArgumentException("VFX configuration resource kind must be VFX");
        Objects.requireNonNull(providerId, "providerId");
        Objects.requireNonNull(providerVersion, "providerVersion");
        Objects.requireNonNull(parameterSchemaVersion, "parameterSchemaVersion");
        Objects.requireNonNull(request, "request");
        if (!resource.resourceId().equals(request.configurationId())) {
            throw new IllegalArgumentException("Resource and VFX configuration IDs must match");
        }
        Objects.requireNonNull(fingerprint, "fingerprint");
        ReproducibilityFingerprint expected = VfxFingerprints.identity(
                providerId, providerVersion, parameterSchemaVersion, request);
        if (!fingerprint.equals(expected)) throw new IllegalArgumentException("VFX fingerprint does not match configuration inputs");
    }
}
