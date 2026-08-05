package com.planeguardian.assets.generation.semantics;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Engine-neutral semantic wheel coordinate with independent importance. */
public record SemanticWheelValue(double x, double y, double salience,
                                 Optional<RelationshipMode> relationshipMode,
                                 Optional<String> focus, List<Pole> secondaryPoles) {
    private static final double EPSILON = 1e-12;

    public SemanticWheelValue {
        requireFinite(x, "x"); requireFinite(y, "y"); requireUnit(salience, "salience");
        if (x * x + y * y > 1 + EPSILON) throw new IllegalArgumentException("Wheel point must lie inside the unit circle");
        relationshipMode = Objects.requireNonNull(relationshipMode, "relationshipMode");
        focus = Objects.requireNonNull(focus, "focus").map(String::trim);
        if (focus.filter(String::isEmpty).isPresent()) throw new IllegalArgumentException("focus must not be blank");
        secondaryPoles = List.copyOf(secondaryPoles);
    }

    public static SemanticWheelValue centered(double salience) {
        return new SemanticWheelValue(0, 0, salience, Optional.empty(), Optional.empty(), List.of());
    }

    public double extremity() { return StrictMath.sqrt(x * x + y * y); }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
    }
    private static void requireUnit(double value, String name) {
        requireFinite(value, name);
        if (value < 0 || value > 1) throw new IllegalArgumentException(name + " must be in [0, 1]");
    }

    public enum RelationshipMode { EQUILIBRIUM, SYNTHESIS, CONFLICT, ALTERNATION, SUPPRESSION, PARADOX }

    public record Pole(double x, double y, double weight) {
        public Pole {
            requireFinite(x, "pole.x"); requireFinite(y, "pole.y"); requireUnit(weight, "pole.weight");
            if (x * x + y * y > 1 + EPSILON) throw new IllegalArgumentException("Pole must lie inside the unit circle");
        }
    }
}
