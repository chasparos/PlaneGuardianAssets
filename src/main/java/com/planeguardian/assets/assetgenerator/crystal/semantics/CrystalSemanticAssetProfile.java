package com.planeguardian.assets.assetgenerator.crystal.semantics;

import com.planeguardian.assets.assetgenerator.crystal.generation.CrystalParameters;
import com.planeguardian.assets.generation.api.Contribution;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.palette.PaletteEntry;
import java.util.List;
import java.util.Objects;

/** Engine-neutral bounded semantic channels shared by crystal-bearing generators. */
public record CrystalSemanticAssetProfile(
        ColorCharacter colorCharacter,
        ShapeCharacter shapeCharacter,
        SizeCharacter sizeCharacter,
        ComplexityCharacter complexityCharacter,
        RadianceCharacter radianceCharacter,
        VfxDecoratorCharacter vfxDecoratorCharacter,
        SettingCharacter settingCharacter) {
    public CrystalSemanticAssetProfile {
        Objects.requireNonNull(colorCharacter, "colorCharacter");
        Objects.requireNonNull(shapeCharacter, "shapeCharacter");
        Objects.requireNonNull(sizeCharacter, "sizeCharacter");
        Objects.requireNonNull(complexityCharacter, "complexityCharacter");
        Objects.requireNonNull(radianceCharacter, "radianceCharacter");
        Objects.requireNonNull(vfxDecoratorCharacter, "vfxDecoratorCharacter");
        Objects.requireNonNull(settingCharacter, "settingCharacter");
    }

    public record ColorCharacter(PaletteEntry paletteEntry, double saturationBias, double clarity, List<Contribution> trace) {
        public ColorCharacter { Objects.requireNonNull(paletteEntry, "paletteEntry"); trace = List.copyOf(trace); bounded(saturationBias); bounded(clarity); }
    }
    public record ShapeCharacter(double facetSharpness, double growthBias, double axisLean, List<Contribution> trace) {
        public ShapeCharacter { trace = List.copyOf(trace); bounded(facetSharpness); signed(growthBias); signed(axisLean); }
    }
    public record SizeCharacter(double prominence, double scaleBias, List<Contribution> trace) {
        public SizeCharacter { trace = List.copyOf(trace); bounded(prominence); signed(scaleBias); }
    }
    public record ComplexityCharacter(double facetDensity, double irregularity, int clusterCount, List<Contribution> trace) {
        public ComplexityCharacter { trace = List.copyOf(trace); bounded(facetDensity); bounded(irregularity); if (clusterCount < 1 || clusterCount > 4) throw new IllegalArgumentException("clusterCount"); }
    }
    public record RadianceCharacter(double intensity, double opacity, double emissionTintStrength, List<Contribution> trace) {
        public RadianceCharacter { trace = List.copyOf(trace); bounded(intensity); bounded(opacity); bounded(emissionTintStrength); }
    }
    public record VfxDecoratorCharacter(StableId providerHint, double strength, List<Contribution> trace) {
        public VfxDecoratorCharacter { Objects.requireNonNull(providerHint, "providerHint"); trace = List.copyOf(trace); bounded(strength); }
    }
    public record SettingCharacter(CrystalParameters.SettingKind preferredSetting, double confidence, List<Contribution> trace) {
        public SettingCharacter { Objects.requireNonNull(preferredSetting, "preferredSetting"); trace = List.copyOf(trace); bounded(confidence); }
    }

    private static void bounded(double value) { if (!Double.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException("bounded value required"); }
    private static void signed(double value) { if (!Double.isFinite(value) || value < -1 || value > 1) throw new IllegalArgumentException("signed value required"); }
}
