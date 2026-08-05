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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Asset Generator UI adapter for the versioned, registered Great Tree generator. */
public final class GreatTreeAssetGenerator implements AuthoringGeneratorProvider {
    public static final String GENERATOR_ID = "pg.tree.deciduous/1";
    @Override
    public com.planeguardian.assets.generation.api.GeneratorDescriptor descriptor() {
        List<com.planeguardian.assets.generation.api.GeneratorDescriptor.Parameter> parameters =
                TreeParameterSchema.current().parameters().stream().map(parameter ->
                        new com.planeguardian.assets.generation.api.GeneratorDescriptor.Parameter(parameter.id(),
                                parameter.description() + " (" + parameter.unit() + ")",
                                parameter.type().name().toLowerCase(java.util.Locale.ROOT),
                                format(parameter.defaultValue(), parameter.type()), parameter.allowedRange(),
                                !isPrimary(parameter.id().value()))).toList();
        TreeMap<String, String> defaults = new TreeMap<>();
        parameters.forEach(parameter -> defaults.put(parameter.id().value(), parameter.defaultValue()));
        return new com.planeguardian.assets.generation.api.GeneratorDescriptor(
                new com.planeguardian.assets.generation.api.StableId(GENERATOR_ID),
                new com.planeguardian.assets.generation.api.ContractVersion(1, 0),
                new com.planeguardian.assets.generation.api.StableId("asset-family.vegetation.tree"),
                "Deciduous Great Tree", parameters,
                List.of(new com.planeguardian.assets.generation.api.GeneratorDescriptor.Preset(
                        new com.planeguardian.assets.generation.api.StableId("preset.tree.great-oak"), "Great Oak", defaults)),
                Set.of(new com.planeguardian.assets.generation.api.StableId("runtime.wind")),
                Set.of("j3o"), Set.of("j3o", "gltf", "glb"),
                Set.of(new com.planeguardian.assets.generation.api.StableId("tree.bark"),
                        new com.planeguardian.assets.generation.api.StableId("tree.foliage")),
                Set.of(new com.planeguardian.assets.generation.api.StableId("socket.tree.crown")),
                Set.of(new com.planeguardian.assets.generation.api.StableId("tree.crown.coverage")));
    }

    @Override
    public java.util.Optional<com.planeguardian.assets.generation.semantics.AssetSemanticAdapter> semanticAdapter() {
        return java.util.Optional.of(new com.planeguardian.assets.generation.tree.GreatTreeSemanticAdapter());
    }

    @Override
    public GenerationResult generate(AuthoringGenerationRequest request, Path outputDirectory) {
        try {
            Input input = snapshot(request);
            Files.createDirectories(outputDirectory);
            var structure = input.structure();
            var composition = input.composition();
            var structural = DeciduousTreeStructureGenerator.generate(structure, composition, input.seed());
            var crown = DeciduousTreeStructureGenerator.generateCrown(structure, composition, input.seed());
            var scene = TreePreviewJmeAdapter.create(new DesktopAssetManager(true), structural, crown,
                    TreePreviewFixture.gameplay(), input.presentation(), RuntimeWeatherInput.calm(), input.seed()).root();
            scene.setUserData("pg.assetId", "asset.authoring." + sanitise(input.name()).toLowerCase(java.util.Locale.ROOT));
            scene.setUserData("pg.generatorId", GENERATOR_ID);
            Path output = outputDirectory.resolve(sanitise(input.name()) + ".j3o");
            BinaryExporter.getInstance().save(scene, output.toFile());
            return GenerationResult.success(output, input.name(), "Generated registered Deciduous Great Tree.",
                    GENERATOR_ID, structural.fingerprint().hex());
        } catch (IOException | RuntimeException exception) {
            return GenerationResult.failure("Failed to generate Deciduous Great Tree: " + exception.getMessage());
        }
    }

    private Input snapshot(AuthoringGenerationRequest request) {
        long visualSeed = request.visualSeed();
        String assetName = request.assetName();
        Map<String, Double> values = new TreeMap<>();
        for (TreeParameterSchema.Parameter parameter : TreeParameterSchema.current().parameters()) {
            String value = request.directParameters().get(parameter.id().value());
            if (value == null) throw new IllegalArgumentException("Missing parameter: " + parameter.id());
            values.put(parameter.id().value(), Double.parseDouble(value));
        }
        values = com.planeguardian.assets.generation.authoring.ParameterPrecedence.resolve(descriptor(), values,
                semanticAdapter().orElseThrow().resolve(request.sourceSemantics(),
                        com.planeguardian.assets.generation.semantics.AssetSemanticAdapter.ResolutionContext.intrinsicOnly()),
                request.explicitOverrides());
        TreeStructure structure = new TreeStructure(decimal(values, "tree.height-metres"),
                decimal(values, "tree.base-radius-metres"), decimal(values, "tree.taper-exponent"),
                decimal(values, "tree.lean-x"), decimal(values, "tree.lean-z"), decimal(values, "tree.curvature"),
                decimal(values, "tree.gnarliness"), decimal(values, "tree.gnarliness-frequency"),
                integer(values, "tree.split.count"), decimal(values, "tree.split.start"),
                decimal(values, "tree.split.departure-angle"),
                decimal(values, "tree.twist-radians"), integer(values, "tree.trunk-ring-count"),
                integer(values, "tree.trunk-vertices-per-ring"));
        TreeBranchLevel branch = new TreeBranchLevel(integer(values, "tree.branch.maximum-children"),
                decimal(values, "tree.branch.attachment-start"), decimal(values, "tree.branch.attachment-end"),
                decimal(values, "tree.branch.length-ratio"), decimal(values, "tree.branch.radius-ratio"),
                decimal(values, "tree.branch.elevation"), decimal(values, "tree.branch.departure-angle-min"),
                decimal(values, "tree.branch.departure-angle-max"), decimal(values, "tree.branch.curvature"),
                decimal(values, "tree.branch.gnarliness"),
                decimal(values, "tree.branch.gnarliness-frequency"), integer(values, "tree.branch.ring-count"),
                integer(values, "tree.branch.vertices-per-ring"));
        TreeRootSettings roots = new TreeRootSettings(integer(values, "tree.root.count"),
                decimal(values, "tree.root.flare-multiplier"), decimal(values, "tree.root.length-ratio"),
                decimal(values, "tree.root.exposed-fraction"), decimal(values, "tree.root.curvature"),
                decimal(values, "tree.root.gnarliness"),
                decimal(values, "tree.root.gnarliness-frequency"), integer(values, "tree.root.ring-count"),
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

    private static boolean isPrimary(String id) { return Set.of("tree.height-metres", "tree.base-radius-metres", "tree.branch.maximum-children", "tree.crown.coverage", "tree.render-tier").contains(id); }
    private static String format(double value, TreeParameterSchema.Type type) { return type == TreeParameterSchema.Type.INTEGER ? Integer.toString((int) value) : Double.toString(value); }

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
