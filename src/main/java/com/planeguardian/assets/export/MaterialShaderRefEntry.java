package com.planeguardian.assets.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight export representation of one material-to-shader binding,
 * embedded inside {@link AssetIndexEntry#getMaterialShaders()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialShaderRefEntry {

    /** Name of the GLTF material (matches {@code materials[].name}). */
    private String materialName;

    /** Shader identifier (matches a {@link ShaderRegistryEntry#getShaderId()}). */
    private String shaderId;

    /**
     * JSON object of per-instance parameter overrides, or {@code null} if
     * no overrides are present.
     */
    private String shaderParameters;
}
