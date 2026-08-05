package com.planeguardian.assets.generation.semantics;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Objects;

/** Asset-family policy translating source Lore Map values into visual channels. */
public interface AssetSemanticAdapter {
    StableId adapterId();
    ContractVersion adapterVersion();
    StableId assetFamily();
    ResolvedVisualProfile resolve(SemanticProfile source, ResolutionContext context);

    record ResolutionContext(SemanticProfile host, double hostInfluence) {
        public ResolutionContext {
            if (!Double.isFinite(hostInfluence) || hostInfluence < 0 || hostInfluence > 1) {
                throw new IllegalArgumentException("hostInfluence must be in [0, 1]");
            }
            if (hostInfluence > 0) Objects.requireNonNull(host, "host");
        }
        public static ResolutionContext intrinsicOnly() { return new ResolutionContext(null, 0); }
    }
}
