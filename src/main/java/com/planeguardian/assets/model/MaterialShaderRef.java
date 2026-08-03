package com.planeguardian.assets.model;

import lombok.*;

/**
 * Binds a named material inside an asset file to a {@link CustomShader}.
 *
 * <p>One record is stored per unique (asset, materialName) combination.
 * At export time the injector reads these records and writes the
 * corresponding {@code extras} block into each GLTF material:</p>
 * <pre>{@code
 * "extras": {
 *   "custom_shader_id": "<shaderId>",
 *   "shader_parameters": { <shaderParameters as object> }
 * }
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialShaderRef {

    private Long id;

    /** FK to {@code assets.id}. */
    private Long assetId;

    /** Name of the material inside the GLTF/GLB file (matches {@code materials[].name}). */
    private String materialName;

    /**
     * References {@link CustomShader#getShaderId()}.
     * For JME3 standard shaders this is the JME3 shader path
     * (e.g. {@code "Common/MatDefs/Light/Lighting"}).
     */
    private String shaderId;

    /**
     * JSON object of parameter overrides specific to this material instance,
     * e.g. {@code {"wind_sway_amplitude": 0.15, "wind_speed_multiplier": 1.2}}.
     * May be {@code null} if no overrides are needed.
     */
    private String shaderParameters;
}
