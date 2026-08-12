package com.planeguardian.assets.assetgenerator.tree.export;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.assetgenerator.tree.generation.RuntimeWeatherInput;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.assetgenerator.tree.generation.TreePresentationSettings;
import com.planeguardian.assets.assetgenerator.tree.generation.TreeWindResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TreeWindJmeAdapterTest {
    @Test
    void bindsNormalizedRuntimeWeatherAndDeterministicPartResponse() {
        TreePresentationSettings settings = TreePresentationSettings.defaults();
        Material material = TreePbrMaterialAdapter.create(new DesktopAssetManager(true), TreePbrMaterialAdapter.BARK,
                recipe(), settings);
        TreeWindResponse response = TreeWindResponse.forPart(TreePbrMaterialAdapter.BARK, settings, 42);
        RuntimeWeatherInput weather = new RuntimeWeatherInput(new Vector3(2, 0, 0), .6, 12.5);

        TreeWindJmeAdapter.bind(material, response, settings, weather);

        assertEquals((float) response.weight(), material.getParam("WindWeight").getValue());
        assertEquals((float) response.phaseOffset(), material.getParam("WindPhase").getValue());
        assertEquals((float) settings.windFrequency(), material.getParam("WindFrequency").getValue());
        assertEquals(new Vector3f(1, 0, 0), material.getParam("WindDirection").getValue());
        assertEquals(.6f, material.getParam("WindIntensity").getValue());
        assertEquals(12.5f, material.getParam("WindTime").getValue());
    }

    @Test
    void rejectsMissingBoundaryInputs() {
        TreePresentationSettings settings = TreePresentationSettings.defaults();
        Material material = TreePbrMaterialAdapter.create(new DesktopAssetManager(true), TreePbrMaterialAdapter.BARK,
                recipe(), settings);
        TreeWindResponse response = TreeWindResponse.forPart(TreePbrMaterialAdapter.BARK, settings, 42);
        RuntimeWeatherInput weather = RuntimeWeatherInput.calm();

        assertThrows(NullPointerException.class, () -> TreeWindJmeAdapter.bind(null, response, settings, weather));
        assertThrows(NullPointerException.class, () -> TreeWindJmeAdapter.bind(material, null, settings, weather));
        assertThrows(NullPointerException.class, () -> TreeWindJmeAdapter.bind(material, response, null, weather));
        assertThrows(NullPointerException.class, () -> TreeWindJmeAdapter.bind(material, response, settings, null));
    }

    private static MaterialRecipe recipe() {
        var resource = new com.planeguardian.assets.generation.api.GeneratedResourceRef(
                new com.planeguardian.assets.generation.api.StableId("material.wind-test"),
                com.planeguardian.assets.generation.api.ResourceKind.MATERIAL,
                new com.planeguardian.assets.generation.api.ContractVersion(1, 0));
        return new MaterialRecipe(resource, resource.version(), new com.planeguardian.assets.generation.api.StableId("pbr"),
                Map.of(), com.planeguardian.assets.generation.resources.material.MaterialRecipeFingerprints.identity(
                resource.resourceId(), resource.version(), resource.version(),
                new com.planeguardian.assets.generation.api.StableId("pbr"), Map.of()));
    }
}
