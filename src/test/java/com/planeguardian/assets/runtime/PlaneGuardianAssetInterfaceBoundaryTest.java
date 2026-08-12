package com.planeguardian.assets.runtime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Enforces roadmap item 17's dependency-boundary acceptance criterion for the
 * documented {@code PlaneGuardianAssetInterface}: the four game-facing
 * surfaces under {@code com.planeguardian.assets.runtime} and
 * {@code com.planeguardian.assets.generation.api} (package/runtime
 * compatibility, {@code RuntimeAssetProvider} discovery, {@code LoadedAsset},
 * and the shared identity/version/fingerprint types) must never themselves
 * pull in generator, {@code ProtoMesh}, semantic-adapter, or authoring-tooling
 * packages. See {@code docs/architecture/generation-platform.md} §
 * "The PlaneGuardianAssetInterface: the one boundary the game depends on".
 */
class PlaneGuardianAssetInterfaceBoundaryTest {
    private static final List<String> FORBIDDEN_DEPENDENCIES = List.of(
            "com.jme3", "javax.swing", "java.awt",
            "com.planeguardian.assets.db",
            "com.planeguardian.assets.tools",
            "com.planeguardian.assets.export",
            "com.planeguardian.assets.gltf",
            "com.planeguardian.assets.assetgenerator",
            "com.planeguardian.assets.generation.authoring",
            "com.planeguardian.assets.generation.topology",
            "com.planeguardian.assets.generation.curves",
            "com.planeguardian.assets.generation.geometry",
            "com.planeguardian.assets.generation.surface",
            "com.planeguardian.assets.generation.surfaces",
            "com.planeguardian.assets.generation.adapters",
            "com.planeguardian.assets.generation.resources",
            "com.planeguardian.assets.generation.performance",
            "com.planeguardian.assets.generation.triangulation",
            "com.planeguardian.assets.generation.determinism",
            "com.planeguardian.assets.generation.math");

    @Test
    void runtimePackageDependsOnlyOnDocumentedSurfaces() throws IOException {
        Path runtimeRoot = Path.of("src", "main", "java", "com", "planeguardian", "assets", "runtime");
        assertNoForbiddenImports(runtimeRoot);
    }

    @Test
    void generationApiPackageDependsOnlyOnDocumentedSurfaces() throws IOException {
        Path apiRoot = Path.of("src", "main", "java", "com", "planeguardian", "assets", "generation", "api");
        assertNoForbiddenImports(apiRoot);
    }

    private static void assertNoForbiddenImports(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String content = Files.readString(source);
                for (String forbidden : FORBIDDEN_DEPENDENCIES) {
                    assertFalse(content.contains(forbidden),
                            () -> source + " must not depend on " + forbidden
                                    + " (PlaneGuardianAssetInterface boundary)");
                }
            }
        }
    }
}
