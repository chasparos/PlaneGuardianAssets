package com.planeguardian.assets.generation.crystal;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.jupiter.api.Assertions.*;

class CrystalSemanticAdapterTest {
    private final CrystalSemanticAdapter adapter = new CrystalSemanticAdapter();

    @Test
    void commonLowPowerFlawedCrystalStaysSmallDimAndSimple() {
        var resolved = adapter.resolve(profile(Map.of(
                "game.power", x(-1, 1),
                "game.rarity", x(-1, 1),
                "game.quality", x(-1, 1))), com.planeguardian.assets.generation.semantics.AssetSemanticAdapter.ResolutionContext.intrinsicOnly());
        assertTrue(resolved.channels().get("crystal.base-radius") < 0.4);
        assertTrue(resolved.channels().get("crystal.emission-strength") < 0.25);
        assertEquals(1d, resolved.channels().get("crystal.cluster-members"));
    }

    @Test
    void legendaryOverwhelmingFlawlessCrystalGrowsBrightLargeAndComplex() {
        var resolved = adapter.resolve(profile(Map.of(
                "game.power", x(1, 1),
                "game.rarity", x(1, 1),
                "game.quality", x(1, 1),
                "lore.magical-tradition", x(1, 1))), com.planeguardian.assets.generation.semantics.AssetSemanticAdapter.ResolutionContext.intrinsicOnly());
        assertTrue(resolved.channels().get("crystal.base-radius") > 0.6);
        assertTrue(resolved.channels().get("crystal.emission-strength") > 0.75);
        assertTrue(resolved.channels().get("crystal.facet-count") >= 10);
        assertTrue(resolved.channels().get("crystal.cluster-members") >= 3);
    }

    @Test
    void opposedElementsSelectDifferentPaletteCharacter() {
        var fire = adapter.profile(profile(Map.of("lore.elemental", y(1, 1))));
        var ice = adapter.profile(profile(Map.of("lore.elemental", y(-1, 1))));
        assertNotEquals(fire.colorCharacter().paletteEntry().id(), ice.colorCharacter().paletteEntry().id());
    }

    @Test
    void manifestationBiasesNaturalRockVersusLevitation() {
        var rooted = adapter.profile(profile(Map.of("lore.manifestation", y(1, 1))));
        var wandering = adapter.profile(profile(Map.of("lore.manifestation", y(-1, 1))));
        assertEquals(CrystalParameters.SettingKind.NATURAL_ROCK, rooted.settingCharacter().preferredSetting());
        assertEquals(CrystalParameters.SettingKind.LEVITATION, wandering.settingCharacter().preferredSetting());
    }

    private static SemanticProfile profile(Map<String, SemanticWheelValue> values) {
        TreeMap<StableId, SemanticWheelValue> wheels = new TreeMap<>();
        values.forEach((id, value) -> wheels.put(new StableId(id), value));
        return new SemanticProfile(new ContractVersion(1, 0), wheels);
    }
    private static SemanticWheelValue x(double x, double s) { return new SemanticWheelValue(x, 0, s, java.util.Optional.empty(), java.util.Optional.empty(), java.util.List.of()); }
    private static SemanticWheelValue y(double y, double s) { return new SemanticWheelValue(0, y, s, java.util.Optional.empty(), java.util.Optional.empty(), java.util.List.of()); }
}
