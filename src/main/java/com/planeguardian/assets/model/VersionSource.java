package com.planeguardian.assets.model;

/**
 * Describes how a particular {@link AssetVersion} came into existence.
 */
public enum VersionSource {
    /** Created by one of the procedural / parametric generator tools. */
    GENERATED,
    /** Imported from an external file (glTF, glb, j3o, obj, …). */
    IMPORTED,
    /** Exported from this tool, edited in a DCC (e.g. Blender), then re-imported. */
    MODIFIED_EXTERNAL
}
