package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.StableId;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Canonical request to configure one reusable VFX plugin. */
public record VfxGenerationRequest(
        StableId configurationId,
        StableId pluginId,
        long seed,
        Map<StableId, VfxValue> parameters,
        List<VfxAttachment> attachments) {
    public VfxGenerationRequest {
        Objects.requireNonNull(configurationId, "configurationId");
        Objects.requireNonNull(pluginId, "pluginId");
        TreeMap<StableId, VfxValue> ordered = new TreeMap<>();
        ordered.putAll(parameters);
        if (ordered.containsValue(null)) throw new IllegalArgumentException("VFX parameter values must not be null");
        parameters = Collections.unmodifiableMap(ordered);
        attachments = attachments.stream().sorted(Comparator
                .comparing(VfxAttachment::socketId)
                .thenComparing(attachment -> transformKey(attachment.localTransform()))).toList();
    }

    private static String transformKey(com.planeguardian.assets.generation.api.Transform transform) {
        return transform.translation().toString() + transform.rotation() + transform.scale();
    }
}
