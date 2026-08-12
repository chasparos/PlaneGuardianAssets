package com.planeguardian.assets.assetgenerator.tree.generation;

/** Fixed structural LOD selection; adapters remain responsible for screen-size selection. */
public record TreeLodSettings(int tier, int branchLevelLimit) {
    public TreeLodSettings {
        if (tier < 0 || tier > 2) throw new IllegalArgumentException("tier must be in [0, 2]");
        if (branchLevelLimit < 0 || branchLevelLimit > 3) throw new IllegalArgumentException("branchLevelLimit must be in [0, 3]");
    }
}
