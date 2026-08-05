package com.planeguardian.assets.generation.semantics;

import com.planeguardian.assets.generation.api.StableId;
import java.util.List;

/** Version-one Lore Map wheel metadata shared by authoring tools. */
public final class StandardSemanticWheels {
    private StandardSemanticWheels() {}
    public static List<SemanticWheelDefinition> all() {
        return List.of(
                wheel("lore.elemental", "Elemental Affinity", "Resonance", "Radiance", "Fire", "Air", "Aether", "Shadow", "Ice", "Water", "Earth"),
                wheel("lore.ethos", "Ethos", "Fervor", "Life", "Order", "Death", "Chaos"),
                wheel("lore.world-relation", "World Relation", "Conviction", "Transformation", "Creation", "Preservation", "Annihilation"),
                wheel("lore.cosmic-provenance", "Cosmic Provenance", "Devotion", "Divine", "Infernal"),
                wheel("lore.magical-tradition", "Magical Tradition", "Attunement", "Arcane", "Controlled", "Primal", "Instinctive"),
                wheel("lore.manifestation", "Manifestation", "Manifestation", "Embodied", "Rooted", "Incorporeal", "Wandering"));
    }
    private static SemanticWheelDefinition wheel(String id, String name, String salience, String... labels) {
        List<SemanticWheelDefinition.Sector> sectors = java.util.stream.IntStream.range(0, labels.length)
                .mapToObj(index -> new SemanticWheelDefinition.Sector(labels[index], index * StrictMath.PI * 2 / labels.length)).toList();
        return new SemanticWheelDefinition(new StableId(id), name, sectors, salience);
    }
}
