package com.planeguardian.assets.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Root object written to {@code shader_registry.json} during export.
 *
 * <p>All custom shaders referenced by exported assets are consolidated here
 * (identical shaders deduplicated by {@code shaderId}).  Standard JME3 shaders
 * are excluded.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShaderRegistry {

    private String version;
    private LocalDateTime exportDate;
    private int totalShaders;
    private List<ShaderRegistryEntry> shaders;
}
