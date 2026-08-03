package com.planeguardian.assets.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a custom (non-standard) shader registered in the asset library.
 *
 * <p>Each custom shader is assigned a stable {@link #shaderId} that is embedded
 * in the {@code extras} field of GLTF materials when the asset is exported.
 * During export, identical shader definitions are consolidated into a single
 * entry in {@code shader_registry.json}.</p>
 *
 * <p>Standard JME3 shaders (e.g. {@code Lighting}, {@code Unshaded}) may also
 * be referenced via the same mechanism by setting {@link #standardJme3} to
 * {@code true}; they are NOT written into the library's shader registry.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomShader {

    private Long id;

    /**
     * Unique string identifier embedded in GLTF {@code extras.custom_shader_id}
     * (e.g. {@code "procedural_wind_bark"} or {@code "Common/MatDefs/Light/Lighting"}).
     */
    private String shaderId;

    /** Human-readable name shown in the tool UI. */
    private String displayName;

    /** Optional description of the shader's purpose. */
    private String description;

    /**
     * JSON object describing the parameter names, types, and default values
     * for this shader, e.g.
     * <pre>{@code
     * {
     *   "wind_sway_amplitude": {"type": "float", "default": 0.1},
     *   "wind_speed_multiplier": {"type": "float", "default": 1.0}
     * }
     * }</pre>
     */
    private String parameterSchema;

    /**
     * When {@code true} this entry represents a built-in JME3 shader.
     * It is referenced in GLTF material extras but is NOT written to the
     * library's {@code shader_registry.json} during export.
     */
    @Builder.Default
    private boolean standardJme3 = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return displayName != null ? displayName : shaderId;
    }
}
