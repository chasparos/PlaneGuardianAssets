package com.planeguardian.assets.generation.resources.cache;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.ResourceArtifact;
import com.planeguardian.assets.generation.resources.texture.EncodedTextureArtifact;
import com.planeguardian.assets.generation.resources.texture.PngTextureEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/** Content-verified local cache which publishes metadata only after artifact bytes are atomically in place. */
public final class LocalGeneratedResourceCache implements GeneratedResourceCache {
    private static final String METADATA_SUFFIX = ".properties";
    private static final String DATA_SUFFIX = ".bin";
    private final Path root;

    public LocalGeneratedResourceCache(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public Optional<CachedResourceArtifact> find(ReproducibilityFingerprint generationFingerprint) throws IOException {
        Path metadata = metadataPath(generationFingerprint);
        Path data = dataPath(generationFingerprint);
        if (!Files.isRegularFile(metadata) || !Files.isRegularFile(data)) {
            return Optional.empty();
        }
        try {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(metadata)) {
                properties.load(input);
            }
            if (!generationFingerprint.hex().equals(properties.getProperty("generationFingerprint"))) {
                return corrupt(metadata, data);
            }
            byte[] bytes = Files.readAllBytes(data);
            ResourceArtifact artifact = artifact(properties);
            if (artifact.byteLength() != bytes.length
                    || !PngTextureEncoder.contentFingerprint(bytes).equals(artifact.contentFingerprint())) {
                return corrupt(metadata, data);
            }
            return Optional.of(new CachedResourceArtifact(generationFingerprint,
                    new EncodedTextureArtifact(artifact, bytes)));
        } catch (IllegalArgumentException exception) {
            return corrupt(metadata, data);
        }
    }

    @Override
    public CachedResourceArtifact store(ReproducibilityFingerprint generationFingerprint,
                                        EncodedTextureArtifact encodedArtifact) throws IOException {
        Optional<CachedResourceArtifact> existing = find(generationFingerprint);
        if (existing.isPresent()) {
            return existing.get();
        }
        Files.createDirectories(root);
        Path data = dataPath(generationFingerprint);
        Path metadata = metadataPath(generationFingerprint);
        Path temporaryData = temporaryPath(data);
        Path temporaryMetadata = temporaryPath(metadata);
        try {
            Files.write(temporaryData, encodedArtifact.bytes());
            moveAtomically(temporaryData, data);
            Properties properties = properties(generationFingerprint, encodedArtifact.artifact());
            try (OutputStream output = Files.newOutputStream(temporaryMetadata)) {
                properties.store(output, null);
            }
            moveAtomically(temporaryMetadata, metadata);
        } catch (java.nio.file.FileAlreadyExistsException exception) {
            Files.deleteIfExists(temporaryData);
            Files.deleteIfExists(temporaryMetadata);
        } finally {
            Files.deleteIfExists(temporaryData);
            Files.deleteIfExists(temporaryMetadata);
        }
        return find(generationFingerprint).orElseThrow(
                () -> new IOException("Generated resource cache entry was not safely published"));
    }

    private Optional<CachedResourceArtifact> corrupt(Path metadata, Path data) throws IOException {
        Files.deleteIfExists(metadata);
        Files.deleteIfExists(data);
        return Optional.empty();
    }

    private Path dataPath(ReproducibilityFingerprint fingerprint) {
        return root.resolve(fingerprint.hex() + DATA_SUFFIX);
    }

    private Path metadataPath(ReproducibilityFingerprint fingerprint) {
        return root.resolve(fingerprint.hex() + METADATA_SUFFIX);
    }

    private static Path temporaryPath(Path target) {
        return target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".tmp");
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target);
        }
    }

    private static Properties properties(ReproducibilityFingerprint fingerprint, ResourceArtifact artifact) {
        Properties properties = new Properties();
        properties.setProperty("generationFingerprint", fingerprint.hex());
        properties.setProperty("artifactId", encoded(artifact.artifactId().value()));
        properties.setProperty("resourceId", encoded(artifact.resource().resourceId().value()));
        properties.setProperty("resourceKind", artifact.resource().kind().name());
        properties.setProperty("resourceVersion", artifact.resource().version().toString());
        properties.setProperty("mediaType", encoded(artifact.mediaType()));
        properties.setProperty("relativePath", encoded(artifact.relativePath()));
        properties.setProperty("byteLength", Long.toString(artifact.byteLength()));
        properties.setProperty("contentFingerprint", artifact.contentFingerprint().hex());
        return properties;
    }

    private static ResourceArtifact artifact(Properties properties) {
        String[] version = required(properties, "resourceVersion").split("\\.", -1);
        if (version.length != 2) throw new IllegalArgumentException("Invalid resource version");
        return new ResourceArtifact(new StableId(decoded(required(properties, "artifactId"))),
                new GeneratedResourceRef(new StableId(decoded(required(properties, "resourceId"))),
                        ResourceKind.valueOf(required(properties, "resourceKind")),
                        new ContractVersion(Integer.parseInt(version[0]), Integer.parseInt(version[1]))),
                decoded(required(properties, "mediaType")), decoded(required(properties, "relativePath")),
                Long.parseLong(required(properties, "byteLength")),
                new ReproducibilityFingerprint(java.util.HexFormat.of()
                        .parseHex(required(properties, "contentFingerprint"))));
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Missing cache metadata: " + key);
        return value;
    }

    private static String encoded(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String decoded(String value) {
        return new String(Base64.getUrlDecoder().decode(value), java.nio.charset.StandardCharsets.UTF_8);
    }
}
