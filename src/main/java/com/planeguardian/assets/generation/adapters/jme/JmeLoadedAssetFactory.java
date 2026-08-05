package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.ComposableLoadedAsset;
import com.planeguardian.assets.runtime.LoadedAsset;
import java.util.List;

/** PlaneGuardian receiving path for loaded jME asset roots. */
public final class JmeLoadedAssetFactory {
    private JmeLoadedAssetFactory() {}
    public static LoadedAsset<Node> wrap(Node root) {
        String assetId = root.getUserData("pg.assetId");
        String generatorId = root.getUserData("pg.generatorId");
        StableId stableAssetId = new StableId(assetId == null ? "asset.loaded.preview" : assetId);
        if ("pg.tree.deciduous/1".equals(generatorId)) return TreeLoadedAssetFactory.wrap(stableAssetId, root);
        return new ComposableLoadedAsset<>(stableAssetId, root, List.of());
    }
}
