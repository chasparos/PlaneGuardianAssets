package com.planeguardian.assets.runtime;

import com.planeguardian.assets.generation.api.StableId;

/** Optional composable behavior installed on a loaded asset. */
public interface RuntimeCapability {
    StableId capabilityId();
}
