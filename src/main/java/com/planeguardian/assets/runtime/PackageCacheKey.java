package com.planeguardian.assets.runtime;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** Version-scoped cache identity for a generated runtime product. */
public final class PackageCacheKey {
    private PackageCacheKey() {
    }

    public static String forAsset(PackageCompatibility compatibility, String generatorId, String generationFingerprint) {
        Objects.requireNonNull(compatibility, "compatibility");
        if (generatorId == null || generatorId.isBlank() || generationFingerprint == null || generationFingerprint.isBlank()) {
            throw new IllegalArgumentException("generatorId and generationFingerprint are required for a cache key");
        }
        String identity = compatibility.indexSchema() + '\0' + compatibility.runtimeApiVersion() + '\0'
                + compatibility.providerApiVersion() + '\0' + generatorId + '\0' + generationFingerprint;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder(64);
            for (byte value : digest) output.append(String.format("%02x", value));
            return output.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
