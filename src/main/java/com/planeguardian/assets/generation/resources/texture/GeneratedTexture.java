package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** First-class generated texture identity and provenance, independent of pixel storage. */
public record GeneratedTexture(
        GeneratedResourceRef resource,
        StableId providerId,
        ContractVersion providerVersion,
        ContractVersion parameterSchemaVersion,
        TextureGenerationRequest request,
        ReproducibilityFingerprint cacheFingerprint,
        TextureColorSpace colorSpace,
        Map<TextureChannel, StableId> channelSemantics) {
    public GeneratedTexture {
        Objects.requireNonNull(resource, "resource");
        if (resource.kind() != ResourceKind.TEXTURE) throw new IllegalArgumentException("Generated texture resource kind must be TEXTURE");
        Objects.requireNonNull(providerId, "providerId");
        Objects.requireNonNull(providerVersion, "providerVersion");
        Objects.requireNonNull(parameterSchemaVersion, "parameterSchemaVersion");
        Objects.requireNonNull(request, "request");
        if (!resource.resourceId().equals(request.textureId())) throw new IllegalArgumentException("Resource and request texture IDs must match");
        Objects.requireNonNull(cacheFingerprint, "cacheFingerprint");
        ReproducibilityFingerprint expected = GeneratedTextureFingerprints.cacheIdentity(
                providerId, providerVersion, parameterSchemaVersion, request);
        if (!cacheFingerprint.equals(expected)) throw new IllegalArgumentException("Texture cache fingerprint does not match its identity inputs");
        Objects.requireNonNull(colorSpace, "colorSpace");
        TreeMap<TextureChannel, StableId> ordered = new TreeMap<>();
        ordered.putAll(channelSemantics);
        channelSemantics = Collections.unmodifiableMap(ordered);
        if (channelSemantics.isEmpty()) throw new IllegalArgumentException("At least one channel semantic is required");
    }
}
