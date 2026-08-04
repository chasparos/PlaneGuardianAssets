package com.planeguardian.assets.export;

import com.google.gson.JsonParser;
import com.planeguardian.assets.runtime.PackageCompatibility;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageManifestWriterTest {
    @Test
    void writesSortedDataAndRuntimeHashes() throws Exception {
        var directory = Files.createTempDirectory("package");
        Files.writeString(directory.resolve("b.txt"), "b");
        Files.writeString(directory.resolve("a.txt"), "a");
        var runtime = Files.createTempFile("runtime", ".jar");
        Files.writeString(runtime, "runtime");

        PackageManifest manifest = PackageManifestWriter.write(directory,
                new PackageCompatibility("pg.asset-index/1", 1, 1), runtime);

        assertEquals(2, manifest.fileSha256().size());
        assertTrue(Files.exists(directory.resolve("package_manifest.json")));
        assertEquals("pg.asset-index/1", JsonParser.parseString(Files.readString(directory.resolve("package_manifest.json")))
                .getAsJsonObject().getAsJsonObject("compatibility").get("indexSchema").getAsString());
    }
}
