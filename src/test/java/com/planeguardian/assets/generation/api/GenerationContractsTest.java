package com.planeguardian.assets.generation.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerationContractsTest {
    @Test
    void requestDefensivelyCopiesAndOrdersParameters() {
        Map<String, String> mutable = new HashMap<>();
        mutable.put("zeta", "last");
        mutable.put("alpha", "first");

        GenerationRequest request = new GenerationRequest(
                new StableId("pg.tree.deciduous/1"),
                new StableId("pg.generator.tree/1"),
                42L,
                versionsAt(1, 0),
                RenderTier.GAMEPLAY,
                new TreeMap<>(mutable));

        mutable.put("middle", "too late");
        assertEquals(List.of("alpha", "zeta"), new ArrayList<>(request.parameters().keySet()));
        assertThrows(UnsupportedOperationException.class,
                () -> request.parameters().put("other", "value"));
    }

    @Test
    void generatedAssetOwnsItsCollectionsAndFingerprintBytes() {
        List<GenerationDiagnostic> diagnostics = new ArrayList<>();
        byte[] digest = new byte[32];
        GeneratedAsset asset = new GeneratedAsset(
                new StableId("pg.tree.deciduous/1"), List.of(), List.of(), List.of(), diagnostics,
                new ReproducibilityFingerprint(digest));

        diagnostics.add(new GenerationDiagnostic(
                GenerationDiagnostic.Severity.INFO, "late", "must not leak"));
        digest[0] = 99;

        assertEquals(List.of(), asset.diagnostics());
        assertEquals(0, asset.fingerprint().bytes()[0]);
        assertThrows(UnsupportedOperationException.class,
                () -> asset.diagnostics().add(new GenerationDiagnostic(
                        GenerationDiagnostic.Severity.INFO, "x", "x")));
    }

    @Test
    void idsAndTransformsRejectNonCanonicalValues() {
        assertEquals("pg.tree.deciduous/1", new StableId("pg.tree.deciduous/1").value());
        assertThrows(IllegalArgumentException.class, () -> new StableId("PG Tree"));
        assertThrows(IllegalArgumentException.class, () -> new Rotation(0, 0, 0, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new Transform(Vector3.ZERO, Rotation.IDENTITY, new Vector3(1, -1, 1)));
    }

    private static GenerationVersions versionsAt(int major, int minor) {
        ContractVersion version = new ContractVersion(major, minor);
        return new GenerationVersions(version, version, version, version, version, version);
    }
}
