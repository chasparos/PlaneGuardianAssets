package com.planeguardian.assets.generation.palette;

import com.planeguardian.assets.generation.api.StableId;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class NamedColorPaletteTest {
    @Test
    void standardPaletteUsesUniqueStableIdsAndImmutableEntries() {
        NamedColorPalette palette = NamedColorPalette.standard();
        assertEquals(palette.entries().size(), palette.entries().stream().map(PaletteEntry::id).distinct().count());
        assertThrows(UnsupportedOperationException.class, () -> palette.entries().add(palette.entries().get(0)));
    }

    @Test
    void lookupFindsKnownEntriesAndRejectsUnknownIds() {
        NamedColorPalette palette = NamedColorPalette.standard();
        assertEquals("Ruby Core", palette.require(new StableId("palette.gem.ruby-core")).displayName());
        assertTrue(palette.find(new StableId("palette.gem.unknown")).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> palette.require(new StableId("palette.gem.unknown")));
    }

    @Test
    void constructorRejectsDuplicateIds() {
        PaletteEntry entry = new PaletteEntry(new StableId("palette.test"), "Test", new LinearColor(1, 1, 1, 1), "Test intent");
        assertThrows(IllegalArgumentException.class, () -> new NamedColorPalette(List.of(entry, entry)));
    }
}
