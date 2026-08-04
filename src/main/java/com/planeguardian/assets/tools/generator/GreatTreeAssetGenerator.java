package com.planeguardian.assets.tools.generator;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.planeguardian.assets.generation.adapters.jme.TreePreviewJmeAdapter;
import com.planeguardian.assets.generation.api.RenderTier;
import com.planeguardian.assets.generation.preview.RuntimeWeatherInput;
import com.planeguardian.assets.generation.preview.TreePreviewFixture;
import com.planeguardian.assets.generation.tree.DeciduousTreeStructureGenerator;
import com.planeguardian.assets.generation.tree.TreeBranchLevel;
import com.planeguardian.assets.generation.tree.TreeComposition;
import com.planeguardian.assets.generation.tree.TreeCrownSettings;
import com.planeguardian.assets.generation.tree.TreeFeatureSettings;
import com.planeguardian.assets.generation.tree.TreeLodSettings;
import com.planeguardian.assets.generation.tree.TreeParameterSchema;
import com.planeguardian.assets.generation.tree.TreePresentationSettings;
import com.planeguardian.assets.generation.tree.TreeRootSettings;
import com.planeguardian.assets.generation.tree.TreeStructure;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Asset Generator UI adapter for the versioned, registered Great Tree generator. */
public final class GreatTreeAssetGenerator implements AssetGenerator {
    public static final String GENERATOR_ID = "pg.tree.deciduous/1";
    private final Map<String, JSpinner> controls = new LinkedHashMap<>();
    private final JTextField name = new JTextField("GreatTree_01", 22);
    private final JTextField seed = new JTextField("42", 14);

    @Override
    public String generatorId() {
        return GENERATOR_ID;
    }

    @Override
    public String getName() {
        return "Deciduous Great Tree";
    }

    @Override
    public JPanel buildParameterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 6, 4, 6);
        constraints.anchor = GridBagConstraints.WEST;
        int row = add(panel, constraints, 0, "Asset Name", name);
        row = add(panel, constraints, row, "Seed", seed);
        for (TreeParameterSchema.Parameter parameter : TreeParameterSchema.current().parameters()) {
            JSpinner spinner = spinner(parameter);
            spinner.setName(parameter.id().value());
            controls.put(parameter.id().value(), spinner);
            row = add(panel, constraints, row, parameter.description() + " (" + parameter.unit() + ")", spinner);
        }
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        panel.add(new JLabel("All controls are versioned by TreeParameterSchema v"
                + TreeParameterSchema.current().version() + "."), constraints);
        return panel;
    }

    @Override
    public GenerationResult generate(Path outputDirectory) {
        try {
            Input input = snapshot();
            Files.createDirectories(outputDirectory);
            var structure = input.structure();
            var composition = input.composition();
            var structural = DeciduousTreeStructureGenerator.generate(structure, composition, input.seed());
            var crown = DeciduousTreeStructureGenerator.generateCrown(structure, composition, input.seed());
            var scene = TreePreviewJmeAdapter.create(new DesktopAssetManager(true), structural, crown,
                    TreePreviewFixture.gameplay(), input.presentation(), RuntimeWeatherInput.calm(), input.seed()).root();
            Path output = outputDirectory.resolve(sanitise(input.name()) + ".j3o");
            BinaryExporter.getInstance().save(scene, output.toFile());
            return GenerationResult.success(output, input.name(), "Generated registered Deciduous Great Tree.",
                    GENERATOR_ID, structural.fingerprint().hex());
        } catch (IOException | RuntimeException exception) {
            return GenerationResult.failure("Failed to generate Deciduous Great Tree: " + exception.getMessage());
        }
    }

    private Input snapshot() {
        long visualSeed = Long.parseLong(seed.getText().trim());
        String assetName = name.getText().trim();
        if (assetName.isBlank()) throw new IllegalArgumentException("Asset name cannot be blank");
        Map<String, Double> values = new LinkedHashMap<>();
        for (TreeParameterSchema.Parameter parameter : TreeParameterSchema.current().parameters()) {
            values.put(parameter.id().value(), ((Number) controls.get(parameter.id().value()).getValue()).doubleValue());
        }
        TreeStructure structure = new TreeStructure(decimal(values, "tree.height-metres"),
                decimal(values, "tree.base-radius-metres"), decimal(values, "tree.taper-exponent"),
                decimal(values, "tree.lean-x"), decimal(values, "tree.lean-z"), decimal(values, "tree.curvature"),
                decimal(values, "tree.twist-radians"), integer(values, "tree.trunk-ring-count"),
                integer(values, "tree.trunk-vertices-per-ring"));
        TreeBranchLevel branch = new TreeBranchLevel(integer(values, "tree.branch.maximum-children"),
                decimal(values, "tree.branch.attachment-start"), decimal(values, "tree.branch.attachment-end"),
                decimal(values, "tree.branch.length-ratio"), decimal(values, "tree.branch.radius-ratio"),
                decimal(values, "tree.branch.elevation"), integer(values, "tree.branch.ring-count"),
                integer(values, "tree.branch.vertices-per-ring"));
        TreeRootSettings roots = new TreeRootSettings(integer(values, "tree.root.count"),
                decimal(values, "tree.root.flare-multiplier"), decimal(values, "tree.root.length-ratio"),
                decimal(values, "tree.root.exposed-fraction"), integer(values, "tree.root.ring-count"),
                integer(values, "tree.root.vertices-per-ring"));
        TreeCrownSettings crown = new TreeCrownSettings(decimal(values, "tree.crown.coverage"),
                integer(values, "tree.crown.maximum-clusters"), decimal(values, "tree.crown.width-ratio"),
                decimal(values, "tree.crown.height-ratio"), decimal(values, "tree.crown.vertical-offset-ratio"),
                integer(values, "tree.crown.latitude-bands"), integer(values, "tree.crown.radial-segments"));
        TreeFeatureSettings features = new TreeFeatureSettings(integer(values, "tree.feature.maximum-moss"),
                integer(values, "tree.feature.maximum-vines"), integer(values, "tree.feature.maximum-flowers"),
                integer(values, "tree.feature.maximum-fruit"), integer(values, "tree.feature.maximum-fungi"),
                integer(values, "tree.feature.maximum-total"));
        TreeComposition composition = new TreeComposition(1, java.util.List.of(branch), roots,
                new TreeLodSettings(integer(values, "tree.lod.tier"), integer(values, "tree.lod.branch-level-limit")),
                integer(values, "tree.maximum-components"), crown, features);
        int renderTier = integer(values, "tree.render-tier");
        TreePresentationSettings presentation = new TreePresentationSettings(RenderTier.values()[renderTier],
                decimal(values, "tree.host-contact-blend"), decimal(values, "tree.motion.wind-amplitude"),
                decimal(values, "tree.motion.wind-frequency"), decimal(values, "tree.motion.response"),
                decimal(values, "tree.surface.roughness-bias"), decimal(values, "tree.surface.normal-strength"),
                decimal(values, "tree.surface.emission-strength"));
        return new Input(assetName, visualSeed, structure, composition, presentation);
    }

    private static JSpinner spinner(TreeParameterSchema.Parameter parameter) {
        String range = parameter.allowedRange();
        double minimum = range.startsWith("(0") ? .01 : parseBound(range, true);
        double maximum = range.contains("infinity") ? Double.MAX_VALUE : parseBound(range, false);
        Number value = parameter.type() == TreeParameterSchema.Type.INTEGER
                ? (int) parameter.defaultValue() : parameter.defaultValue();
        return parameter.type() == TreeParameterSchema.Type.INTEGER
                ? new JSpinner(new SpinnerNumberModel(value.intValue(), (int) minimum, (int) maximum, 1))
                : new JSpinner(new SpinnerNumberModel(value.doubleValue(), minimum, maximum, .01));
    }

    private static double parseBound(String range, boolean lower) {
        String token = range.substring(1, range.length() - 1).split(",")[lower ? 0 : 1].trim();
        if ("attachment-start".equals(token)) return 0;
        return switch (token) {
            case "-2pi" -> -2 * StrictMath.PI;
            case "2pi" -> 2 * StrictMath.PI;
            default -> Double.parseDouble(token);
        };
    }

    private static int add(JPanel panel, GridBagConstraints constraints, int row, String label, java.awt.Component field) {
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        panel.add(field, constraints);
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        return row + 1;
    }

    private static int integer(Map<String, Double> values, String id) {
        return values.get(id).intValue();
    }

    private static double decimal(Map<String, Double> values, String id) {
        return values.get(id);
    }

    private static String sanitise(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private record Input(String name, long seed, TreeStructure structure, TreeComposition composition,
                         TreePresentationSettings presentation) {
    }
}
