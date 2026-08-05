package com.planeguardian.assets.generation.palette;

import com.planeguardian.assets.generation.api.StableId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Shared deterministic named-color palette used across asset generators. */
public final class NamedColorPalette {
    private static final NamedColorPalette STANDARD = new NamedColorPalette(List.of(
            new PaletteEntry(new StableId("palette.gem.ruby-core"), "Ruby Core", new LinearColor(0.78f, 0.08f, 0.16f, 1f), "Warm red crystal core for fire-aligned or noble gems."),
            new PaletteEntry(new StableId("palette.gem.emerald-core"), "Emerald Core", new LinearColor(0.05f, 0.62f, 0.28f, 1f), "Saturated living green crystal core."),
            new PaletteEntry(new StableId("palette.gem.sapphire-core"), "Sapphire Core", new LinearColor(0.12f, 0.32f, 0.82f, 1f), "Deep blue crystalline body for water or arcane cool tones."),
            new PaletteEntry(new StableId("palette.gem.amethyst-core"), "Amethyst Core", new LinearColor(0.56f, 0.24f, 0.78f, 1f), "Arcane violet body color for mystical crystals."),
            new PaletteEntry(new StableId("palette.gem.citrine-core"), "Citrine Core", new LinearColor(0.90f, 0.60f, 0.18f, 1f), "Amber-gold body color for radiant or rare crystals."),
            new PaletteEntry(new StableId("palette.gem.quartz-clear"), "Quartz Clear", new LinearColor(0.82f, 0.88f, 0.96f, 0.65f), "Neutral clear body color for plain or flawless quartz-like crystals."),
            new PaletteEntry(new StableId("palette.gem.obsidian-shadow"), "Obsidian Shadow", new LinearColor(0.14f, 0.10f, 0.18f, 0.92f), "Dark neutral crystalline accent for shadowed inclusions and flawed hosts."),
            new PaletteEntry(new StableId("palette.emissive.arcane-glow"), "Arcane Glow", new LinearColor(0.60f, 0.38f, 0.95f, 1f), "High-energy violet emissive accent."),
            new PaletteEntry(new StableId("palette.emissive.radiant-glow"), "Radiant Glow", new LinearColor(0.96f, 0.82f, 0.38f, 1f), "Warm radiant emissive accent for powerful or legendary crystals.")));

    private final List<PaletteEntry> entries;
    private final Map<StableId, PaletteEntry> byId;

    public NamedColorPalette(List<PaletteEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        LinkedHashMap<StableId, PaletteEntry> ordered = new LinkedHashMap<>();
        for (PaletteEntry entry : entries) {
            PaletteEntry previous = ordered.putIfAbsent(entry.id(), Objects.requireNonNull(entry, "entry"));
            if (previous != null) throw new IllegalArgumentException("Duplicate palette entry ID: " + entry.id());
        }
        this.entries = List.copyOf(ordered.values());
        this.byId = Map.copyOf(ordered);
    }

    public static NamedColorPalette standard() { return STANDARD; }
    public List<PaletteEntry> entries() { return entries; }
    public Optional<PaletteEntry> find(StableId id) { return Optional.ofNullable(byId.get(id)); }
    public PaletteEntry require(StableId id) { return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown palette entry: " + id)); }
}
