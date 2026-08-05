package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.math.ColorRGBA;
import com.planeguardian.assets.generation.crystal.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CrystalMaterialAdapterTest {
    @Test
    void materialUsesTintedEmissiveAndTransparencyControls() {
        CrystalSemanticAdapter adapter = new CrystalSemanticAdapter();
        var profile = adapter.profile(new com.planeguardian.assets.generation.semantics.SemanticProfile(new com.planeguardian.assets.generation.api.ContractVersion(1,0), new java.util.TreeMap<>(java.util.Map.of(new com.planeguardian.assets.generation.api.StableId("lore.elemental"), new com.planeguardian.assets.generation.semantics.SemanticWheelValue(0,1,1, java.util.Optional.empty(), java.util.Optional.empty(), java.util.List.of()), new com.planeguardian.assets.generation.api.StableId("game.power"), new com.planeguardian.assets.generation.semantics.SemanticWheelValue(1,0,1, java.util.Optional.empty(), java.util.Optional.empty(), java.util.List.of())))));
        var recipe = CrystalMaterialRecipeFactory.create(CrystalParameters.defaults(), profile);
        var material = CrystalMaterialAdapter.create(new DesktopAssetManager(true), recipe, 0.5);
        ColorRGBA emissive = (ColorRGBA) material.getParam("Emissive").getValue();
        assertTrue(emissive.r > emissive.g || emissive.g > emissive.b || emissive.b > 0);
        assertFalse(emissive.equals(ColorRGBA.White));
        assertEquals(Boolean.TRUE, material.isTransparent());
        assertEquals(com.jme3.material.RenderState.FaceCullMode.Back,
                material.getAdditionalRenderState().getFaceCullMode());
        assertFalse(material.getAdditionalRenderState().isDepthWrite());
    }

    @Test
    void backFaceLayerUsesOppositeCullingAndTransparentQueueControls() {
        var recipe = CrystalMaterialRecipeFactory.create(CrystalParameters.defaults(),
                new CrystalSemanticAdapter().profile(new com.planeguardian.assets.generation.semantics.SemanticProfile(
                        new com.planeguardian.assets.generation.api.ContractVersion(1,0), new java.util.TreeMap<>())));
        var material = CrystalMaterialAdapter.createBackFace(new DesktopAssetManager(true), recipe, 0.5);

        assertTrue(material.isTransparent());
        assertEquals(com.jme3.material.RenderState.FaceCullMode.Front,
                material.getAdditionalRenderState().getFaceCullMode());
        assertFalse(material.getAdditionalRenderState().isDepthWrite());
    }
}
