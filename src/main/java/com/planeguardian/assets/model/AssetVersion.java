package com.planeguardian.assets.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable snapshot of an {@link Asset} at a particular point in time.
 *
 * <p>Version numbers start at 1 and increase monotonically per asset.
 * The current "active" version is always the highest {@code versionNumber}.</p>
 *
 * <p>Typical lifecycle:</p>
 * <ol>
 *   <li>v1 – {@link VersionSource#GENERATED} by AssetGeneratorTool</li>
 *   <li>Export for external editing, open in Blender</li>
 *   <li>v2 – {@link VersionSource#MODIFIED_EXTERNAL} imported back</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetVersion {

    private Long id;

    /** The parent {@link Asset} this version belongs to. */
    private Long assetId;

    /** Monotonically increasing per asset (1-based). */
    private int versionNumber;

    /** Absolute path to the file on disk for this version. */
    private String filePath;

    /** How this version was produced. */
    private VersionSource source;

    /** Free-form notes describing what changed in this version. */
    private String notes;

    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "v" + versionNumber + " [" + (source != null ? source : "?") + "]";
    }
}
