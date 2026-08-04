package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;

import java.util.Objects;

/** Fingerprinted upstream resource participating in generated texture provenance. */
public record TextureSource(
        GeneratedResourceRef resource,
        ReproducibilityFingerprint fingerprint) {
    public TextureSource {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
