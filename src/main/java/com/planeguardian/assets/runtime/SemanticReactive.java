package com.planeguardian.assets.runtime;

import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;

/** Optional capability that consumes already-resolved visual semantics. */
public interface SemanticReactive extends RuntimeCapability {
    LoadedAsset.SemanticApplicationResult applySemantics(ResolvedVisualProfile profile,
                                                          LoadedAsset.SemanticContext context);
}
