package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.cache.LocalGeneratedResourceCache;
import com.planeguardian.assets.assetgenerator.tree.generation.texture.providers.FoliageMaskTextureProvider;
import com.planeguardian.assets.assetgenerator.tree.generation.texture.providers.PainterlyBarkTextureProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PngTextureEncoderTest {
    @TempDir Path temporaryDirectory;

    @Test
    void encodesEverySupportedFormatAsDeterministicValidPng() throws IOException {
        PngTextureEncoder encoder = new PngTextureEncoder();
        for (TexturePixelFormat format : TexturePixelFormat.values()) {
            TexturePixels pixels = new TexturePixels(1, 1, format, source(format));
            byte[] first = encoder.encode(pixels);
            byte[] repeated = encoder.encode(pixels);

            assertArrayEquals(first, repeated);
            assertArrayEquals(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10},
                    java.util.Arrays.copyOf(first, 8));
            assertArrayEquals(expectedRow(format), decompressedImageData(first));
        }
    }

    @Test
    void storesAndDeduplicatesBarkAndFoliageArtifacts() throws IOException {
        LocalGeneratedResourceCache cache = new LocalGeneratedResourceCache(temporaryDirectory);
        PngTextureEncoder encoder = new PngTextureEncoder();
        List<GeneratedTextureProduct> products = List.of(
                new PainterlyBarkTextureProvider().generate(new TextureGenerationRequest(
                        new StableId("texture.bark"), 8, 8, 4,
                        Map.of("contrast", "0.7", "grainScale", "9"), List.of())),
                new FoliageMaskTextureProvider().generate(new TextureGenerationRequest(
                        new StableId("texture.foliage"), 8, 8, 4,
                        Map.of("clusterScale", "8", "coverage", "0.55"), List.of())));

        for (GeneratedTextureProduct product : products) {
            EncodedTextureArtifact encoded = encoder.encode(product,
                    new StableId("artifact." + product.descriptor().resource().resourceId().value()),
                    "textures/generated/" + product.descriptor().resource().resourceId().value() + ".png");
            var first = cache.store(product.descriptor().cacheFingerprint(), encoded);
            var repeated = cache.store(product.descriptor().cacheFingerprint(), encoded);

            assertEquals(first, repeated);
            assertTrue(cache.find(product.descriptor().cacheFingerprint()).isPresent());
            assertArrayEquals(encoded.bytes(), repeated.encodedArtifact().bytes());
        }
        try (var files = Files.list(temporaryDirectory)) {
            assertEquals(4, files.count());
        }
    }

    @Test
    void treatsCorruptedCachedBytesAsMissAndRemovesEntry() throws IOException {
        LocalGeneratedResourceCache cache = new LocalGeneratedResourceCache(temporaryDirectory);
        GeneratedTextureProduct product = product();
        EncodedTextureArtifact encoded = new PngTextureEncoder().encode(product,
                new StableId("artifact.test"), "textures/generated/test.png");
        cache.store(product.descriptor().cacheFingerprint(), encoded);
        Path data = temporaryDirectory.resolve(product.descriptor().cacheFingerprint().hex() + ".png");
        Files.write(data, new byte[]{1, 2, 3});

        assertFalse(cache.find(product.descriptor().cacheFingerprint()).isPresent());
        try (var files = Files.list(temporaryDirectory)) {
            assertEquals(0, files.count());
        }
    }

    private static GeneratedTextureProduct product() {
        TextureGenerationRequest request = new TextureGenerationRequest(new StableId("texture.test"), 1, 1, 1,
                Map.of(), List.of());
        GeneratedTexture descriptor = new GeneratedTexture(
                new GeneratedResourceRef(request.textureId(), ResourceKind.TEXTURE, new ContractVersion(1, 0)),
                new StableId("provider.test"), new ContractVersion(1, 0), new ContractVersion(1, 0), request,
                GeneratedTextureFingerprints.cacheIdentity(new StableId("provider.test"), new ContractVersion(1, 0),
                        new ContractVersion(1, 0), request),
                TextureColorSpace.SRGB, Map.of(TextureChannel.RED, new StableId("channel.test")));
        return new GeneratedTextureProduct(descriptor,
                new TexturePixels(1, 1, TexturePixelFormat.R8_UNORM, new byte[]{4}));
    }

    private static byte[] source(TexturePixelFormat format) {
        return switch (format) {
            case R8_UNORM -> new byte[]{1};
            case RG8_UNORM -> new byte[]{1, 2};
            case RGB8_UNORM -> new byte[]{1, 2, 3};
            case RGBA8_UNORM -> new byte[]{1, 2, 3, 4};
        };
    }

    private static byte[] expectedRow(TexturePixelFormat format) {
        return switch (format) {
            case R8_UNORM -> new byte[]{0, 1};
            case RG8_UNORM -> new byte[]{0, 1, 2, 0, (byte) 255};
            case RGB8_UNORM -> new byte[]{0, 1, 2, 3};
            case RGBA8_UNORM -> new byte[]{0, 1, 2, 3, 4};
        };
    }

    private static byte[] decompressedImageData(byte[] png) throws IOException {
        int index = 8;
        java.io.ByteArrayOutputStream data = new java.io.ByteArrayOutputStream();
        while (index < png.length) {
            int length = ByteBuffer.wrap(png, index, 4).getInt();
            String type = new String(png, index + 4, 4, java.nio.charset.StandardCharsets.US_ASCII);
            if ("IDAT".equals(type)) data.write(png, index + 8, length);
            index += 12 + length;
        }
        try (InflaterInputStream input = new InflaterInputStream(new ByteArrayInputStream(data.toByteArray()))) {
            return input.readAllBytes();
        }
    }
}
