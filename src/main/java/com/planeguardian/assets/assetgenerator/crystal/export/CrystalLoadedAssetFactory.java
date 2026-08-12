package com.planeguardian.assets.assetgenerator.crystal.export;

import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.ComposableLoadedAsset;
import com.planeguardian.assets.runtime.LoadedAsset;
import java.util.List;

/** Installs crystal semantic runtime behavior on a loaded generated asset. */
public final class CrystalLoadedAssetFactory {
    private CrystalLoadedAssetFactory() { }

    public static LoadedAsset<Node> wrap(StableId assetId, Node root) {
        return new ComposableLoadedAsset<>(assetId, root, List.of(new CrystalSemanticJmeCapability(root)));
    }
}
