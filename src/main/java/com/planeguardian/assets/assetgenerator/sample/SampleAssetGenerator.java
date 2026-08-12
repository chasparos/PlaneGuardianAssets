package com.planeguardian.assets.assetgenerator.sample;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.authoring.ParameterPrecedence;
import com.planeguardian.assets.tools.generator.AuthoringGenerationRequest;
import com.planeguardian.assets.tools.generator.AuthoringGeneratorProvider;
import com.planeguardian.assets.tools.generator.GenerationResult;
import com.planeguardian.assets.assetgenerator.sample.export.SampleAssetExporter;
import com.planeguardian.assets.assetgenerator.sample.export.SampleShapeJmeAdapter;
import com.planeguardian.assets.assetgenerator.sample.generation.SampleShapeGenerator;
import com.planeguardian.assets.assetgenerator.sample.generation.SampleShapeProduct;
import com.planeguardian.assets.assetgenerator.sample.semantics.SampleSemanticAdapter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/** Minimal registered generator used to demonstrate package and semantic boundaries. */
public final class SampleAssetGenerator implements AuthoringGeneratorProvider {
    public static final String GENERATOR_ID = "pg.sample.shape/1";

    @Override
    public GeneratorDescriptor descriptor() {
        var parameters = List.of(
                new GeneratorDescriptor.Parameter(new StableId(SampleSemanticAdapter.SHAPE_PARAMETER),
                        "Cylinder", "boolean", "true", "true|false", false),
                new GeneratorDescriptor.Parameter(new StableId("sample.scale"),
                        "Scale", "number", "1.0", "[0.1,10]", false));
        var defaults = new TreeMap<String, String>();
        defaults.put("sample.cylinder", "true");
        defaults.put("sample.scale", "1.0");
        return new GeneratorDescriptor(new StableId(GENERATOR_ID), new ContractVersion(1, 0),
                new StableId("asset-family.sample"), "Sample Shape", parameters,
                List.of(new GeneratorDescriptor.Preset(new StableId("preset.sample.default"),
                        "Default Shape", defaults)), Set.of(), Set.of("j3o"), Set.of("j3o"),
                Set.of(new StableId("sample.shape")), Set.of(),
                Set.of(new StableId(SampleSemanticAdapter.SHAPE_PARAMETER)));
    }

    @Override
    public Optional<com.planeguardian.assets.generation.semantics.AssetSemanticAdapter> semanticAdapter() {
        return Optional.of(new SampleSemanticAdapter());
    }

    @Override
    public GenerationResult generate(AuthoringGenerationRequest request, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            Map<String, Double> direct = Map.of("sample.cylinder",
                    Boolean.parseBoolean(request.directParameters().getOrDefault("sample.cylinder", "true")) ? 1d : 0d,
                    "sample.scale", Double.parseDouble(request.directParameters().getOrDefault("sample.scale", "1.0")));
            Map<String, Double> values = ParameterPrecedence.resolve(descriptor(), direct,
                    semanticAdapter().orElseThrow().resolve(request.sourceSemantics(),
                            com.planeguardian.assets.generation.semantics.AssetSemanticAdapter.ResolutionContext.intrinsicOnly()),
                    request.explicitOverrides());
            double scale = values.get("sample.scale");
            if (!Double.isFinite(scale) || scale <= 0) throw new IllegalArgumentException("sample.scale must be positive");
            boolean cylinder = values.get("sample.cylinder") >= 0.5;
            SampleShapeProduct product = SampleShapeGenerator.generate(cylinder, scale, request.visualSeed());
            Node root = SampleShapeJmeAdapter.create(new DesktopAssetManager(true), product, GENERATOR_ID);
            Path output = outputDirectory.resolve(request.assetName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".j3o");
            SampleAssetExporter.write(root, output);
            return GenerationResult.success(output, request.assetName(), "Generated sample " + (cylinder ? "cylinder." : "box."),
                    GENERATOR_ID, product.fingerprint().hex());
        } catch (IOException | RuntimeException exception) {
            return GenerationResult.failure("Failed to generate sample shape: " + exception.getMessage());
        }
    }
}
