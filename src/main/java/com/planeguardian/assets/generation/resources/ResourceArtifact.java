package com.planeguardian.assets.generation.resources;

import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Objects;

/** Metadata for one encoded resource artifact; bytes and persistence live elsewhere. */
public record ResourceArtifact(
        StableId artifactId,
        GeneratedResourceRef resource,
        String mediaType,
        String relativePath,
        long byteLength,
        ReproducibilityFingerprint contentFingerprint) {
    public ResourceArtifact {
        Objects.requireNonNull(artifactId, "artifactId");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(mediaType, "mediaType");
        Objects.requireNonNull(relativePath, "relativePath");
        Objects.requireNonNull(contentFingerprint, "contentFingerprint");
        if (mediaType.isBlank() || !mediaType.contains("/")) throw new IllegalArgumentException("Artifact media type must be valid");
        if (byteLength < 0) throw new IllegalArgumentException("Artifact byte length must be non-negative");
        java.util.List<String> pathSegments = java.util.Arrays.asList(relativePath.split("/", -1));
        if (relativePath.isBlank() || relativePath.startsWith("/") || relativePath.startsWith("\\")
                || relativePath.contains("\\") || relativePath.matches("^[A-Za-z]:.*")
                || pathSegments.stream().anyMatch(segment -> segment.isBlank() || segment.equals(".") || segment.equals(".."))) {
            throw new IllegalArgumentException("Artifact path must be a safe relative forward-slash path");
        }
    }
}
