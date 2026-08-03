package com.planeguardian.assets.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Persistent record representing a single asset in the library.
 * The actual binary file is stored on disk; only the path (and metadata)
 * are kept in H2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    private Long id;

    /** Human-readable name shown in the browser. */
    private String name;

    /** Absolute path to the asset file on disk (glb, j3o, png, …). */
    private String filePath;

    private AssetType assetType;

    /** When true, this asset is included in the next Export Library run. */
    @Builder.Default
    private boolean includeInExport = true;

    /** Optional free-form JSON for tags, LOD hints, etc. */
    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Used by JList cell renderer. */
    @Override
    public String toString() {
        return name != null ? name : "(Unnamed)";
    }
}

