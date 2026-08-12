package com.planeguardian.assets.assetgenerator.sample;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.authoring.ParameterPrecedence;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.vfx.VfxAttachment;
import com.planeguardian.assets.generation.resources.vfx.VfxGenerationRequest;
import com.planeguardian.assets.generation.resources.vfx.VfxValue;
import com.planeguardian.assets.tools.generator.AuthoringGenerationRequest;
import com.planeguardian.assets.tools.generator.AuthoringGeneratorProvider;
import com.planeguardian.assets.tools.generator.GenerationResult;
import com.planeguardian.assets.assetgenerator.sample.export.SampleAssetExporter;
import com.planeguardian.assets.assetgenerator.sample.export.SampleFireflySwarmJmeAdapter;
import com.planeguardian.assets.assetgenerator.sample.export.SampleShapeJmeAdapter;
import com.planeguardian.assets.assetgenerator.sample.generation.SampleShapeGenerator;
import com.planeguardian.assets.assetgenerator.sample.generation.SampleShapeProduct;
import com.planeguardian.assets.assetgenerator.sample.generation.material.SampleMaterialRecipeFactory;
import com.planeguardian.assets.assetgenerator.sample.generation.vfx.SampleFireflySwarmVfxProvider;
import com.planeguardian.assets.assetgenerator.sample.semantics.SampleSemanticAdapter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Minimal registered generator used to demonstrate package and semantic boundaries.
 *
 * <p>What: the smallest complete worked example of the standard authoring flow -
 * resolve parameters (direct/semantic/override precedence), build engine-neutral
 * geometry, wrap it in an engine-neutral material recipe, configure a bounded VFX
 * plugin (a small swarm of fireflies orbiting the shape), then realize all three
 * through jME adapters and export. Why: every other registered generator (Great
 * Tree, Crystal, ...) follows this same shape, so this class exists to be read
 * first, kept intentionally small, and never grown into a "real" asset.
 *
 * <p>Soft contracts this class must obey:
 * <ul>
 *   <li>{@link #generate} only orchestrates already-established, engine-neutral
 *       building blocks ({@code SampleShapeGenerator}, {@code SampleMaterialRecipeFactory},
 *       {@code SampleFireflySwarmVfxProvider}); it must not itself decide geometry,
 *       material, or particle values beyond passing resolved parameters through.</li>
 *   <li>jME types ({@code DesktopAssetManager}, {@code Node}, ...) only appear here at
 *       the export boundary, and only via the dedicated adapters - never inline.</li>
 *   <li>Every declared socket must be attached to before a VFX configuration
 *       references it, and every generation must remain deterministic for a given
 *       {@code (cylinder, scale, seed)} triple.</li>
 * </ul>
 */
public final class SampleAssetGenerator implements AuthoringGeneratorProvider {
    public static final String GENERATOR_ID = "pg.sample.shape/1";

    /** Stable socket identifying the shape's own center, used to anchor the firefly swarm. */
    private static final StableId CENTER_SOCKET = new StableId("sample.socket.center");

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
                Set.of(new StableId("sample.shape")), Set.of(CENTER_SOCKET),
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
            MaterialRecipe material = SampleMaterialRecipeFactory.create(cylinder);
            Node root = SampleShapeJmeAdapter.create(new DesktopAssetManager(true), product, material, GENERATOR_ID);
            root.attachChild(SampleFireflySwarmJmeAdapter.create(fireflySwarm(scale, request.visualSeed()),
                    List.of(new GeneratedSocket(CENTER_SOCKET, new StableId("sample.shape"), Transform.IDENTITY))));
            Path output = outputDirectory.resolve(request.assetName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".j3o");
            SampleAssetExporter.write(root, output);
            return GenerationResult.success(output, request.assetName(), "Generated sample " + (cylinder ? "cylinder." : "box."),
                    GENERATOR_ID, product.fingerprint().hex());
        } catch (IOException | RuntimeException exception) {
            return GenerationResult.failure("Failed to generate sample shape: " + exception.getMessage());
        }
    }

    /**
     * Configures the bounded firefly swarm around the shape. The swarm radius scales
     * with the shape's own {@code scale} so the fireflies always visibly orbit the
     * geometry rather than sitting inside it or drifting far away, regardless of how
     * large or small the sample shape is authored.
     */
    private static VfxGenerationRequest fireflySwarm(double scale, long seed) {
        return new VfxGenerationRequest(new StableId("vfx-config.sample.firefly-swarm"),
                SampleFireflySwarmVfxProvider.PLUGIN_ID, seed, Map.of(
                new StableId("color"), new VfxValue.Numeric(List.of(0.85, 0.95, 0.35, 1.0)),
                new StableId("count"), new VfxValue.Numeric(List.of(12.0)),
                new StableId("lifetime"), new VfxValue.Numeric(List.of(3.0)),
                new StableId("size"), new VfxValue.Numeric(List.of(0.03)),
                new StableId("radius"), new VfxValue.Numeric(List.of(Math.max(0.05, scale * 1.5)))),
                List.of(new VfxAttachment(CENTER_SOCKET, Transform.IDENTITY)));
    }
}
