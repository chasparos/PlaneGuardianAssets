package com.planeguardian.assets;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.assetgenerator.tree.GreatTreeAssetGenerator;
import com.planeguardian.assets.export.PackageManifest;
import com.planeguardian.assets.export.PackageManifestWriter;
import com.planeguardian.assets.generation.adapters.jme.JmeLoadedAssetFactory;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.runtime.EnvironmentState;
import com.planeguardian.assets.runtime.LoadedAsset;
import com.planeguardian.assets.runtime.PackageCompatibility;
import com.planeguardian.assets.runtime.RuntimePackageResolver;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.tools.AssetPersistenceLoader;
import com.planeguardian.assets.tools.generator.AuthoringGenerationRequest;
import com.planeguardian.assets.tools.generator.GenerationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the minimum viable runtime contract end to end, from the game's
 * perspective: generate a registered asset, perform the full package export
 * (a hash-bound manifest verifying the binary package file), resolve it
 * through the {@code RuntimePackageResolver} exactly as the game would from
 * an {@code asset_index.json}-declared generator ID, then load and check the
 * fidelity of the resulting {@link LoadedAsset} instance.
 */
class PlaneGuardianAssetRuntimeContractEndToEndTest {
    @TempDir Path packageDir;

    @Test
    void generatesExportsResolvesAndLoadsAssetThroughTheDocumentedRuntimeContract() throws Exception {
        // 1. Generation time: a registered authoring provider produces the binary package file.
        GreatTreeAssetGenerator generator = new GreatTreeAssetGenerator();
        var descriptor = generator.descriptor();
        TreeMap<String, String> values = new TreeMap<>(descriptor.presets().get(0).parameterValues());
        Path assetsDir = packageDir.resolve("assets");
        AuthoringGenerationRequest request = new AuthoringGenerationRequest("GreatOakContractE2E", 99, values,
                new SemanticProfile(new ContractVersion(1, 0), new TreeMap<>()), Set.of());
        GenerationResult result = generator.generate(request, assetsDir);
        assertTrue(result.success(), result.message());
        assertEquals(GreatTreeAssetGenerator.GENERATOR_ID, result.generatorId());

        // 2. Export/package time: the game only ever receives an asset_index.json-declared
        // generator ID plus a hash-bound package it must verify before trusting the runtime jar.
        PackageCompatibility compatibility = new PackageCompatibility("pg.asset-index/1", 1, 1);
        Path runtimeArtifact = Files.createTempFile("planeguardian-runtime", ".jar");
        Files.writeString(runtimeArtifact, "runtime-jar-fixture-bytes");
        PackageManifest manifest = PackageManifestWriter.write(packageDir, compatibility, runtimeArtifact);
        String relativePath = packageDir.relativize(result.outputPath()).toString().replace('\\', '/');
        assertTrue(manifest.fileSha256().containsKey(relativePath));

        // Verify binary package file: the game recomputes and compares the hash before loading it.
        assertEquals(manifest.fileSha256().get(relativePath), PackageManifestWriter.sha256(result.outputPath()));
        Files.write(result.outputPath(), "corruption".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                StandardOpenOption.APPEND);
        assertNotEquals(manifest.fileSha256().get(relativePath), PackageManifestWriter.sha256(result.outputPath()));
        // Regenerate a clean, verifiable package file for the subsequent load step.
        result = generator.generate(request, assetsDir);
        assertEquals(manifest.fileSha256().get(relativePath), PackageManifestWriter.sha256(result.outputPath()));

        // 3. Load time: resolve the asset_index.json-declared generator ID to a trusted
        // RuntimeAssetProvider (discovered only through ServiceLoader) or a safe fallback.
        RuntimePackageResolver resolver = new RuntimePackageResolver(compatibility);
        RuntimePackageResolver.Resolution resolution =
                resolver.resolve(result.generatorId(), compatibility, relativePath);
        assertFalse(resolution.usesFallback());
        assertEquals("pg.tree.deciduous", resolution.provider().providerId());

        // 4. Load the verified binary package file and wrap it through the generic
        // receiving-end LoadedAsset facade; the game never touches generator-private types.
        Spatial loaded = AssetPersistenceLoader.load(new DesktopAssetManager(true), result.outputPath());
        loaded.updateGeometricState();
        LoadedAsset<Node> instance = JmeLoadedAssetFactory.wrap((Node) loaded);

        // Fidelity checks on the loaded instance.
        List<Geometry> geometries = new ArrayList<>();
        collect(loaded, geometries);
        assertFalse(geometries.isEmpty());
        assertTrue(geometries.stream().allMatch(geometry -> geometry.getMesh().getTriangleCount() > 0));
        assertTrue(geometries.stream().allMatch(geometry -> geometry.getMaterial() != null));
        BoundingVolume bounds = loaded.getWorldBound();
        assertNotNull(bounds);
        assertTrue(bounds instanceof BoundingBox box && box.getXExtent() > 0 && box.getYExtent() > 0 && box.getZExtent() > 0);
        Node sockets = (Node) ((Node) loaded).getChild("asset.sockets");
        assertNotNull(sockets);
        assertTrue(sockets.getQuantity() > 0);
        assertTrue(instance.capabilities().stream().anyMatch(capability ->
                capability.capabilityId().value().equals("runtime.tree.semantics")));
        assertTrue(instance.capabilities().stream().anyMatch(capability ->
                capability.capabilityId().value().equals("runtime.tree.environment")));

        // Minimum viable game contract: initialize instance semantics once, then drive per-frame updates.
        ResolvedVisualProfile profile = new ResolvedVisualProfile(new ContractVersion(1, 0),
                new TreeMap<>(java.util.Map.of("tree.crown.coverage", .8)), List.of(),
                new ReproducibilityFingerprint(new byte[32]));
        LoadedAsset.SemanticApplicationResult initialized = instance.initializeSemantics(profile);
        assertTrue(initialized.changed());
        assertFalse(instance.initializeSemantics(profile).changed());
        instance.update(.016, new EnvironmentState(1, new Vector3(1, 0, 0), .5, new TreeMap<>()));
    }

    private static void collect(Spatial spatial, List<Geometry> geometries) {
        if (spatial instanceof Geometry geometry) geometries.add(geometry);
        if (spatial instanceof Node node) node.getChildren().forEach(child -> collect(child, geometries));
    }
}
