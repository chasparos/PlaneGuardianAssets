package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.StableId;

import java.util.List;
import java.util.Objects;

/** Versioned public controls for the bounded engine-neutral Great Tree structure. */
public record TreeParameterSchema(int version, List<Parameter> parameters) {
    public TreeParameterSchema {
        if (version != 2) throw new IllegalArgumentException("Unsupported tree parameter schema version");
        parameters = List.copyOf(parameters);
        if (parameters.isEmpty()) throw new IllegalArgumentException("Tree parameter schema must not be empty");
        if (parameters.stream().map(Parameter::id).distinct().count() != parameters.size()) {
            throw new IllegalArgumentException("Tree parameter schema contains duplicate IDs");
        }
    }

    public static TreeParameterSchema current() {
        return new TreeParameterSchema(2, List.of(
                decimal("tree.height-metres", "Trunk height", "metres", "(0, infinity)", 12),
                decimal("tree.base-radius-metres", "Trunk base radius", "metres", "(0, infinity)", .45),
                decimal("tree.taper-exponent", "Trunk taper exponent", "ratio", "[0, 4]", 1.15),
                decimal("tree.lean-x", "Trunk lateral X lean", "ratio", "[-0.5, 0.5]", 0),
                decimal("tree.lean-z", "Trunk lateral Z lean", "ratio", "[-0.5, 0.5]", 0),
                decimal("tree.curvature", "Trunk curvature", "ratio", "[0, 0.5]", .08),
                decimal("tree.gnarliness", "Trunk gnarliness", "ratio", "[0, 0.5]", .035),
                decimal("tree.gnarliness-frequency", "Trunk gnarliness frequency", "waves", "[0, 12]", 3),
                integer("tree.split.count", "Trunk split count", "count", "[0, 3]", 0),
                decimal("tree.split.start", "First trunk split height", "fraction", "[0.15, 0.9]", .55),
                decimal("tree.split.departure-angle", "Trunk split departure angle", "radians", "[0.05, 1.4]", .5),
                decimal("tree.twist-radians", "Trunk twist", "radians", "[-2pi, 2pi]", 0),
                integer("tree.trunk-ring-count", "Trunk ring count", "count", "[2, 128]", 16),
                integer("tree.trunk-vertices-per-ring", "Trunk vertices per ring", "count", "[8, 64]", 12),
                integer("tree.branch.maximum-children", "Maximum children per branch level", "count", "[1, 32]", 6),
                decimal("tree.branch.attachment-start", "Branch attachment start", "fraction", "[0, 1]", .30),
                decimal("tree.branch.attachment-end", "Branch attachment end", "fraction", "[attachment-start, 1]", .82),
                decimal("tree.branch.length-ratio", "Branch length ratio", "ratio", "[0.05, 1]", .36),
                decimal("tree.branch.radius-ratio", "Branch radius relative to parent at attachment", "ratio", "[0.02, 0.8]", .28),
                decimal("tree.branch.elevation", "Branch elevation", "ratio", "[-0.5, 1]", .42),
                decimal("tree.branch.departure-angle-min", "Minimum branch departure angle", "radians", "[0.05, 2.9845]", .45),
                decimal("tree.branch.departure-angle-max", "Maximum branch departure angle", "radians", "[0.05, 2.9845]", 1.15),
                decimal("tree.branch.curvature", "Branch master curvature", "ratio", "[-1, 1]", .25),
                decimal("tree.branch.gnarliness", "Branch gnarliness", "ratio", "[0, 0.5]", .10),
                decimal("tree.branch.gnarliness-frequency", "Branch gnarliness frequency", "waves", "[0, 12]", 2.5),
                integer("tree.branch.ring-count", "Branch ring count", "count", "[2, 64]", 12),
                integer("tree.branch.vertices-per-ring", "Branch vertices per ring", "count", "[8, 32]", 8),
                integer("tree.root.count", "Major root count", "count", "[4, 8]", 5),
                decimal("tree.root.flare-multiplier", "Root flare multiplier", "ratio", "[1, 3]", 1.55),
                decimal("tree.root.length-ratio", "Root length ratio", "ratio", "[0.1, 2]", .30),
                decimal("tree.root.exposed-fraction", "Exposed root fraction", "fraction", "[0, 1]", .45),
                decimal("tree.root.curvature", "Root master curvature", "ratio", "[-1, 1]", .25),
                decimal("tree.root.gnarliness", "Root gnarliness", "ratio", "[0, 0.5]", .12),
                decimal("tree.root.gnarliness-frequency", "Root gnarliness frequency", "waves", "[0, 12]", 2),
                integer("tree.root.ring-count", "Root ring count", "count", "[2, 64]", 12),
                integer("tree.root.vertices-per-ring", "Root vertices per ring", "count", "[8, 32]", 8),
                integer("tree.lod.tier", "Structural LOD tier", "level", "[0, 2]", 0),
                integer("tree.lod.branch-level-limit", "Maximum active branch levels", "level", "[0, 3]", 1),
                integer("tree.maximum-components", "Structural component budget", "components", "[2, 128]", 32),
                decimal("tree.crown.coverage", "Foliage crown coverage", "fraction", "[0, 1]", .72),
                integer("tree.crown.maximum-clusters", "Maximum foliage shell clusters", "count", "[0, 64]", 8),
                decimal("tree.crown.width-ratio", "Crown width relative to tree height", "ratio", "[0.1, 2]", .62),
                decimal("tree.crown.height-ratio", "Crown height relative to tree height", "ratio", "[0.1, 2]", .38),
                decimal("tree.crown.vertical-offset-ratio", "Crown base relative to tree height", "ratio", "[0, 1.5]", .62),
                integer("tree.crown.latitude-bands", "Foliage shell latitude bands", "count", "[2, 16]", 4),
                integer("tree.crown.radial-segments", "Foliage shell radial segments", "count", "[8, 32]", 8),
                integer("tree.feature.maximum-moss", "Maximum moss feature anchors", "count", "[0, 32]", 8),
                integer("tree.feature.maximum-vines", "Maximum vine feature anchors", "count", "[0, 32]", 6),
                integer("tree.feature.maximum-flowers", "Maximum flower feature anchors", "count", "[0, 32]", 12),
                integer("tree.feature.maximum-fruit", "Maximum fruit feature anchors", "count", "[0, 32]", 10),
                integer("tree.feature.maximum-fungi", "Maximum fungal feature anchors", "count", "[0, 32]", 6),
                integer("tree.feature.maximum-total", "Maximum admitted feature anchors", "count", "[0, 96]", 24),
                decimal("tree.host-contact-blend", "Host contact blend strength", "fraction", "[0, 1]", .35),
                decimal("tree.motion.wind-amplitude", "Wind response amplitude", "fraction", "[0, 1]", .45),
                decimal("tree.motion.wind-frequency", "Wind response frequency", "hertz", "[0.01, 10]", .8),
                decimal("tree.motion.response", "Semantic motion response", "fraction", "[0, 1]", .5),
                decimal("tree.surface.roughness-bias", "PBR roughness bias", "fraction", "[-1, 1]", 0),
                decimal("tree.surface.normal-strength", "Normal-map strength", "ratio", "[0, 4]", 1),
                decimal("tree.surface.emission-strength", "PBR emission strength", "fraction", "[0, 1]", 0),
                integer("tree.render-tier", "Preview render tier", "enum-index", "[0, 2]", 1)));
    }

    private static Parameter decimal(String id, String description, String unit, String range, double defaultValue) {
        return new Parameter(new StableId(id), description, Type.DECIMAL, unit, range, defaultValue);
    }

    private static Parameter integer(String id, String description, String unit, String range, int defaultValue) {
        return new Parameter(new StableId(id), description, Type.INTEGER, unit, range, defaultValue);
    }

    public enum Type { DECIMAL, INTEGER }

    public record Parameter(StableId id, String description, Type type, String unit, String allowedRange,
                            double defaultValue) {
        public Parameter {
            Objects.requireNonNull(id, "id");
            requireText(description, "description");
            Objects.requireNonNull(type, "type");
            requireText(unit, "unit");
            requireText(allowedRange, "allowedRange");
            if (!Double.isFinite(defaultValue)) throw new IllegalArgumentException("defaultValue must be finite");
            if (type == Type.INTEGER && defaultValue != StrictMath.rint(defaultValue)) {
                throw new IllegalArgumentException("Integer parameter default must be integral");
            }
        }

        private static void requireText(String value, String name) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
