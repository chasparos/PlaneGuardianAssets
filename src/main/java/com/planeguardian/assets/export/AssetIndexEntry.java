package com.planeguardian.assets.export;

import com.planeguardian.assets.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    /** Package-local GLB used when no compatible trusted runtime provider is present. */
    private String fallbackGltf;
    /** Canonical identity of generated data consumed by the runtime cache. */
    private String generationFingerprint;
    /** Version-scoped runtime cache identity derived from compatibility and provenance. */
    private String cacheKey;
    /** Stable generator identifier, if this asset has a runtime generator. */
    private String generatorId;
    /**
     * Material-to-shader bindings for this asset.  Each entry corresponds to
     * one GLTF material that uses a custom or standard JME3 shader.
     * The same information is also injected into the {@code extras} field of
     * each material in the exported GLTF/GLB file.
     */
    private List<MaterialShaderRefEntry> materialShaders;
}
