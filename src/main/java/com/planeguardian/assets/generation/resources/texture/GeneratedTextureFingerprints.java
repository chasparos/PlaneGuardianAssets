package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;

public final class GeneratedTextureFingerprints {
    private GeneratedTextureFingerprints() {
    }

    public static ReproducibilityFingerprint cacheIdentity(
            StableId providerId,
            ContractVersion providerVersion,
            ContractVersion parameterSchemaVersion,
            TextureGenerationRequest request) {
        FingerprintBuilder fingerprint = new FingerprintBuilder()
                .addString("generated-texture-cache/1")
                .addId(providerId)
                .addLong(providerVersion.major()).addLong(providerVersion.minor())
                .addLong(parameterSchemaVersion.major()).addLong(parameterSchemaVersion.minor())
                .addId(request.textureId()).addLong(request.width()).addLong(request.height()).addLong(request.seed());
        request.parameters().forEach((name, value) -> fingerprint.addString(name).addString(value));
        request.sources().forEach(source -> fingerprint
                .addId(source.resource().resourceId())
                .addString(source.resource().kind().name())
                .addLong(source.resource().version().major()).addLong(source.resource().version().minor())
                .addString(source.fingerprint().hex()));
        return fingerprint.build();
    }
}
