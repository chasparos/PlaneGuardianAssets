package com.planeguardian.assets.generation.semantics;

import com.planeguardian.assets.generation.api.StableId;
import java.util.List;
import java.util.Objects;

/** Display metadata for a semantic wheel; meaning remains in the resolver. */
public record SemanticWheelDefinition(StableId id, String displayName, List<Sector> sectors,
                                      String salienceLabel) {
    public SemanticWheelDefinition {
        Objects.requireNonNull(id); requireText(displayName, "displayName");
        sectors = List.copyOf(sectors); if (sectors.size() < 2) throw new IllegalArgumentException("A wheel needs at least two sectors");
        requireText(salienceLabel, "salienceLabel");
    }
    private static void requireText(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank"); }
    public record Sector(String label, double angleRadians) {
        public Sector { requireText(label, "label"); if (!Double.isFinite(angleRadians)) throw new IllegalArgumentException("angle must be finite"); }
    }
}
