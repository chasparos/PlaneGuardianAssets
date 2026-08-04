package com.planeguardian.assets.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimePackageResolverTest {
    private static final PackageCompatibility COMPATIBILITY = new PackageCompatibility("pg.asset-index/1", 1, 1);

    @Test
    void resolvesTrustedProviderAndFallsBackWhenAbsent() {
        RuntimePackageResolver resolver = new RuntimePackageResolver(COMPATIBILITY,
                List.of(new DeciduousTreeRuntimeProvider()));

        assertEquals("pg.tree.deciduous", resolver.resolve("pg.tree.deciduous/1", COMPATIBILITY, "assets/tree.glb")
                .provider().providerId());
        RuntimePackageResolver.Resolution fallback = resolver.resolve("unknown/1", COMPATIBILITY, "assets/tree.glb");
        assertTrue(fallback.usesFallback());
        assertEquals("assets/tree.glb", fallback.fallbackGltf());
    }

    @Test
    void discoversTheRegisteredGreatTreeProviderFromTheClasspath() {
        RuntimePackageResolver resolver = new RuntimePackageResolver(COMPATIBILITY);

        assertEquals("pg.tree.deciduous", resolver.resolve("pg.tree.deciduous/1", COMPATIBILITY,
                "assets/tree.glb").provider().providerId());
    }

    @Test
    void rejectsIncompatibleContractsAndUnsafeFallbacks() {
        RuntimePackageResolver resolver = new RuntimePackageResolver(COMPATIBILITY, List.of());
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve("unknown/1", new PackageCompatibility("pg.asset-index/2", 1, 1), "assets/tree.glb"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve("unknown/1", COMPATIBILITY, "../tree.glb"));
    }

    @Test
    void cacheKeyChangesWithProviderCompatibility() {
        String fingerprint = "a".repeat(64);
        assertTrue(!PackageCacheKey.forAsset(COMPATIBILITY, "pg.tree.deciduous/1", fingerprint)
                .equals(PackageCacheKey.forAsset(new PackageCompatibility("pg.asset-index/1", 1, 2),
                        "pg.tree.deciduous/1", fingerprint)));
    }
}
