package com.planeguardian.assets.generation.tree;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Validated import envelope for proposed direct controls and editable source wheels. */
public record TreeParameterProposal(int schemaVersion, Map<String, Double> parameterValues,
                                    Map<String, Double> semanticWheels, String rationale) {
    private static final Set<String> WHEELS = Set.of("vitality", "regularity", "transformation", "genesis", "water", "fire");

    public TreeParameterProposal {
        if (schemaVersion != TreeParameterSchema.current().version()) throw new IllegalArgumentException("Unsupported proposal schema version");
        parameterValues = Map.copyOf(new TreeMap<>(parameterValues));
        semanticWheels = Map.copyOf(new TreeMap<>(semanticWheels));
        if (rationale == null || rationale.isBlank()) throw new IllegalArgumentException("rationale must not be blank");
        Map<String, TreeParameterSchema.Parameter> schema = TreeParameterSchema.current().parameters().stream()
                .collect(Collectors.toMap(parameter -> parameter.id().value(), parameter -> parameter));
        parameterValues.forEach((id, value) -> validateParameter(schema.get(id), value, id));
        semanticWheels.forEach(TreeParameterProposal::validateWheel);
    }

    private static void validateParameter(TreeParameterSchema.Parameter parameter, Double value, String id) {
        if (parameter == null) throw new IllegalArgumentException("Unknown or derived parameter: " + id);
        if (value == null || !Double.isFinite(value)) throw new IllegalArgumentException("Parameter value must be finite: " + id);
        if (parameter.type() == TreeParameterSchema.Type.INTEGER && value != StrictMath.rint(value)) {
            throw new IllegalArgumentException("Integer parameter value must be integral: " + id);
        }
    }

    private static void validateWheel(String id, Double value) {
        if (!WHEELS.contains(id)) throw new IllegalArgumentException("Unknown or derived semantic wheel: " + id);
        if (value == null || !Double.isFinite(value) || value < -1 || value > 1) {
            throw new IllegalArgumentException("Semantic wheel must be in [-1, 1]: " + id);
        }
        if ((id.equals("water") || id.equals("fire")) && value < 0) {
            throw new IllegalArgumentException("Elemental semantic wheel must be in [0, 1]: " + id);
        }
    }
}
