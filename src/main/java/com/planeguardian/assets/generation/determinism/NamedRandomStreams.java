package com.planeguardian.assets.generation.determinism;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Derives independent streams from a root seed and stable UTF-8 stream name. */
public final class NamedRandomStreams {
    private NamedRandomStreams() {
    }

    public static DeterministicRandom open(long rootSeed, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Stream name must not be blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update("PlaneGuardianAssets.RandomStream/1\0".getBytes(StandardCharsets.UTF_8));
            digest.update(ByteBuffer.allocate(Long.BYTES).putLong(rootSeed).array());
            digest.update(name.getBytes(StandardCharsets.UTF_8));
            return new DeterministicRandom(ByteBuffer.wrap(digest.digest()).getLong());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by Java", exception);
        }
    }
}
