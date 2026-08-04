package com.planeguardian.assets.generation.surface;

public enum NormalPolicy {
    /** Require and retain every authored per-corner normal. */
    PRESERVE_AUTHORED,
    /** Average triangle normals only within each source polygon face. */
    FLAT_BY_FACE,
    /** Average triangle normals across corners sharing a source vertex. */
    SMOOTH_BY_SOURCE_VERTEX
}
