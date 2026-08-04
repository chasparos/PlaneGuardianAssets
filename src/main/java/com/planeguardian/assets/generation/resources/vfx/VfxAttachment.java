package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;

import java.util.Objects;

/** Effect attachment to a stable generated-asset socket plus a local offset. */
public record VfxAttachment(StableId socketId, Transform localTransform) {
    public VfxAttachment {
        Objects.requireNonNull(socketId, "socketId");
        Objects.requireNonNull(localTransform, "localTransform");
    }
}
