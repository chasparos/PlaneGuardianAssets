package com.planeguardian.assets.assetgenerator.tree.generation.vfx.providers;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.resources.vfx.VfxAttachment;
import com.planeguardian.assets.generation.resources.vfx.VfxGenerationRequest;
import com.planeguardian.assets.generation.resources.vfx.VfxProviderRegistry;
import com.planeguardian.assets.generation.resources.vfx.VfxValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PollenMotesVfxProviderTest {
    @Test
    void configuresBoundedPollenAndIsDiscoverable() {
        VfxProviderRegistry registry = VfxProviderRegistry.discover(Thread.currentThread().getContextClassLoader());
        var provider = registry.require(PollenMotesVfxProvider.PROVIDER_ID, PollenMotesVfxProvider.PLUGIN_ID);
        VfxGenerationRequest request = request(24);
        var first = provider.configure(request);
        var repeated = provider.configure(request);

        assertEquals(first.fingerprint(), repeated.fingerprint());
        assertEquals(PollenMotesVfxProvider.PROVIDER_ID, first.providerId());
    }

    @Test
    void rejectsUnboundedRateAndMissingAttachment() {
        PollenMotesVfxProvider provider = new PollenMotesVfxProvider();
        assertThrows(IllegalArgumentException.class, () -> provider.configure(request(900)));
        VfxGenerationRequest unattached = new VfxGenerationRequest(new StableId("vfx-config.test"),
                PollenMotesVfxProvider.PLUGIN_ID, 1, parameters(20), List.of());
        assertThrows(IllegalArgumentException.class, () -> provider.configure(unattached));
    }

    private static VfxGenerationRequest request(double rate) {
        return new VfxGenerationRequest(new StableId("vfx-config.tree-pollen"),
                PollenMotesVfxProvider.PLUGIN_ID, 19, parameters(rate),
                List.of(new VfxAttachment(new StableId("socket.crown"), Transform.IDENTITY)));
    }

    private static Map<StableId, VfxValue> parameters(double rate) {
        return Map.of(
                new StableId("rate"), new VfxValue.Numeric(List.of(rate)),
                new StableId("lifetime"), new VfxValue.Numeric(List.of(6.0)),
                new StableId("size"), new VfxValue.Numeric(List.of(0.04)),
                new StableId("color"), new VfxValue.Numeric(List.of(1.0, 0.86, 0.35, 0.7)));
    }
}
