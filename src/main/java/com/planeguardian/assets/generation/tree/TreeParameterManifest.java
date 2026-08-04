package com.planeguardian.assets.generation.tree;

import java.util.List;
import java.util.Objects;

/** AI-readable projection of the public schema; derived semantic outputs are deliberately absent. */
public record TreeParameterManifest(int schemaVersion, List<TreeParameterSchema.Parameter> parameters,
                                    List<String> editableSemanticWheels) {
    public TreeParameterManifest {
        if (schemaVersion != TreeParameterSchema.current().version()) throw new IllegalArgumentException("Unsupported schema version");
        parameters = List.copyOf(parameters);
        editableSemanticWheels = List.copyOf(editableSemanticWheels);
        if (!parameters.equals(TreeParameterSchema.current().parameters())) throw new IllegalArgumentException("Manifest must expose the current schema");
    }

    public static TreeParameterManifest current() {
        return new TreeParameterManifest(TreeParameterSchema.current().version(), TreeParameterSchema.current().parameters(),
                List.of("vitality", "regularity", "transformation", "genesis", "water", "fire"));
    }
}
