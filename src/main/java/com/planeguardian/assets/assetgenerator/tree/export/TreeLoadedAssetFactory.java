package com.planeguardian.assets.assetgenerator.tree.export;

import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.ComposableLoadedAsset;
import com.planeguardian.assets.runtime.LoadedAsset;
import java.util.List;

/** Installs the standard reusable runtime capabilities on a loaded tree node. */
public final class TreeLoadedAssetFactory {
    private TreeLoadedAssetFactory() {}
    public static LoadedAsset<Node> wrap(StableId assetId, Node root) {
        return new ComposableLoadedAsset<>(assetId, root,
                List.of(new TreeSemanticJmeCapability(root), new TreeEnvironmentJmeCapability(root)));
    }
}
