package com.planeguardian.assets.assetgenerator.tree.runtime;

import com.planeguardian.assets.runtime.RuntimeAssetProvider;

/**
 * Runtime capability marker for the first trusted generated-asset provider.
 *
 * <p>This is asset-family-specific (it only ever supports the Great Tree
 * generator's ID) and therefore lives alongside the tree generator rather
 * than in the generic {@code com.planeguardian.assets.runtime} package, which
 * must stay free of any single asset family's identity. It implements the
 * generic {@link RuntimeAssetProvider} contract and is discovered only
 * through {@code ServiceLoader}.
 */
public final class DeciduousTreeRuntimeProvider implements RuntimeAssetProvider {
    @Override
    public String providerId() {
        return "pg.tree.deciduous";
    }

    @Override
    public int apiVersion() {
        return 1;
    }

    @Override
    public boolean supports(String generatorId) {
        return "pg.tree.deciduous/1".equals(generatorId);
    }
}
