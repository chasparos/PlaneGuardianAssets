package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.LoadedAsset;

/**
 * Asset-family-specific jME loaded-asset binding, discovered only through
 * {@code ServiceLoader}. Keeps {@link JmeLoadedAssetFactory} generic: it never
 * hard-codes a family's generator ID or imports a family's {@code export}
 * package directly.
 */
public interface JmeAssetFamilyBinding {
    boolean supports(String generatorId);

    LoadedAsset<Node> wrap(StableId assetId, Node root);
}
