package com.planeguardian.assets.generation.resources.texture.providers;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.texture.GeneratedTextureProviderRegistry;
import com.planeguardian.assets.generation.resources.texture.TextureColorSpace;
import com.planeguardian.assets.generation.resources.texture.TextureGenerationRequest;
import com.planeguardian.assets.generation.resources.texture.TexturePixelFormat;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InitialTextureProvidersTest {
    @Test
    void barkIsDeterministicSrgbRgbaAndSeedSensitive() {
        PainterlyBarkTextureProvider provider = new PainterlyBarkTextureProvider();
        var first = provider.generate(bark(7));
        var repeated = provider.generate(bark(7));
        var changed = provider.generate(bark(8));

        assertEquals(TextureColorSpace.SRGB, first.descriptor().colorSpace());
        assertEquals(TexturePixelFormat.RGBA8_UNORM, first.pixels().format());
        assertArrayEquals(first.pixels().bytes(), repeated.pixels().bytes());
        assertFalse(java.util.Arrays.equals(first.pixels().bytes(), changed.pixels().bytes()));
        assertNotEquals(first.descriptor().cacheFingerprint(), changed.descriptor().cacheFingerprint());
    }

    @Test
    void foliageProducesDeterministicLinearCoverageWithBothStates() {
        FoliageMaskTextureProvider provider = new FoliageMaskTextureProvider();
        var product = provider.generate(new TextureGenerationRequest(new StableId("texture.foliage-mask"),
                32, 32, 11, Map.of("clusterScale", "8", "coverage", "0.55"), List.of()));
        byte[] pixels = product.pixels().bytes();

        assertEquals(TextureColorSpace.LINEAR, product.descriptor().colorSpace());
        assertEquals(TexturePixelFormat.R8_UNORM, product.pixels().format());
        assertFalse(java.util.Arrays.stream(toUnsigned(pixels)).allMatch(value -> value == 0));
        assertFalse(java.util.Arrays.stream(toUnsigned(pixels)).allMatch(value -> value == 255));
    }

    @Test
    void providersRejectIncompleteOrOutOfRangeParameters() {
        PainterlyBarkTextureProvider provider = new PainterlyBarkTextureProvider();
        assertThrows(IllegalArgumentException.class, () -> provider.generate(new TextureGenerationRequest(
                new StableId("texture.bark"), 8, 8, 1, Map.of("contrast", "0.5"), List.of())));
        assertThrows(IllegalArgumentException.class, () -> provider.generate(new TextureGenerationRequest(
                new StableId("texture.bark"), 8, 8, 1,
                Map.of("contrast", "2", "grainScale", "8"), List.of())));
    }

    @Test
    void textureProvidersAreDiscoverableByStableId() {
        GeneratedTextureProviderRegistry registry = GeneratedTextureProviderRegistry.discover(
                Thread.currentThread().getContextClassLoader());
        assertEquals(2, registry.providers().size());
        assertEquals(PainterlyBarkTextureProvider.class, registry.require(PainterlyBarkTextureProvider.ID).getClass());
    }

    private static TextureGenerationRequest bark(long seed) {
        return new TextureGenerationRequest(new StableId("texture.bark"), 16, 16, seed,
                Map.of("contrast", "0.7", "grainScale", "9"), List.of());
    }

    private static int[] toUnsigned(byte[] bytes) {
        int[] values = new int[bytes.length];
        for (int index = 0; index < bytes.length; index++) values[index] = Byte.toUnsignedInt(bytes[index]);
        return values;
    }
}
