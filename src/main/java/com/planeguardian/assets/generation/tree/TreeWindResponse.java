package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.determinism.NamedRandomStreams;

import java.util.Objects;

/** Stable renderer-independent wind attributes; weather force remains a runtime input. */
public record TreeWindResponse(StableId partId, double weight, double phaseOffset) {
    public TreeWindResponse {
        Objects.requireNonNull(partId, "partId");
        if (!Double.isFinite(weight) || weight < 0 || weight > 1) throw new IllegalArgumentException("weight must be in [0, 1]");
        if (!Double.isFinite(phaseOffset) || phaseOffset < 0 || phaseOffset > 1) {
            throw new IllegalArgumentException("phaseOffset must be in [0, 1]");
        }
    }

    public static TreeWindResponse forPart(StableId partId, TreePresentationSettings settings, long seed) {
        Objects.requireNonNull(settings, "settings");
        var random = NamedRandomStreams.open(seed, "tree.wind." + partId.value());
        return new TreeWindResponse(partId, settings.windAmplitude() * settings.motionResponse(), random.nextDouble());
    }
}
