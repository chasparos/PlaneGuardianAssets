package com.planeguardian.assets.generation.api;

import java.util.Objects;

public record GeneratedSocket(StableId socketId, StableId role, Transform transform) {
    public GeneratedSocket {
        Objects.requireNonNull(socketId, "socketId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(transform, "transform");
    }
}
