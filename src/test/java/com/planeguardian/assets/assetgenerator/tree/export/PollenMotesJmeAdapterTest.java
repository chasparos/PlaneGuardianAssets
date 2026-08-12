package com.planeguardian.assets.assetgenerator.tree.export;

import com.jme3.effect.ParticleEmitter;
import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.Rotation;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.resources.vfx.VfxAttachment;
import com.planeguardian.assets.generation.resources.vfx.VfxGenerationRequest;
import com.planeguardian.assets.generation.resources.vfx.VfxValue;
import com.planeguardian.assets.assetgenerator.tree.generation.vfx.providers.PollenMotesVfxProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PollenMotesJmeAdapterTest {
    @Test
    void realizesConfiguredEmitterAtResolvedSocketAndLocalOffset() {
        StableId socketId = new StableId("socket.crown");
        Transform socketTransform = new Transform(new Vector3(1, 2, 3), Rotation.IDENTITY, new Vector3(2, 2, 2));
        Transform localTransform = new Transform(new Vector3(4, 5, 6), Rotation.IDENTITY, new Vector3(.5, .5, .5));
        var configuration = new PollenMotesVfxProvider().configure(request(socketId, localTransform));

        Node effects = PollenMotesJmeAdapter.create(configuration,
                List.of(new GeneratedSocket(socketId, new StableId("tree.crown"), socketTransform)));

        assertEquals("vfx.vfx-config.tree-pollen", effects.getName());
        assertEquals(1, effects.getChildren().size());
        ParticleEmitter emitter = (ParticleEmitter) effects.getChild(0);
        assertEquals("pollen.socket.crown", emitter.getName());
        assertEquals(24f, emitter.getParticlesPerSec());
        assertEquals(6f, emitter.getLowLife());
        assertEquals(.04f, emitter.getStartSize());
        assertEquals(1f, emitter.getStartColor().r);
        assertEquals(.7f, emitter.getStartColor().a);
        assertEquals(5f, emitter.getLocalTranslation().x);
        assertEquals(7f, emitter.getLocalTranslation().y);
        assertEquals(9f, emitter.getLocalTranslation().z);
        assertEquals(1f, emitter.getLocalScale().x);
    }

    @Test
    void rejectsUnresolvedSocket() {
        StableId socketId = new StableId("socket.crown");
        var configuration = new PollenMotesVfxProvider().configure(request(socketId, Transform.IDENTITY));

        assertThrows(IllegalArgumentException.class,
                () -> PollenMotesJmeAdapter.create(configuration, List.of()));
    }

    private static VfxGenerationRequest request(StableId socketId, Transform localTransform) {
        return new VfxGenerationRequest(new StableId("vfx-config.tree-pollen"),
                PollenMotesVfxProvider.PLUGIN_ID, 19, Map.of(
                new StableId("rate"), new VfxValue.Numeric(List.of(24.0)),
                new StableId("lifetime"), new VfxValue.Numeric(List.of(6.0)),
                new StableId("size"), new VfxValue.Numeric(List.of(.04)),
                new StableId("color"), new VfxValue.Numeric(List.of(1.0, .86, .35, .7))),
                List.of(new VfxAttachment(socketId, localTransform)));
    }
}
