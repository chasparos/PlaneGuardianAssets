package com.planeguardian.assets.generation.tree;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeParameterSchemaTest {
    @Test
    void currentSchemaPublishesEveryDirectStructuralControl() {
        TreeParameterSchema schema = TreeParameterSchema.current();

        assertEquals(1, schema.version());
        assertEquals(26, schema.parameters().size());
        assertEquals(schema.parameters().size(), schema.parameters().stream()
                .map(TreeParameterSchema.Parameter::id).distinct().count());
        Set<String> ids = schema.parameters().stream().map(parameter -> parameter.id().value())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(ids.containsAll(Set.of(
                "tree.height-metres", "tree.branch.maximum-children", "tree.root.exposed-fraction",
                "tree.lod.branch-level-limit", "tree.maximum-components")));
    }

    @Test
    void schemaRejectsDuplicateStableIDsAndInvalidIntegerDefaults() {
        TreeParameterSchema.Parameter parameter = TreeParameterSchema.current().parameters().get(0);
        assertThrows(IllegalArgumentException.class, () -> new TreeParameterSchema(1, java.util.List.of(parameter, parameter)));
        assertThrows(IllegalArgumentException.class, () -> new TreeParameterSchema.Parameter(
                parameter.id(), "Integer", TreeParameterSchema.Type.INTEGER, "count", "[0, 1]", .5));
    }
}
