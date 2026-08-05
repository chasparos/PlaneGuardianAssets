package com.planeguardian.assets.generation.palette;

import com.planeguardian.assets.generation.api.StableId;
import java.util.Objects;

/** Curated shared named color with a stable semantic meaning. */
public record PaletteEntry(StableId id, String displayName, LinearColor color, String semanticIntent) {
    public PaletteEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(color, "color");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName must not be blank");
        if (semanticIntent == null || semanticIntent.isBlank()) throw new IllegalArgumentException("semanticIntent must not be blank");
    }
}
