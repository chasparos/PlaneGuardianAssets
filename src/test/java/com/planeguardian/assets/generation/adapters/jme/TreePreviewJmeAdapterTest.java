package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.planeguardian.assets.generation.preview.RuntimeWeatherInput;
import com.planeguardian.assets.generation.preview.TreePreviewFixture;
import com.planeguardian.assets.generation.tree.DeciduousTreeStructureGenerator;
import com.planeguardian.assets.generation.tree.TreePresentationSettings;
import com.planeguardian.assets.generation.tree.TreeStructure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreePreviewJmeAdapterTest {
    @Test
    void realizesFixedGameplaySilhouetteLodAndShadowFixture() {
        TreeStructure structure = TreeStructure.defaults();
        var structural = DeciduousTreeStructureGenerator.generate(structure, structure.composition(), 71);
        var crown = DeciduousTreeStructureGenerator.generateCrown(structure, structure.composition(), 71);
        var scene = TreePreviewJmeAdapter.create(new DesktopAssetManager(true),
                structural, crown,
                TreePreviewFixture.gameplay(), TreePresentationSettings.defaults(),
                new RuntimeWeatherInput(new com.planeguardian.assets.generation.api.Vector3(1, 0, 0), .5, 2), 71);

        assertEquals(TreePreviewFixture.gameplay(), scene.fixture());
        assertEquals(structural.parts().size() + crown.parts().size(), scene.root().getChildren().size());
        assertTrue(scene.root().getChildren().stream().allMatch(child ->
                child instanceof Geometry geometry
                        && geometry.getShadowMode() == RenderQueue.ShadowMode.CastAndReceive
                        && geometry.getMesh().getTriangleCount() > 0
                        && geometry.getMaterial().getParam("WindWeight") != null));
        assertNotNull(scene.shadowRenderer(new DesktopAssetManager(true)));
        Camera camera = new Camera(1280, 720);
        scene.applyCamera(camera);
        assertEquals(15f, camera.getLocation().x);
    }
}
