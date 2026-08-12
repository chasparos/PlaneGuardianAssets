package com.planeguardian.assets.assetgenerator.crystal.generation;

import com.planeguardian.assets.generation.api.StableId;
import java.util.Objects;
import java.util.Optional;

/** Direct immutable crystal generation controls. */
public record CrystalParameters(double baseRadius, double tipTaper, int facetCount, int facetRows, int clusterMemberCount,
                                double sizeScale, CutStyle cutStyle, SettingKind settingKind, StableId paletteEntryId,
                                Optional<Double> hueOverrideDegrees) {
    public CrystalParameters(double baseRadius, double tipTaper, int facetCount, int facetRows, int clusterMemberCount,
                             double sizeScale, SettingKind settingKind, StableId paletteEntryId,
                             Optional<Double> hueOverrideDegrees) {
        this(baseRadius, tipTaper, facetCount, facetRows, clusterMemberCount, sizeScale, CutStyle.PRISM,
                settingKind, paletteEntryId, hueOverrideDegrees);
    }
    public CrystalParameters {
        requireRange(baseRadius, 0.05, 4, "baseRadius");
        requireRange(tipTaper, 0.05, 1, "tipTaper");
        if (facetCount < 4 || facetCount > 16) throw new IllegalArgumentException("facetCount");
        if (facetRows < 2 || facetRows > 16) throw new IllegalArgumentException("facetRows");
        if (clusterMemberCount < 1 || clusterMemberCount > 4) throw new IllegalArgumentException("clusterMemberCount");
        requireRange(sizeScale, 0.25, 4, "sizeScale");
        Objects.requireNonNull(cutStyle, "cutStyle");
        Objects.requireNonNull(settingKind, "settingKind");
        Objects.requireNonNull(paletteEntryId, "paletteEntryId");
        hueOverrideDegrees = Objects.requireNonNull(hueOverrideDegrees, "hueOverrideDegrees");
        hueOverrideDegrees.ifPresent(value -> requireRange(value, -180, 180, "hueOverrideDegrees"));
    }

    public static CrystalParameters defaults() {
        return new CrystalParameters(0.35, 0.18, 6, 5, 1, 1, CutStyle.PRISM, SettingKind.NATURAL_ROCK,
                new StableId("palette.gem.quartz-clear"), Optional.empty());
    }

    public enum CutStyle { PRISM, CUSHION, BRILLIANT }
    public enum SettingKind { NATURAL_ROCK, LEVITATION }

    private static void requireRange(double value, double min, double max, String name) {
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException(name + " must be in [" + min + ", " + max + "]");
    }
}
