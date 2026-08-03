package com.planeguardian.assets.export;

import com.planeguardian.assets.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents one asset in the exported {@code asset_index.json}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetIndexEntry {
    private Long id;
    private String name;
    private AssetType assetType;
    /** Relative path inside the export directory (e.g. {@code assets/Jet_1.glb}). */
    private String exportedPath;
    /** Original absolute path on the authoring machine. */
    private String originalPath;
    /** Optional free-form JSON metadata copied from the library record. */
    private String metadata;
}
