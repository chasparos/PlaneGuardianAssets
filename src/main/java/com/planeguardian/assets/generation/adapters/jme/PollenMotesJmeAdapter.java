package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.resources.vfx.VfxConfiguration;
import com.planeguardian.assets.generation.resources.vfx.VfxValue;
import com.planeguardian.assets.generation.resources.vfx.providers.PollenMotesVfxProvider;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Realizes the trusted shared pollen configuration against already-resolved tree sockets. */
public final class PollenMotesJmeAdapter {
    private PollenMotesJmeAdapter() {
    }

    public static Node create(VfxConfiguration configuration, Iterable<GeneratedSocket> sockets) {
        Objects.requireNonNull(configuration, "configuration");
        if (!configuration.request().pluginId().equals(PollenMotesVfxProvider.PLUGIN_ID)) {
            throw new IllegalArgumentException("Unsupported jME VFX plugin: " + configuration.request().pluginId());
        }
        Map<StableId, GeneratedSocket> index = java.util.stream.StreamSupport.stream(sockets.spliterator(), false)
                .collect(Collectors.toMap(GeneratedSocket::socketId, socket -> socket, (first, ignored) -> first));
        Node effects = new Node("vfx." + configuration.resource().resourceId().value());
        for (var attachment : configuration.request().attachments()) {
            GeneratedSocket socket = index.get(attachment.socketId());
            if (socket == null) throw new IllegalArgumentException("Unknown VFX socket: " + attachment.socketId());
            ParticleEmitter emitter = new ParticleEmitter("pollen." + attachment.socketId().value(), ParticleMesh.Type.Triangle,
                    Math.max(1, (int) Math.ceil(scalar(configuration, "rate"))));
            emitter.setParticlesPerSec((float) scalar(configuration, "rate"));
            emitter.setLowLife((float) scalar(configuration, "lifetime"));
            emitter.setHighLife((float) scalar(configuration, "lifetime"));
            emitter.setStartSize((float) scalar(configuration, "size"));
            emitter.setEndSize((float) scalar(configuration, "size"));
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
