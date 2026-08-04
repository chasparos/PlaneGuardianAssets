package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratedTextureContractsTest {
    private static final StableId PROVIDER = new StableId("texture.bark");
    private static final ContractVersion VERSION = new ContractVersion(1, 0);

    @Test
    void cacheIdentityCanonicalizesParameterAndSourceOrder() {
        TextureSource first = source("source.a", 1);
        TextureSource second = source("source.b", 2);
        Map<String, String> reverseParameters = new LinkedHashMap<>();
        reverseParameters.put("scale", "4");
        reverseParameters.put("cracks", "0.3");
        TextureGenerationRequest a = new TextureGenerationRequest(
                new StableId("texture.oak-bark"), 256, 256, 42,
                reverseParameters, List.of(second, first));
        TextureGenerationRequest b = new TextureGenerationRequest(
                new StableId("texture.oak-bark"), 256, 256, 42,
                Map.of("cracks", "0.3", "scale", "4"), List.of(first, second));

        assertEquals(a.parameters(), b.parameters());
        assertEquals(a.sources(), b.sources());
        assertEquals(fingerprint(a), fingerprint(b));
    }

    @Test
    void cacheIdentityChangesWithSeedDimensionsVersionAndSourceFingerprint() {
        TextureGenerationRequest baseline = request(42, 256, source("source.a", 1));

        assertNotEquals(fingerprint(baseline), fingerprint(request(43, 256, source("source.a", 1))));
        assertNotEquals(fingerprint(baseline), fingerprint(request(42, 512, source("source.a", 1))));
        assertNotEquals(fingerprint(baseline), fingerprint(request(42, 256, source("source.a", 2))));
        assertNotEquals(fingerprint(baseline), GeneratedTextureFingerprints.cacheIdentity(
                PROVIDER, new ContractVersion(1, 1), VERSION, baseline));
    }

    @Test
    void descriptorRequiresTextureKindMatchingIdAndDerivedFingerprint() {
        TextureGenerationRequest request = request(42, 256, source("source.a", 1));
        GeneratedResourceRef texture = new GeneratedResourceRef(request.textureId(), ResourceKind.TEXTURE, VERSION);
        GeneratedTexture generated = new GeneratedTexture(texture, PROVIDER, VERSION, VERSION,
                request, fingerprint(request), TextureColorSpace.SRGB,
                Map.of(TextureChannel.RED, new StableId("channel.albedo.r")));

        assertEquals(request.textureId(), generated.resource().resourceId());
        assertThrows(IllegalArgumentException.class, () -> new GeneratedTexture(
                new GeneratedResourceRef(request.textureId(), ResourceKind.MESH, VERSION),
                PROVIDER, VERSION, VERSION, request, fingerprint(request), TextureColorSpace.SRGB,
                Map.of(TextureChannel.RED, new StableId("channel.albedo.r"))));
        assertThrows(IllegalArgumentException.class, () -> new GeneratedTexture(
                texture, PROVIDER, VERSION, VERSION, request, bytes(99), TextureColorSpace.SRGB,
                Map.of(TextureChannel.RED, new StableId("channel.albedo.r"))));
    }

    @Test
    void requestAndDescriptorCollectionsAreImmutable() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("scale", "4");
        TextureGenerationRequest request = new TextureGenerationRequest(
                new StableId("texture.oak-bark"), 64, 64, 1, parameters, List.of());
        parameters.put("late", "change");
        GeneratedTexture generated = new GeneratedTexture(
                new GeneratedResourceRef(request.textureId(), ResourceKind.TEXTURE, VERSION),
                PROVIDER, VERSION, VERSION, request, fingerprint(request), TextureColorSpace.LINEAR,
                Map.of(TextureChannel.RED, new StableId("channel.height")));

        assertEquals(Map.of("scale", "4"), request.parameters());
        assertThrows(UnsupportedOperationException.class, () -> generated.channelSemantics().clear());
    }

    private static TextureGenerationRequest request(long seed, int size, TextureSource source) {
        return new TextureGenerationRequest(new StableId("texture.oak-bark"), size, size, seed,
                Map.of("scale", "4"), List.of(source));
    }

    private static ReproducibilityFingerprint fingerprint(TextureGenerationRequest request) {
        return GeneratedTextureFingerprints.cacheIdentity(PROVIDER, VERSION, VERSION, request);
    }

    private static TextureSource source(String id, int marker) {
        return new TextureSource(new GeneratedResourceRef(new StableId(id), ResourceKind.TEXTURE, VERSION), bytes(marker));
    }

    private static ReproducibilityFingerprint bytes(int marker) {
        byte[] bytes = new byte[32];
        bytes[0] = (byte) marker;
        return new ReproducibilityFingerprint(bytes);
    }
}
