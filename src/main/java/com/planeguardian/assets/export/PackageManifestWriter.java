package com.planeguardian.assets.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.planeguardian.assets.runtime.PackageCompatibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/** Writes a deterministic hash manifest after all package data files have been finalized. */
public final class PackageManifestWriter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private PackageManifestWriter() {
    }

    public static PackageManifest write(Path packageDirectory, PackageCompatibility compatibility,
                                        Path runtimeArtifact) throws IOException {
        if (!Files.isRegularFile(runtimeArtifact)) {
            throw new IOException("Paired runtime artifact does not exist: " + runtimeArtifact);
        }
        LinkedHashMap<String, String> hashes;
        try (var paths = Files.walk(packageDirectory)) {
            hashes = paths.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals("package_manifest.json"))
                    .sorted()
                    .collect(Collectors.toMap(
                            path -> packageDirectory.relativize(path).toString().replace('\\', '/'),
                            PackageManifestWriter::sha256,
                            (left, right) -> left,
                            LinkedHashMap::new));
        }
        PackageManifest manifest = new PackageManifest(compatibility, hashes, sha256(runtimeArtifact));
        Files.writeString(packageDirectory.resolve("package_manifest.json"), GSON.toJson(manifest));
        return manifest;
    }

    public static String sha256(Path file) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
            StringBuilder output = new StringBuilder(64);
            for (byte value : digest) output.append(String.format("%02x", value));
            return output.toString();
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new IllegalStateException("Unable to hash " + file, exception);
        }
    }
}
