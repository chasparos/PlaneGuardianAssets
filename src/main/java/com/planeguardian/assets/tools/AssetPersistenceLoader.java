package com.planeguardian.assets.tools;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.ModelKey;
import com.jme3.export.Savable;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.export.GltfPersistenceFormat;
import java.io.IOException;
import java.nio.file.Path;

/** Shared viewer-equivalent persistence path used by preview and integration tests. */
public final class AssetPersistenceLoader {
    private AssetPersistenceLoader() {}
    public static Spatial load(AssetManager assets, Path path) throws IOException {
        Path absolute = path.toAbsolutePath();
        if (absolute.getParent() != null) assets.registerLocator(absolute.getParent().toString(), FileLocator.class);
        String name = absolute.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith(".gltf") || name.endsWith(".glb")) return GltfPersistenceFormat.loadAsset(assets, absolute);
        if (name.endsWith(".j3o")) {
            BinaryImporter importer = BinaryImporter.getInstance();
            synchronized (importer) {
                importer.setAssetManager(assets);
                Savable loaded = importer.load(absolute.toFile());
                if (loaded instanceof Spatial spatial) return spatial;
                throw new IOException("J3O did not contain a scene Spatial: " + absolute);
            }
        }
        ModelKey key = new ModelKey(absolute.getFileName().toString());
        assets.deleteFromCache(key);
        return assets.loadModel(key);
    }
}
