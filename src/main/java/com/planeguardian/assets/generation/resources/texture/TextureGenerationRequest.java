package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.StableId;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Canonical, storage-neutral input used to derive texture cache identity. */
public record TextureGenerationRequest(
        StableId textureId,
        int width,
        int height,
        long seed,
        Map<String, String> parameters,
        List<TextureSource> sources) {
    public TextureGenerationRequest {
        Objects.requireNonNull(textureId, "textureId");
        if (width < 1 || height < 1) throw new IllegalArgumentException("Texture dimensions must be positive");
        TreeMap<String, String> ordered = new TreeMap<>();
        parameters.forEach((name, value) -> {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Parameter name must not be blank");
            if (value == null) throw new IllegalArgumentException("Parameter value must not be null");
            ordered.put(name, value);
        });
        parameters = Collections.unmodifiableMap(ordered);
        sources = sources.stream().sorted(Comparator
                .comparing((TextureSource source) -> source.resource().resourceId())
                .thenComparing(source -> source.resource().kind())
                .thenComparing(source -> source.resource().version().major())
                .thenComparing(source -> source.resource().version().minor())
                .thenComparing(source -> source.fingerprint().hex())).toList();
    }
}
