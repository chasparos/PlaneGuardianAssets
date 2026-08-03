package com.planeguardian.assets.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Root object written to {@code asset_index.json}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetIndex {
    private String version;
    private LocalDateTime exportDate;
    private int totalAssets;
    private List<AssetIndexEntry> assets;
    /**
     * Consolidated registry of all custom shaders referenced by exported assets.
     * Standard JME3 shaders are excluded. Written separately to
     * {@code shader_registry.json} and also embedded here for convenience.
     */
    private ShaderRegistry shaderRegistry;
}
