package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.tree.TreeParameterSchema;
import com.planeguardian.assets.tools.generator.assetgenerator.generation.GreatTreeAssetGenerator;
import org.junit.jupiter.api.Test;

import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import com.planeguardian.assets.generation.api.StableId;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreatTreeAssetGeneratorTest {
    @Test
    void registersTheRuntimeGreatTreeAndExposesEveryVersionedControl() {
        assertTrue(AssetGeneratorTool.registeredGenerators().stream().map(provider -> provider.descriptor().generatorId())
                .collect(java.util.stream.Collectors.toSet()).contains(new StableId(GreatTreeAssetGenerator.GENERATOR_ID)));

        GreatTreeAssetGenerator generator = new GreatTreeAssetGenerator();
        Set<String> exposed = generator.descriptor().parameters().stream().map(parameter -> parameter.id().value())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> expected = TreeParameterSchema.current().parameters().stream()
                .map(parameter -> parameter.id().value())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(expected, exposed);
        assertTrue(exposed.stream().allMatch(id -> !id.isBlank()));
        assertEquals("Great Oak", generator.descriptor().presets().get(0).displayName());
        assertEquals(expected, generator.descriptor().presets().get(0).parameterValues().keySet());
    }

    @Test
    void registryIsDeterministicAndNotTreeShaped() {
        AuthoringGeneratorProvider rock = new StubProvider("generator.rock/1", "Rock");
        AuthoringGeneratorProvider tree = new StubProvider("generator.tree/1", "Tree");
        AuthoringGeneratorRegistry registry = new AuthoringGeneratorRegistry(List.of(tree, rock));
        assertEquals(List.of(new StableId("generator.rock/1"), new StableId("generator.tree/1")),
                registry.providers().stream().map(provider -> provider.descriptor().generatorId()).toList());
        assertEquals(rock, registry.require(new StableId("generator.rock/1")));
        assertThrows(IllegalArgumentException.class,
                () -> new AuthoringGeneratorRegistry(List.of(rock, new StubProvider("generator.rock/1", "Duplicate"))));
    }

    @Test
    void schemaDefaultsPopulateEditableWorkbenchControls() throws Exception {
        GreatTreeAssetGenerator provider = new GreatTreeAssetGenerator();
        GeneratorParameterEditor[] holder = new GeneratorParameterEditor[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> holder[0] = new GeneratorParameterEditor(provider));
        AuthoringGenerationRequest initial = holder[0].snapshot();
        provider.descriptor().presets().get(0).parameterValues().forEach((id, expected) ->
                assertEquals(Double.parseDouble(expected),
                        Double.parseDouble(initial.directParameters().get(id)), 0.000_001, id));
        javax.swing.JSpinner initialHeight = findSpinner(holder[0].panel(), "tree.height-metres");
        assertTrue(initialHeight.getPreferredSize().width <= 220);
        assertEquals(12d, java.text.NumberFormat.getNumberInstance().parse(
                ((javax.swing.JSpinner.DefaultEditor) initialHeight.getEditor()).getTextField().getText())
                .doubleValue(), 0.000_001);

        javax.swing.JSpinner height = initialHeight;
        javax.swing.SwingUtilities.invokeAndWait(() -> height.setValue(18d));
        assertEquals("18.0", holder[0].snapshot().directParameters().get("tree.height-metres"));

        String localizedDecimal = java.text.NumberFormat.getNumberInstance().format(19.75);
        javax.swing.SwingUtilities.invokeAndWait(() -> ((javax.swing.JSpinner.DefaultEditor) height.getEditor())
                .getTextField().setText(localizedDecimal));
        assertEquals("19.75", holder[0].snapshot().directParameters().get("tree.height-metres"));
    }

    @Test
    void topologyFailuresProduceAnActionableBoundedSummary() {
        String technical = "Failed: Cannot triangulate invalid topology: [TopologyIssue[code=self-intersecting-face]]";
        AssetGeneratorTool.FailurePresentation failure = AssetGeneratorTool.presentFailure(technical);
        assertTrue(failure.summary().contains("reducing"));
        assertTrue(failure.summary().contains("ring count"));
        assertEquals(technical, failure.details());
        assertTrue(failure.summary().length() < 400);
    }

    private static javax.swing.JSpinner findSpinner(java.awt.Container container, String name) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof javax.swing.JSpinner spinner && name.equals(spinner.getName())) return spinner;
            if (component instanceof java.awt.Container child) {
                try { return findSpinner(child, name); } catch (IllegalArgumentException ignored) { }
            }
        }
        throw new IllegalArgumentException("Missing spinner: " + name);
    }

    private record StubProvider(GeneratorDescriptor descriptor) implements AuthoringGeneratorProvider {
        private StubProvider(String id, String name) {
            this(new GeneratorDescriptor(new StableId(id), new com.planeguardian.assets.generation.api.ContractVersion(1, 0),
                    new StableId("asset-family.fixture"), name, List.of(), List.of(), Set.of(), Set.of("j3o"),
                    Set.of("glb"), Set.of(), Set.of(), Set.of()));
        }
        @Override public GenerationResult generate(AuthoringGenerationRequest request, Path outputDirectory) {
            return GenerationResult.failure("fixture");
        }
    }
}
