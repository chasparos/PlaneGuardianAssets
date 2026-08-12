package com.planeguardian.assets.assetgenerator.sample.export;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.planeguardian.assets.assetgenerator.sample.generation.vfx.SampleFireflySwarmVfxProvider;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.resources.vfx.VfxConfiguration;
import com.planeguardian.assets.generation.resources.vfx.VfxValue;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Realizes a validated {@code SampleFireflySwarmVfxProvider} configuration as a jME
 * {@link ParticleEmitter} swarm around already-resolved sample-shape sockets.
 *
 * <p>What: one glowing, additively-blended point-particle emitter per attachment,
 * bounded inside a sphere of the configured radius centered on the socket so the
 * fireflies visibly orbit the generated geometry rather than drifting away from it.
 * Why: mirrors the pattern {@code PollenMotesJmeAdapter} establishes for the Great
 * Tree so every reader of this codebase sees the same generate → configure → realize
 * shape for VFX, regardless of which generator they are looking at.
 *
 * <p>Soft contract: this adapter never invents configuration values itself - every
 * emitter parameter must come from the already-validated {@link VfxConfiguration};
 * an unresolved socket or unsupported plugin id must fail loudly instead of silently
 * skipping the attachment.
 */
public final class SampleFireflySwarmJmeAdapter {
    private SampleFireflySwarmJmeAdapter() {
    }

    public static Node create(VfxConfiguration configuration, Iterable<GeneratedSocket> sockets) {
        Objects.requireNonNull(configuration, "configuration");
        if (!configuration.request().pluginId().equals(SampleFireflySwarmVfxProvider.PLUGIN_ID)) {
            throw new IllegalArgumentException("Unsupported jME VFX plugin: " + configuration.request().pluginId());
        }
        Map<StableId, GeneratedSocket> index = java.util.stream.StreamSupport.stream(sockets.spliterator(), false)
                .collect(Collectors.toMap(GeneratedSocket::socketId, socket -> socket, (first, ignored) -> first));
        float radius = (float) scalar(configuration, "radius");
        float lifetime = (float) scalar(configuration, "lifetime");
        int count = Math.max(1, (int) Math.ceil(scalar(configuration, "count")));
        Node effects = new Node("vfx." + configuration.resource().resourceId().value());
        for (var attachment : configuration.request().attachments()) {
            GeneratedSocket socket = index.get(attachment.socketId());
            if (socket == null) throw new IllegalArgumentException("Unknown VFX socket: " + attachment.socketId());
            ParticleEmitter emitter = new ParticleEmitter("firefly.swarm." + attachment.socketId().value(),
                    ParticleMesh.Type.Point, count);
            // A swarm needs to stay bounded around the geometry rather than fly away with
            // the wind, so particles are born anywhere inside a local sphere and given a
            // small, weightless, omnidirectional wander instead of gravity/direction.
            emitter.setShape(new EmitterSphereShape(Vector3f.ZERO, radius));
            emitter.setGravity(0f, 0f, 0f);
            emitter.setLowLife(lifetime);
            emitter.setHighLife(lifetime);
            emitter.setParticlesPerSec(count / lifetime);
            emitter.setStartSize((float) scalar(configuration, "size"));
            emitter.setEndSize((float) scalar(configuration, "size"));
            emitter.setInitialVelocity(Vector3f.ZERO);
            emitter.setVelocityVariation(1f);
            emitter.setFacingVelocity(false);
            emitter.setStartColor(color(configuration));
            emitter.setEndColor(color(configuration));
            apply(emitter, socket.transform());
            apply(emitter, attachment.localTransform());
            effects.attachChild(emitter);
        }
        return effects;
    }

    private static double scalar(VfxConfiguration configuration, String id) {
        VfxValue value = configuration.request().parameters().get(new StableId(id));
        return ((VfxValue.Numeric) value).components().get(0);
    }

    private static ColorRGBA color(VfxConfiguration configuration) {
        var value = ((VfxValue.Numeric) configuration.request().parameters().get(new StableId("color"))).components();
        return new ColorRGBA(value.get(0).floatValue(), value.get(1).floatValue(), value.get(2).floatValue(), value.get(3).floatValue());
    }

    private static void apply(com.jme3.scene.Spatial spatial, Transform transform) {
        spatial.move((float) transform.translation().x(), (float) transform.translation().y(), (float) transform.translation().z());
        spatial.scale((float) transform.scale().x(), (float) transform.scale().y(), (float) transform.scale().z());
    }
}
