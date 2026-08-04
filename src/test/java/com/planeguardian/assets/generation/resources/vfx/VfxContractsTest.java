package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VfxContractsTest {
    private static final StableId PROVIDER = new StableId("vfx-provider.particles");
    private static final StableId PLUGIN = new StableId("vfx.smoke-plume");
    private static final StableId CONFIGURATION = new StableId("vfx-config.tree-smoke");
    private static final ContractVersion VERSION = new ContractVersion(1, 0);

    @Test
    void requestCanonicalizesParametersAndAttachments() {
        Map<StableId, VfxValue> reversed = new LinkedHashMap<>();
        reversed.put(new StableId("rate"), new VfxValue.Numeric(List.of(12.0)));
        reversed.put(new StableId("enabled"), new VfxValue.Flag(true));
        VfxAttachment crown = new VfxAttachment(new StableId("socket.crown"), Transform.IDENTITY);
        VfxAttachment trunk = new VfxAttachment(new StableId("socket.trunk"), Transform.IDENTITY);

        VfxGenerationRequest a = new VfxGenerationRequest(CONFIGURATION, PLUGIN, 5,
                reversed, List.of(trunk, crown));
        VfxGenerationRequest b = new VfxGenerationRequest(CONFIGURATION, PLUGIN, 5,
                Map.of(new StableId("enabled"), new VfxValue.Flag(true),
                        new StableId("rate"), new VfxValue.Numeric(List.of(12.0))), List.of(crown, trunk));

        assertEquals(a.parameters(), b.parameters());
        assertEquals(a.attachments(), b.attachments());
        assertEquals(fingerprint(a), fingerprint(b));
    }

    @Test
    void exactResourcesAndAttachmentsParticipateInFingerprint() {
        VfxGenerationRequest baseline = request(new VfxValue.Resource(texture(), bytes(1)), List.of());
        VfxGenerationRequest changedResource = request(new VfxValue.Resource(texture(), bytes(2)), List.of());
        VfxGenerationRequest attached = request(new VfxValue.Resource(texture(), bytes(1)),
                List.of(new VfxAttachment(new StableId("socket.crown"), Transform.IDENTITY)));
        assertNotEquals(fingerprint(baseline), fingerprint(changedResource));
        assertNotEquals(fingerprint(baseline), fingerprint(attached));
    }

    @Test
    void configurationValidatesKindIdAndFingerprint() {
        VfxGenerationRequest request = request(new VfxValue.Flag(true), List.of());
        VfxConfiguration configuration = new VfxConfiguration(vfxResource(), PROVIDER, VERSION, VERSION,
                request, fingerprint(request));
        assertEquals(CONFIGURATION, configuration.resource().resourceId());
        assertThrows(IllegalArgumentException.class, () -> new VfxConfiguration(
                new GeneratedResourceRef(CONFIGURATION, ResourceKind.MATERIAL, VERSION),
                PROVIDER, VERSION, VERSION, request, fingerprint(request)));
        assertThrows(IllegalArgumentException.class, () -> new VfxConfiguration(
                vfxResource(), PROVIDER, VERSION, VERSION, request, bytes(9)));
    }

    @Test
    void registryIsOrderedAndRejectsDuplicateOrUnsupportedProviders() {
        StubProvider second = new StubProvider(new StableId("provider.z"), PLUGIN);
        StubProvider first = new StubProvider(new StableId("provider.a"), PLUGIN);
        VfxProviderRegistry registry = VfxProviderRegistry.of(List.of(second, first));
        assertEquals(List.of(first.providerId(), second.providerId()), List.copyOf(registry.providers().keySet()));
        assertEquals(first, registry.require(first.providerId(), PLUGIN));
        assertThrows(IllegalArgumentException.class, () -> VfxProviderRegistry.of(List.of(first,
                new StubProvider(first.providerId(), PLUGIN))));
        assertThrows(IllegalArgumentException.class, () -> registry.require(first.providerId(), new StableId("vfx.unknown")));
    }

    private static VfxGenerationRequest request(VfxValue value, List<VfxAttachment> attachments) {
        return new VfxGenerationRequest(CONFIGURATION, PLUGIN, 7,
                Map.of(new StableId("input"), value), attachments);
    }

    private static ReproducibilityFingerprint fingerprint(VfxGenerationRequest request) {
        return VfxFingerprints.identity(PROVIDER, VERSION, VERSION, request);
    }

    private static GeneratedResourceRef vfxResource() {
        return new GeneratedResourceRef(CONFIGURATION, ResourceKind.VFX, VERSION);
    }

    private static GeneratedResourceRef texture() {
        return new GeneratedResourceRef(new StableId("texture.smoke-noise"), ResourceKind.TEXTURE, VERSION);
    }

    private static ReproducibilityFingerprint bytes(int marker) {
        byte[] bytes = new byte[32];
        bytes[0] = (byte) marker;
        return new ReproducibilityFingerprint(bytes);
    }

    private record StubProvider(StableId providerId, StableId supported) implements VfxProvider {
        @Override public ContractVersion providerVersion() { return VERSION; }
        @Override public ContractVersion parameterSchemaVersion() { return VERSION; }
        @Override public boolean supports(StableId pluginId) { return supported.equals(pluginId); }
        @Override public VfxConfiguration configure(VfxGenerationRequest request) {
            return new VfxConfiguration(new GeneratedResourceRef(request.configurationId(), ResourceKind.VFX, VERSION),
                    providerId, VERSION, VERSION, request,
                    VfxFingerprints.identity(providerId, VERSION, VERSION, request));
        }
    }
}
