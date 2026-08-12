package com.planeguardian.assets.assetgenerator.tree.export;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.planeguardian.assets.assetgenerator.tree.generation.RuntimeWeatherInput;
import com.planeguardian.assets.assetgenerator.tree.generation.TreePreviewFixture;
import com.planeguardian.assets.assetgenerator.tree.generation.DeciduousTreeStructureGenerator;
import com.planeguardian.assets.assetgenerator.tree.generation.TreePresentationSettings;
import com.planeguardian.assets.assetgenerator.tree.generation.TreeStructure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals(structural.parts().size() + crown.parts().size() + 1, scene.root().getChildren().size());
        assertEquals(structural.sockets().size(), ((com.jme3.scene.Node) scene.root().getChild("asset.sockets")).getQuantity());
        assertTrue(scene.root().getChildren().stream().filter(Geometry.class::isInstance).allMatch(child ->
                child instanceof Geometry geometry
                        && geometry.getShadowMode() == RenderQueue.ShadowMode.CastAndReceive
                        && geometry.getMesh().getTriangleCount() > 0
                        && geometry.getMaterial().getMaterialDef().getAssetName()
                                .equals("Common/MatDefs/Light/PBRLighting.j3md")));
        assertNotNull(scene.shadowRenderer(new DesktopAssetManager(true)));
        Camera camera = new Camera(1280, 720);
        scene.applyCamera(camera);
        assertEquals(15f, camera.getLocation().x);
        assertEquals(9f, camera.getLocation().y);
        assertEquals(18f, camera.getLocation().z);
    }

    @Test
    void rejectsShadowRendererForFixturesWithoutShadows() {
        TreeStructure structure = TreeStructure.defaults();
        var structural = DeciduousTreeStructureGenerator.generate(structure, structure.composition(), 71);
        var crown = DeciduousTreeStructureGenerator.generateCrown(structure, structure.composition(), 71);
        TreePreviewFixture unshadowed = new TreePreviewFixture(
                com.planeguardian.assets.generation.api.RenderTier.GAMEPLAY,
                new com.planeguardian.assets.generation.api.Vector3(15, 9, 18),
                new com.planeguardian.assets.generation.api.Vector3(0, 6, 0),
                new com.planeguardian.assets.generation.api.Vector3(-.5, -1, -.5), .45, false);
        var scene = TreePreviewJmeAdapter.create(new DesktopAssetManager(true), structural, crown, unshadowed,
                TreePresentationSettings.defaults(), RuntimeWeatherInput.calm(), 71);

        assertThrows(IllegalStateException.class, () -> scene.shadowRenderer(new DesktopAssetManager(true)));
    }
}
