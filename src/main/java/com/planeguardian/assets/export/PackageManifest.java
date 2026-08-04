package com.planeguardian.assets.export;

import com.planeguardian.assets.runtime.PackageCompatibility;

import java.util.Map;

/** Hash-bound description of the exported data package and its paired runtime contract. */
public record PackageManifest(PackageCompatibility compatibility, Map<String, String> fileSha256,
                              String runtimeArtifactSha256) {
    public PackageManifest {
        fileSha256 = Map.copyOf(fileSha256);
        if (runtimeArtifactSha256 == null || !runtimeArtifactSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("runtimeArtifactSha256 must be SHA-256");
        }
    }
}
