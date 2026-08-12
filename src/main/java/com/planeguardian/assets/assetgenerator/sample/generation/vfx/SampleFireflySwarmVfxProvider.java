package com.planeguardian.assets.assetgenerator.sample.generation.vfx;

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

/**
 * Sample-scoped VFX plugin describing a small swarm of glowing fireflies orbiting
 * around a generated shape.
 *
 * <p>What: an engine-neutral, validated configuration (color, count, lifetime, size,
 * swarm radius) for a bounded firefly swarm. Why: the sample generator is meant to be
 * a worked reference for the full generate → material → VFX flow other generators
 * use, so it must demonstrate a {@link VfxProvider} configured and fingerprinted the
 * same way {@code PollenMotesVfxProvider} is for the Great Tree, rather than the
 * generator wiring up engine particle state directly.
 *
 * <p>Soft contract: this provider must stay engine-neutral (no jME imports), reject
 * out-of-range or missing parameters eagerly (fail fast, not at render time), and
 * require at least one socket attachment so the swarm is always placed relative to
 * generated geometry rather than floating at the world origin. Only
 * {@code SampleFireflySwarmJmeAdapter} may translate this configuration into a
 * concrete jME {@code ParticleEmitter}.
 */
public final class SampleFireflySwarmVfxProvider implements VfxProvider {
    public static final StableId PROVIDER_ID = new StableId("vfx-provider.sample.firefly-swarm");
    public static final StableId PLUGIN_ID = new StableId("vfx.sample.firefly-swarm");
    private static final ContractVersion VERSION = new ContractVersion(1, 0);
    private static final Set<StableId> PARAMETERS = Set.of(
            new StableId("color"), new StableId("count"), new StableId("lifetime"),
            new StableId("size"), new StableId("radius"));

    @Override public StableId providerId() { return PROVIDER_ID; }
    @Override public ContractVersion providerVersion() { return VERSION; }
    @Override public ContractVersion parameterSchemaVersion() { return VERSION; }
    @Override public boolean supports(StableId pluginId) { return PLUGIN_ID.equals(pluginId); }

    @Override
    public VfxConfiguration configure(VfxGenerationRequest request) {
        if (!supports(request.pluginId())) throw new IllegalArgumentException("Unsupported VFX plugin: " + request.pluginId());
        if (!request.parameters().keySet().equals(PARAMETERS)) throw new IllegalArgumentException("Invalid firefly parameter set");
        requireScalar(request, "count", 1, 60);
        requireScalar(request, "lifetime", 0.5, 20);
        requireScalar(request, "size", 0.005, 0.5);
        requireScalar(request, "radius", 0.05, 20);
        VfxValue color = request.parameters().get(new StableId("color"));
        if (!(color instanceof VfxValue.Numeric numeric) || numeric.components().size() != 4
                || numeric.components().stream().anyMatch(value -> value < 0 || value > 1)) {
            throw new IllegalArgumentException("Firefly color must contain four components in [0, 1]");
        }
        if (request.attachments().isEmpty()) throw new IllegalArgumentException("Firefly swarm requires at least one socket attachment");
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
