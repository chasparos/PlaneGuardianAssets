package com.planeguardian.assets.runtime;

import com.jme3.asset.AssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.scene.Spatial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Compatibility bridge for PlaneGuardian's persistence/load boundary.
 * The game integration replaces this adapter with its PersistenceFormat implementation.
 */
public final class PlaneGuardianPersistenceBridge {
    private PlaneGuardianPersistenceBridge() {
    }

    public static Path persist(Spatial completedAsset, Path destination) throws IOException {
        Objects.requireNonNull(completedAsset, "completedAsset");
        Objects.requireNonNull(destination, "destination");
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        BinaryExporter.getInstance().save(completedAsset, destination.toFile());
        return destination;
    }

    public static Spatial loadAsset(AssetManager assetManager, Path persistedAsset) throws IOException {
        Objects.requireNonNull(assetManager, "assetManager");
        Objects.requireNonNull(persistedAsset, "persistedAsset");
        BinaryImporter importer = BinaryImporter.getInstance();
        importer.setAssetManager(assetManager);
        return (Spatial) importer.load(persistedAsset.toFile());
    }
}
