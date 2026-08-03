package com.planeguardian.assets.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One entry in the exported {@code shader_registry.json}.
 *
 * <p>Standard JME3 shaders are referenced in material extras but are NOT
 * included in this registry.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShaderRegistryEntry {

    /** Unique string identifier embedded in GLTF {@code extras.custom_shader_id}. */
    private String shaderId;

    /** Human-readable name. */
    private String displayName;

    /** Optional description of the shader's purpose. */
    private String description;

    /**
     * JSON object describing parameter names, types, and default values.
     * May be {@code null} if no schema is defined.
     */
    private String parameterSchema;
}
