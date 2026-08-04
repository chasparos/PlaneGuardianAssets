package com.planeguardian.assets.generation.api;

import java.util.Objects;

public record GenerationDiagnostic(Severity severity, String code, String message) {
    public enum Severity { INFO, WARNING, ERROR }

    public GenerationDiagnostic {
        Objects.requireNonNull(severity, "severity");
        requireText(code, "code");
        requireText(message, "message");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
