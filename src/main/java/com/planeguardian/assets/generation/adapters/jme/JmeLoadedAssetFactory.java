package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.ComposableLoadedAsset;
import com.planeguardian.assets.runtime.LoadedAsset;
import java.util.List;
import java.util.ServiceLoader;

/** PlaneGuardian receiving path for loaded jME asset roots. */
public final class JmeLoadedAssetFactory {
    private static final List<JmeAssetFamilyBinding> BINDINGS = ServiceLoader.load(JmeAssetFamilyBinding.class)
            .stream().map(ServiceLoader.Provider::get).toList();

    private JmeLoadedAssetFactory() {}

    public static LoadedAsset<Node> wrap(Node root) {
        String assetId = root.getUserData("pg.assetId");
        String generatorId = root.getUserData("pg.generatorId");
        StableId stableAssetId = new StableId(assetId == null ? "asset.loaded.preview" : assetId);
        for (JmeAssetFamilyBinding binding : BINDINGS) {
            if (generatorId != null && binding.supports(generatorId)) {
                return binding.wrap(stableAssetId, root);
            }
        }
        return new ComposableLoadedAsset<>(stableAssetId, root, List.of());
    }
}
