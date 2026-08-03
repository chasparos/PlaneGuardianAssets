package com.planeguardian.assets.gltf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.planeguardian.assets.model.MaterialShaderRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GltfExtrasInjector#injectIntoJson}.
 */
class GltfExtrasInjectorTest {

    private static final String GLTF_ONE_MATERIAL = """
            {
              "asset": {"version": "2.0"},
              "materials": [
                {
                  "name": "Bark_Material",
                  "pbrMetallicRoughness": {
                    "baseColorFactor": [0.4, 0.3, 0.2, 1.0]
                  }
                }
              ]
            }
            """;

    private static final String GLTF_EXISTING_EXTRAS = """
            {
              "asset": {"version": "2.0"},
              "materials": [
                {
                  "name": "Bark_Material",
                  "extras": {
                    "existing_key": "existing_value"
                  }
                }
              ]
            }
            """;

    private static final String GLTF_NO_MATERIALS = """
            {
              "asset": {"version": "2.0"},
              "meshes": []
            }
            """;

    @Test
    void injectsExtrasIntoMatchingMaterial() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .materialName("Bark_Material")
                .shaderId("procedural_wind_bark")
                .shaderParameters("{\"wind_sway_amplitude\":0.15,\"wind_speed_multiplier\":1.2}")
                .build();

        String result = GltfExtrasInjector.injectIntoJson(GLTF_ONE_MATERIAL, List.of(ref));

        JsonObject root = JsonParser.parseString(result).getAsJsonObject();
        JsonObject material = root.getAsJsonArray("materials").get(0).getAsJsonObject();
        assertTrue(material.has("extras"), "material should have extras");
        JsonObject extras = material.getAsJsonObject("extras");
        assertEquals("procedural_wind_bark", extras.get("custom_shader_id").getAsString());
        assertTrue(extras.has("shader_parameters"));
        assertEquals(0.15, extras.getAsJsonObject("shader_parameters")
                .get("wind_sway_amplitude").getAsDouble(), 1e-9);
    }

    @Test
    void mergesWithExistingExtras() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .materialName("Bark_Material")
                .shaderId("procedural_wind_bark")
                .shaderParameters(null)
                .build();

        String result = GltfExtrasInjector.injectIntoJson(GLTF_EXISTING_EXTRAS, List.of(ref));

        JsonObject root = JsonParser.parseString(result).getAsJsonObject();
        JsonObject extras = root.getAsJsonArray("materials").get(0)
                .getAsJsonObject().getAsJsonObject("extras");
        // existing key preserved
        assertEquals("existing_value", extras.get("existing_key").getAsString());
        // new key added
        assertEquals("procedural_wind_bark", extras.get("custom_shader_id").getAsString());
    }

    @Test
    void noOpWhenNoMaterials() {
        String result = GltfExtrasInjector.injectIntoJson(GLTF_NO_MATERIALS, List.of(
                MaterialShaderRef.builder().materialName("X").shaderId("Y").build()));

        JsonObject root = JsonParser.parseString(result).getAsJsonObject();
        assertFalse(root.has("materials"));
    }

    @Test
    void skipsNonMatchingMaterials() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .materialName("NonExistent_Material")
                .shaderId("some_shader")
                .build();

        String result = GltfExtrasInjector.injectIntoJson(GLTF_ONE_MATERIAL, List.of(ref));

        JsonObject root = JsonParser.parseString(result).getAsJsonObject();
        JsonObject material = root.getAsJsonArray("materials").get(0).getAsJsonObject();
        assertFalse(material.has("extras"),
                "extras should not be injected for a non-matching material name");
    }

    @Test
    void noShaderParametersOmitsParametersKey() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .materialName("Bark_Material")
                .shaderId("procedural_wind_bark")
                .shaderParameters(null)
                .build();

        String result = GltfExtrasInjector.injectIntoJson(GLTF_ONE_MATERIAL, List.of(ref));

        JsonObject root = JsonParser.parseString(result).getAsJsonObject();
        JsonObject extras = root.getAsJsonArray("materials").get(0)
                .getAsJsonObject().getAsJsonObject("extras");
        assertEquals("procedural_wind_bark", extras.get("custom_shader_id").getAsString());
        assertFalse(extras.has("shader_parameters"),
                "shader_parameters should be absent when not provided");
    }
}
