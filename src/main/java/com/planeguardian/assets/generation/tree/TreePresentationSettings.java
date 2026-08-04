package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.RenderTier;

import java.util.Objects;

/** Direct version-one presentation controls consumed only by renderer-boundary adapters. */
public record TreePresentationSettings(
        RenderTier renderTier,
        double hostContactBlend,
        double windAmplitude,
        double windFrequency,
        double motionResponse,
        double roughnessBias,
        double normalStrength,
        double emissionStrength) {
    public TreePresentationSettings {
        Objects.requireNonNull(renderTier, "renderTier");
        unit(hostContactBlend, "hostContactBlend");
        unit(windAmplitude, "windAmplitude");
        if (!Double.isFinite(windFrequency) || windFrequency < .01 || windFrequency > 10) {
            throw new IllegalArgumentException("windFrequency must be in [0.01, 10]");
        }
        unit(motionResponse, "motionResponse");
        signedUnit(roughnessBias, "roughnessBias");
        if (!Double.isFinite(normalStrength) || normalStrength < 0 || normalStrength > 4) {
            throw new IllegalArgumentException("normalStrength must be in [0, 4]");
        }
        unit(emissionStrength, "emissionStrength");
    }

    public static TreePresentationSettings defaults() {
        return new TreePresentationSettings(RenderTier.GAMEPLAY, .35, .45, .8, .5, 0, 1, 0);
    }

    private static void unit(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException(name + " must be in [0, 1]");
    }

    private static void signedUnit(double value, String name) {
        if (!Double.isFinite(value) || value < -1 || value > 1) throw new IllegalArgumentException(name + " must be in [-1, 1]");
    }
}
