package com.planeguardian.assets.assetgenerator.tree.export;

import com.jme3.scene.Node;
import com.planeguardian.assets.generation.adapters.jme.JmeAssetFamilyBinding;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.LoadedAsset;

/** Discovered binding that installs the tree's reusable runtime capabilities. */
public final class TreeJmeAssetFamilyBinding implements JmeAssetFamilyBinding {
    @Override
    public boolean supports(String generatorId) {
        return "pg.tree.deciduous/1".equals(generatorId);
    }

    @Override
    public LoadedAsset<Node> wrap(StableId assetId, Node root) {
        return TreeLoadedAssetFactory.wrap(assetId, root);
    }
}
