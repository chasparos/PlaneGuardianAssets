package com.planeguardian.assets.assetgenerator.tree;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.tools.AssetPersistenceLoader;
import com.planeguardian.assets.tools.generator.AuthoringGenerationRequest;
import com.planeguardian.assets.tools.generator.GenerationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreatTreeAuthoringEndToEndTest {
    @TempDir Path output;

    @Test
    void defaultPresetGeneratesAndReloadsThroughViewerPersistencePath() throws Exception {
        GreatTreeAssetGenerator provider = new GreatTreeAssetGenerator();
        var descriptor = provider.descriptor();
        TreeMap<String, String> values = new TreeMap<>(descriptor.presets().get(0).parameterValues());
        AuthoringGenerationRequest request = new AuthoringGenerationRequest("GreatOakSmoke", 42, values,
                new SemanticProfile(new ContractVersion(1, 0), new TreeMap<>()), java.util.Set.of());

        GenerationResult result = provider.generate(request, output);
        assertTrue(result.success(), result.message());
        assertTrue(java.nio.file.Files.size(result.outputPath()) > 0);

        Spatial loaded = AssetPersistenceLoader.load(new DesktopAssetManager(true), result.outputPath());
        loaded.updateGeometricState();
        List<Geometry> geometries = new ArrayList<>(); collect(loaded, geometries);
        assertFalse(geometries.isEmpty());
        assertTrue(geometries.stream().allMatch(geometry -> geometry.getMesh().getTriangleCount() > 0));
        assertTrue(geometries.stream().allMatch(geometry -> geometry.getMaterial() != null));
        BoundingVolume bounds = loaded.getWorldBound();
        assertNotNull(bounds);
        assertTrue(Float.isFinite(bounds.getCenter().x) && Float.isFinite(bounds.getCenter().y) && Float.isFinite(bounds.getCenter().z));
        assertTrue(bounds instanceof BoundingBox box && box.getXExtent() > 0 && box.getYExtent() > 0 && box.getZExtent() > 0);
        Node sockets = (Node) ((Node) loaded).getChild("asset.sockets");
        assertNotNull(sockets);
        assertTrue(sockets.getQuantity() > 0);
        assertTrue(sockets.getChildren().stream().allMatch(socket -> socket.getUserData("pg.socketId") != null));
        var runtime = com.planeguardian.assets.generation.adapters.jme.JmeLoadedAssetFactory.wrap((Node) loaded);
        assertTrue(runtime.capabilities().stream().anyMatch(capability ->
                capability.capabilityId().value().equals("runtime.tree.semantics")));
        assertTrue(runtime.capabilities().stream().anyMatch(capability ->
                capability.capabilityId().value().equals("runtime.tree.environment")));
    }

    private static void collect(Spatial spatial, List<Geometry> geometries) {
        if (spatial instanceof Geometry geometry) geometries.add(geometry);
        if (spatial instanceof Node node) node.getChildren().forEach(child -> collect(child, geometries));
    }
}
