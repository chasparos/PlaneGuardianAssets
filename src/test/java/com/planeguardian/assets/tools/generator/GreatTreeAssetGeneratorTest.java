package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.tree.TreeParameterSchema;
import org.junit.jupiter.api.Test;

import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreatTreeAssetGeneratorTest {
    @Test
    void registersTheRuntimeGreatTreeAndExposesEveryVersionedControl() {
        assertEquals(Set.of(GreatTreeAssetGenerator.GENERATOR_ID),
                AssetGeneratorTool.registeredGenerators().stream().map(AssetGenerator::generatorId)
                        .collect(java.util.stream.Collectors.toSet()));

        GreatTreeAssetGenerator generator = new GreatTreeAssetGenerator();
        Set<String> exposed = namedSpinners(generator.buildParameterPanel());
        Set<String> expected = TreeParameterSchema.current().parameters().stream()
                .map(parameter -> parameter.id().value())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(expected, exposed);
        assertTrue(exposed.stream().allMatch(id -> !id.isBlank()));
    }

    private static Set<String> namedSpinners(Container container) {
        Set<String> names = new HashSet<>();
        for (Component component : container.getComponents()) {
            if (component instanceof JSpinner spinner) names.add(spinner.getName());
            if (component instanceof Container child) names.addAll(namedSpinners(child));
        }
        return names;
    }
}
