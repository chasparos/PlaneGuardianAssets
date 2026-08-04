package com.planeguardian.assets.generation.resources.texture.providers;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.texture.GeneratedTexture;
import com.planeguardian.assets.generation.resources.texture.GeneratedTextureFingerprints;
import com.planeguardian.assets.generation.resources.texture.TextureChannel;
import com.planeguardian.assets.generation.resources.texture.TextureColorSpace;
import com.planeguardian.assets.generation.resources.texture.TextureGenerationRequest;

import java.util.Map;
import java.util.Set;

final class ProviderTextureSupport {
    static final ContractVersion VERSION = new ContractVersion(1, 0);

    private ProviderTextureSupport() {
    }

    static void requireParameters(TextureGenerationRequest request, Set<String> expected) {
        if (!request.parameters().keySet().equals(expected)) {
            throw new IllegalArgumentException("Expected texture parameters " + expected + " but received " + request.parameters().keySet());
        }
    }

    static double number(TextureGenerationRequest request, String name, double minimum, double maximum) {
        double value;
        try {
            value = Double.parseDouble(request.parameters().get(name));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Texture parameter " + name + " must be numeric", exception);
        }
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException("Texture parameter " + name + " must be in [" + minimum + ", " + maximum + "]");
        }
        return value;
    }

    static GeneratedTexture descriptor(
            StableId providerId, TextureGenerationRequest request, TextureColorSpace colorSpace,
            Map<TextureChannel, StableId> semantics) {
        return new GeneratedTexture(
                new GeneratedResourceRef(request.textureId(), ResourceKind.TEXTURE, VERSION),
                providerId, VERSION, VERSION, request,
                GeneratedTextureFingerprints.cacheIdentity(providerId, VERSION, VERSION, request),
                colorSpace, semantics);
    }

    static double noise(long seed, int x, int y) {
        long value = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53;
    }

    static int byteValue(double value) {
        return (int) StrictMath.round(StrictMath.max(0, StrictMath.min(1, value)) * 255);
    }

    static int pixelBytes(TextureGenerationRequest request, int bytesPerPixel) {
        try {
            return Math.multiplyExact(Math.multiplyExact(request.width(), request.height()), bytesPerPixel);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Texture pixel payload is too large", exception);
        }
    }
}
