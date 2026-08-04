package com.planeguardian.assets.generation.resources.vfx.providers;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.vfx.VfxConfiguration;
import com.planeguardian.assets.generation.resources.vfx.VfxFingerprints;
import com.planeguardian.assets.generation.resources.vfx.VfxGenerationRequest;
import com.planeguardian.assets.generation.resources.vfx.VfxProvider;
import com.planeguardian.assets.generation.resources.vfx.VfxValue;

import java.util.Set;

/** Shared bounded pollen/mote configuration used by trees and other ambient assets. */
public final class PollenMotesVfxProvider implements VfxProvider {
    public static final StableId PROVIDER_ID = new StableId("vfx-provider.pollen-motes");
    public static final StableId PLUGIN_ID = new StableId("vfx.pollen-motes");
    private static final ContractVersion VERSION = new ContractVersion(1, 0);
    private static final Set<StableId> PARAMETERS = Set.of(
            new StableId("color"), new StableId("lifetime"), new StableId("rate"), new StableId("size"));

    @Override public StableId providerId() { return PROVIDER_ID; }
    @Override public ContractVersion providerVersion() { return VERSION; }
    @Override public ContractVersion parameterSchemaVersion() { return VERSION; }
    @Override public boolean supports(StableId pluginId) { return PLUGIN_ID.equals(pluginId); }

    @Override
    public VfxConfiguration configure(VfxGenerationRequest request) {
        if (!supports(request.pluginId())) throw new IllegalArgumentException("Unsupported VFX plugin: " + request.pluginId());
        if (!request.parameters().keySet().equals(PARAMETERS)) throw new IllegalArgumentException("Invalid pollen parameter set");
        requireScalar(request, "rate", 0, 500);
        requireScalar(request, "lifetime", 0.1, 60);
        requireScalar(request, "size", 0.001, 2);
        VfxValue color = request.parameters().get(new StableId("color"));
        if (!(color instanceof VfxValue.Numeric numeric) || numeric.components().size() != 4
                || numeric.components().stream().anyMatch(value -> value < 0 || value > 1)) {
            throw new IllegalArgumentException("Pollen color must contain four components in [0, 1]");
        }
        if (request.attachments().isEmpty()) throw new IllegalArgumentException("Pollen requires at least one socket attachment");
        var fingerprint = VfxFingerprints.identity(PROVIDER_ID, VERSION, VERSION, request);
        return new VfxConfiguration(
                new GeneratedResourceRef(request.configurationId(), ResourceKind.VFX, VERSION),
                PROVIDER_ID, VERSION, VERSION, request, fingerprint);
    }

    private static void requireScalar(VfxGenerationRequest request, String name, double minimum, double maximum) {
        VfxValue value = request.parameters().get(new StableId(name));
        if (!(value instanceof VfxValue.Numeric numeric) || numeric.components().size() != 1
                || numeric.components().get(0) < minimum || numeric.components().get(0) > maximum) {
            throw new IllegalArgumentException(name + " must be a scalar in [" + minimum + ", " + maximum + "]");
        }
    }
}
