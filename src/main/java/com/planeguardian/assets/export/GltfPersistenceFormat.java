package com.planeguardian.assets.export;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Repository-owned persistence boundary for a completed glTF/GLB asset.
 * It proves the export data can become a jME Scenegraph without authoring-format coupling.
 */
public final class GltfPersistenceFormat {
    private GltfPersistenceFormat() {
    }

    public static Node loadAsset(AssetManager assetManager, Path gltfAsset) throws IOException {
        Objects.requireNonNull(assetManager, "assetManager");
        Objects.requireNonNull(gltfAsset, "gltfAsset");
        Path absolute = gltfAsset.toAbsolutePath().normalize();
        if (!Files.isRegularFile(absolute) || !isGltf(absolute)) {
            throw new IOException("Asset must be an existing .gltf or .glb file: " + gltfAsset);
        }
        assetManager.registerLocator(absolute.getParent().toString(), FileLocator.class);
        Spatial loaded = assetManager.loadModel(absolute.getFileName().toString());
        if (loaded instanceof Node node) return node;
        Node root = new Node("gltf-asset");
        root.attachChild(loaded);
        return root;
    }

    private static boolean isGltf(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".gltf") || name.endsWith(".glb");
    }
}
