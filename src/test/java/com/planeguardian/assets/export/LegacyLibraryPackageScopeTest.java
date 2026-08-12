package com.planeguardian.assets.export;

import com.planeguardian.assets.assetgenerator.tree.runtime.DeciduousTreeRuntimeProvider;
import com.planeguardian.assets.runtime.RuntimePackageResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the scoping decision recorded in
 * {@code docs/architecture/generation-platform.md} ("Known drift against this
 * boundary") and roadmap item 16: the legacy JDBC-backed library exporter is
 * explicitly out of scope of {@code PlaneGuardianAssetInterface}. A library
 * asset (no {@code generatorId}, always a valid package-local fallback GLB)
 * must always resolve to the fallback path even when a trusted
 * {@link com.planeguardian.assets.runtime.RuntimeAssetProvider} is registered,
 * so the game domain never needs library-specific knowledge to consume
 * legacy-exported entries correctly.
 */
class LegacyLibraryPackageScopeTest {
    @Test
    void libraryAssetWithoutGeneratorIdAlwaysFallsBackEvenWithTrustedProviderRegistered() {
        RuntimePackageResolver resolver = new RuntimePackageResolver(
                ExportManager.PACKAGE_COMPATIBILITY, List.of(new DeciduousTreeRuntimeProvider()));

        // A legacy library asset has no generatorId (blank metadata), matching
        // ExportManager's AssetIndexEntry#generatorId contract.
        RuntimePackageResolver.Resolution resolution =
                resolver.resolve("", ExportManager.PACKAGE_COMPATIBILITY, "assets/Jet_1.glb");

        assertTrue(resolution.usesFallback());
    }
}
