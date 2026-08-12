package com.planeguardian.assets.assetgenerator.crystal.semantics;

import com.planeguardian.assets.assetgenerator.crystal.generation.CrystalParameters;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.Contribution;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.palette.NamedColorPalette;
import com.planeguardian.assets.generation.palette.PaletteEntry;
import com.planeguardian.assets.generation.semantics.AssetSemanticAdapter;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Crystal semantic translation from lore/game wheels to bounded crystal channels. */
public final class CrystalSemanticAdapter implements AssetSemanticAdapter {
    private static final StableId FAMILY = new StableId("asset-family.gem.crystal");
    private static final ContractVersion VERSION = new ContractVersion(1, 0);
    private static final NamedColorPalette PALETTE = NamedColorPalette.standard();
    /** Shared, generator-neutral VFX plugin identifier for the pollen-motes effect (matches the tree generator's provider). */
    private static final StableId POLLEN_MOTES_VFX_PLUGIN_ID = new StableId("vfx.pollen-motes");

    @Override public StableId adapterId() { return new StableId("adapter.crystal.semantic-profile"); }
    @Override public ContractVersion adapterVersion() { return VERSION; }
    @Override public StableId assetFamily() { return FAMILY; }

    @Override
    public ResolvedVisualProfile resolve(SemanticProfile source, ResolutionContext context) {
        CrystalSemanticAssetProfile profile = profile(source);
        TreeMap<String, Double> channels = new TreeMap<>();
        channels.put("crystal.base-radius", lerp(0.18, 0.72, profile.sizeCharacter().prominence()));
        channels.put("crystal.tip-taper", lerp(0.12, 0.4, profile.shapeCharacter().facetSharpness()));
        channels.put("crystal.facet-count", (double) Math.round(lerp(5, 12, profile.complexityCharacter().facetDensity())));
        channels.put("crystal.facet-rows", (double) Math.round(lerp(3, 8, profile.complexityCharacter().facetDensity())));
        channels.put("crystal.cluster-members", (double) profile.complexityCharacter().clusterCount());
        channels.put("crystal.size-scale", lerp(0.85, 1.8, profile.sizeCharacter().prominence()));
        channels.put("crystal.opacity", lerp(0.25, 0.82, profile.radianceCharacter().opacity()));
        channels.put("crystal.emission-strength", lerp(0.05, 1.0, profile.radianceCharacter().intensity()));
        channels.put("crystal.vfx-strength", profile.vfxDecoratorCharacter().strength());
        channels.put("crystal.setting.levitation", profile.settingCharacter().preferredSetting() == CrystalParameters.SettingKind.LEVITATION ? 1.0 : 0.0);
        channels.put("crystal.color.saturation", profile.colorCharacter().saturationBias());
        channels.put("crystal.color.clarity", profile.colorCharacter().clarity());
        channels.put("crystal.shape.axis-lean", profile.shapeCharacter().axisLean());
        channels.put("crystal.shape.growth-bias", profile.shapeCharacter().growthBias());
        List<Contribution> trace = new ArrayList<>();
        profile.colorCharacter().trace().forEach(trace::add);
        profile.shapeCharacter().trace().forEach(trace::add);
        profile.sizeCharacter().trace().forEach(trace::add);
        profile.complexityCharacter().trace().forEach(trace::add);
        profile.radianceCharacter().trace().forEach(trace::add);
        profile.vfxDecoratorCharacter().trace().forEach(trace::add);
        profile.settingCharacter().trace().forEach(trace::add);
        return new ResolvedVisualProfile(VERSION, channels, trace, fingerprint(channels));
    }

    public CrystalSemanticAssetProfile profile(SemanticProfile source) {
        SemanticWheelValue elemental = wheel(source, "lore.elemental");
        SemanticWheelValue tradition = wheel(source, "lore.magical-tradition");
        SemanticWheelValue manifestation = wheel(source, "lore.manifestation");
        SemanticWheelValue relation = wheel(source, "lore.world-relation");
        SemanticWheelValue power = wheel(source, "game.power");
        SemanticWheelValue rarity = wheel(source, "game.rarity");
        SemanticWheelValue quality = wheel(source, "game.quality");
        PaletteEntry palette = choosePalette(elemental, tradition);
        var color = new CrystalSemanticAssetProfile.ColorCharacter(palette, clamp01(0.45 + rarity.x() * rarity.salience() * 0.35 + tradition.x() * tradition.salience() * 0.15), clamp01(0.5 + quality.x() * quality.salience() * 0.4), List.of(
                contribution("crystal.color.palette", "game.rarity", rarity.x() * rarity.salience()),
                contribution("crystal.color.palette", "lore.magical-tradition", tradition.x() * tradition.salience()),
                contribution("crystal.color.clarity", "game.quality", quality.x() * quality.salience())));
        var shape = new CrystalSemanticAssetProfile.ShapeCharacter(clamp01(0.35 + quality.x() * quality.salience() * 0.25 + rarity.x() * rarity.salience() * 0.2), signed(power.y() * power.salience()), signed(manifestation.y() * manifestation.salience()), List.of(
                contribution("crystal.shape.facet-sharpness", "game.quality", quality.x() * quality.salience()),
                contribution("crystal.shape.axis-lean", "lore.manifestation", manifestation.y() * manifestation.salience())));
        var size = new CrystalSemanticAssetProfile.SizeCharacter(clamp01(0.35 + power.x() * power.salience() * 0.3 + rarity.x() * rarity.salience() * 0.25), signed(power.x() * power.salience()), List.of(
                contribution("crystal.size.prominence", "game.power", power.x() * power.salience()),
                contribution("crystal.size.prominence", "game.rarity", rarity.x() * rarity.salience())));
        int clusterCount = Math.max(1, Math.min(4, (int) Math.round(1 + clamp01((rarity.x() + 1) * 0.5 * rarity.salience() + relation.x() * relation.salience() * 0.15) * 3)));
        var complexity = new CrystalSemanticAssetProfile.ComplexityCharacter(clamp01(0.3 + rarity.x() * rarity.salience() * 0.4 + tradition.x() * tradition.salience() * 0.15), clamp01(0.2 + relation.x() * relation.salience() * 0.25), clusterCount, List.of(
                contribution("crystal.complexity.facet-density", "game.rarity", rarity.x() * rarity.salience()),
                contribution("crystal.complexity.cluster-count", "lore.world-relation", relation.x() * relation.salience())));
        var radiance = new CrystalSemanticAssetProfile.RadianceCharacter(clamp01(0.1 + power.x() * power.salience() * 0.6 + rarity.x() * rarity.salience() * 0.15), clamp01(0.4 + quality.x() * quality.salience() * 0.45), clamp01(0.4 + power.x() * power.salience() * 0.5), List.of(
                contribution("crystal.radiance.intensity", "game.power", power.x() * power.salience()),
                contribution("crystal.radiance.opacity", "game.quality", quality.x() * quality.salience())));
        var vfx = new CrystalSemanticAssetProfile.VfxDecoratorCharacter(POLLEN_MOTES_VFX_PLUGIN_ID, clamp01(0.05 + power.x() * power.salience() * 0.45 + rarity.x() * rarity.salience() * 0.25), List.of(
                contribution("crystal.vfx.strength", "game.power", power.x() * power.salience()),
                contribution("crystal.vfx.strength", "game.rarity", rarity.x() * rarity.salience())));
        CrystalParameters.SettingKind settingKind = manifestation.y() * manifestation.salience() >= 0 ? CrystalParameters.SettingKind.NATURAL_ROCK : CrystalParameters.SettingKind.LEVITATION;
        var setting = new CrystalSemanticAssetProfile.SettingCharacter(settingKind, clamp01(Math.abs(manifestation.y()) * manifestation.salience()), List.of(
                contribution("crystal.setting.preference", "lore.manifestation", manifestation.y() * manifestation.salience())));
        return new CrystalSemanticAssetProfile(color, shape, size, complexity, radiance, vfx, setting);
    }

    private static PaletteEntry choosePalette(SemanticWheelValue elemental, SemanticWheelValue tradition) {
        double angle = Math.atan2(elemental.y(), elemental.x());
        if (Math.cos(angle) > 0.65) return PALETTE.require(new StableId("palette.gem.sapphire-core"));
        if (Math.cos(angle) < -0.65) return PALETTE.require(new StableId("palette.gem.citrine-core"));
        if (Math.sin(angle) > 0.35) return PALETTE.require(new StableId("palette.gem.ruby-core"));
        if (Math.sin(angle) < -0.35) return PALETTE.require(new StableId("palette.gem.emerald-core"));
        return tradition.x() * tradition.salience() > 0 ? PALETTE.require(new StableId("palette.gem.amethyst-core")) : PALETTE.require(new StableId("palette.gem.quartz-clear"));
    }

    private static SemanticWheelValue wheel(SemanticProfile profile, String id) { return profile.wheels().getOrDefault(new StableId(id), SemanticWheelValue.centered(0)); }
    private static double lerp(double min, double max, double t) { return min + (max - min) * t; }
    private static double clamp01(double value) { return Math.max(0, Math.min(1, value)); }
    private static double signed(double value) { return Math.max(-1, Math.min(1, value)); }
    private static Contribution contribution(String target, String source, double amount) { return new Contribution(target, source, amount, Double.toString(amount)); }
    private static ReproducibilityFingerprint fingerprint(Map<String, Double> channels) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            channels.forEach((key, value) -> digest.update((key + "=" + Double.toHexString(value) + "\n").getBytes(StandardCharsets.UTF_8)));
            return new ReproducibilityFingerprint(digest.digest());
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
