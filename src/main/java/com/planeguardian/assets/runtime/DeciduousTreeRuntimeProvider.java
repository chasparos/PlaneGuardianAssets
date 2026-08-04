package com.planeguardian.assets.runtime;

/** Runtime capability marker for the first trusted generated-asset provider. */
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
