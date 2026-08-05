package com.planeguardian.assets.tools.generator;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.renderer.queue.RenderQueue;
import com.planeguardian.assets.generation.adapters.jme.CrystalMaterialAdapter;
import com.planeguardian.assets.generation.adapters.jme.JmeMeshAdapter;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import com.planeguardian.assets.generation.api.RenderTier;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.crystal.*;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Registered authoring provider for semantically-aware crystals. */
public final class CrystalAssetGenerator implements AuthoringGeneratorProvider {
    public static final String GENERATOR_ID = "pg.gem.crystal/1";
    private static final StableId FAMILY = new StableId("asset-family.gem.crystal");

    @Override public GeneratorDescriptor descriptor() {
        List<GeneratorDescriptor.Parameter> parameters = List.of(
                parameter("crystal.base-radius", "Base Radius", "number", "0.35", "[0.05,4]", false),
                parameter("crystal.tip-taper", "Tip Taper", "number", "0.18", "[0.05,1]", false),
                parameter("crystal.facet-count", "Facet Count", "integer", "6", "[4,16]", false),
                parameter("crystal.facet-rows", "Facet Rows", "integer", "5", "[2,16]", true),
                parameter("crystal.cluster-members", "Cluster Members", "integer", "1", "[1,4]", false),
                parameter("crystal.size-scale", "Size Scale", "number", "1.0", "[0.25,4]", false),
                parameter("crystal.cut-style", "Cut Style", "enum", "PRISM", "PRISM|CUSHION|BRILLIANT", false),
                parameter("crystal.setting-kind", "Setting Kind", "enum", "NATURAL_ROCK", "NATURAL_ROCK|LEVITATION", false),
                parameter("crystal.palette-entry-id", "Palette Entry", "string", "palette.gem.quartz-clear", "stable-id", true),
                parameter("crystal.hue-override-degrees", "Hue Override Degrees", "number", "0.0", "[-180,180]", true),
                parameter("crystal.opacity", "Opacity", "number", "0.6", "[0,1]", true),
                parameter("crystal.emission-strength", "Emission Strength", "number", "0.2", "[0,1]", true));
        SortedMap<String,String> defaults = new TreeMap<>();
        for (var parameter : parameters) defaults.put(parameter.id().value(), parameter.defaultValue());
        return new GeneratorDescriptor(new StableId(GENERATOR_ID), new ContractVersion(1,0), FAMILY, "Semantically Aware Crystal", parameters,
                List.of(new GeneratorDescriptor.Preset(new StableId("preset.crystal.default"), "Quartz Cluster", defaults)),
                Set.of(), Set.of("j3o"), Set.of("j3o"),
                Set.of(CrystalGeometryGenerator.CRYSTAL_ROLE, CrystalGeometryGenerator.HOST_ROLE),
                Set.of(CrystalGeometryGenerator.BASE_SOCKET),
                Set.of(new StableId("crystal.base-radius"), new StableId("crystal.tip-taper"), new StableId("crystal.facet-count"), new StableId("crystal.facet-rows"), new StableId("crystal.cluster-members"), new StableId("crystal.size-scale"), new StableId("crystal.opacity"), new StableId("crystal.emission-strength")));
    }

    @Override public Optional<com.planeguardian.assets.generation.semantics.AssetSemanticAdapter> semanticAdapter() { return Optional.of(new CrystalSemanticAdapter()); }

    @Override public GenerationResult generate(AuthoringGenerationRequest request, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            Map<String, Double> values = new TreeMap<>();
            for (var parameter : descriptor().parameters()) {
                if ("enum".equals(parameter.valueType()) || "string".equals(parameter.valueType())) continue;
                String raw = request.directParameters().getOrDefault(parameter.id().value(), parameter.defaultValue());
                values.put(parameter.id().value(), Double.parseDouble(raw));
            }
            var resolved = semanticAdapter().orElseThrow().resolve(request.sourceSemantics(), com.planeguardian.assets.generation.semantics.AssetSemanticAdapter.ResolutionContext.intrinsicOnly());
            values = com.planeguardian.assets.generation.authoring.ParameterPrecedence.resolve(descriptor(), values, resolved, request.explicitOverrides());
            CrystalParameters parameters = parameters(request, values);
            CrystalSemanticAssetProfile profile = ((CrystalSemanticAdapter) semanticAdapter().orElseThrow()).profile(request.sourceSemantics());
            CrystalStructureProduct structure = CrystalGeometryGenerator.generate(parameters, request.visualSeed());
            MaterialRecipe crystalMaterial = CrystalMaterialRecipeFactory.create(parameters, profile);
            Node root = new Node("crystal.preview");
            var assets = new DesktopAssetManager(true);
            for (int sides : new int[]{4, 6, 8}) {
                CrystalParameters variantParameters = withFacetCount(parameters, sides);
                Node variant = new Node("crystal.variant." + sides);
                CrystalGeometryGenerator.generate(variantParameters, request.visualSeed()).parts().values().stream()
                        .filter(part -> !part.hostContact()).forEach(part -> {
                            Geometry geometry = new Geometry(part.id().value(), JmeMeshAdapter.convert(part.renderMesh()));
                            geometry.setMaterial(CrystalMaterialAdapter.create(assets, crystalMaterial, 0.5));
                            geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                            variant.attachChild(geometry);
                        });
                variant.setCullHint(sides == selectedSides(parameters.facetCount()) ? com.jme3.scene.Spatial.CullHint.Inherit
                        : com.jme3.scene.Spatial.CullHint.Always);
                root.attachChild(variant);
            }
            CrystalGeometryGenerator.generate(parameters, request.visualSeed()).parts().values().stream()
                    .filter(CrystalStructuralPart::hostContact).forEach(part -> {
                        Geometry geometry = new Geometry(part.id().value(), JmeMeshAdapter.convert(part.renderMesh()));
                        geometry.setMaterial(CrystalMaterialAdapter.create(assets, crystalMaterial, 0.5));
                        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                        root.attachChild(geometry);
                    });
            structure.sockets().forEach(socket -> { Node n = new Node(socket.socketId().value()); n.setUserData("pg.socketId", socket.socketId().value()); root.attachChild(n); });
            root.setUserData("pg.generatorId", GENERATOR_ID);
            Path output = outputDirectory.resolve(request.assetName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".j3o");
            BinaryExporter.getInstance().save(root, output.toFile());
            return GenerationResult.success(output, request.assetName(), "Generated semantically aware crystal.", GENERATOR_ID, structure.fingerprint().hex());
        } catch (IOException | RuntimeException exception) {
            return GenerationResult.failure("Failed to generate crystal: " + exception.getMessage());
        }
    }

    private static CrystalParameters withFacetCount(CrystalParameters parameters, int count) {
            return new CrystalParameters(parameters.baseRadius(), parameters.tipTaper(), count, parameters.facetRows(),
                    parameters.clusterMemberCount(), parameters.sizeScale(), parameters.cutStyle(), parameters.settingKind(),
                    parameters.paletteEntryId(), parameters.hueOverrideDegrees());
        }

    private static int selectedSides(int requested) {
            return requested <= 5 ? 4 : requested <= 7 ? 6 : 8;
    }

    private static CrystalParameters parameters(AuthoringGenerationRequest request, Map<String, Double> values) {
        String setting = request.directParameters().getOrDefault("crystal.setting-kind", "NATURAL_ROCK");
        String palette = request.directParameters().getOrDefault("crystal.palette-entry-id", "palette.gem.quartz-clear");
        double hueOverride = Double.parseDouble(request.directParameters().getOrDefault("crystal.hue-override-degrees", "0.0"));
        String cutStyle = request.directParameters().getOrDefault("crystal.cut-style", "PRISM");
        return new CrystalParameters(values.get("crystal.base-radius"), values.get("crystal.tip-taper"),
                values.get("crystal.facet-count").intValue(), values.get("crystal.facet-rows").intValue(),
                values.get("crystal.cluster-members").intValue(), values.get("crystal.size-scale"),
                CrystalParameters.CutStyle.valueOf(cutStyle), CrystalParameters.SettingKind.valueOf(setting),
                new StableId(palette), Math.abs(hueOverride) < 1e-9 ? Optional.empty() : Optional.of(hueOverride));
    }

    private static GeneratorDescriptor.Parameter parameter(String id, String name, String type, String defaultValue, String allowed, boolean advanced) {
        return new GeneratorDescriptor.Parameter(new StableId(id), name, type, defaultValue, allowed, advanced);
    }
}
