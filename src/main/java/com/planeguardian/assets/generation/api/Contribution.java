package com.planeguardian.assets.generation.api;

import java.util.Objects;

/** Inspectable explanation of one resolved parameter influence. */
public record Contribution(
        String targetParameter,
        String source,
        double amount,
        String explanation) {

    public Contribution {
        requireText(targetParameter, "targetParameter");
        requireText(source, "source");
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("amount must be finite");
        }
        Objects.requireNonNull(explanation, "explanation");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
