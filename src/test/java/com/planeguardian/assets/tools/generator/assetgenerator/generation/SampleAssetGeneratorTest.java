package com.planeguardian.assets.tools.generator.assetgenerator.generation;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import com.planeguardian.assets.tools.generator.AuthoringGenerationRequest;
import com.planeguardian.assets.tools.generator.AuthoringGeneratorRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleAssetGeneratorTest {
    @TempDir Path output;

    @Test
    void serviceLoaderDiscoversSampleGenerator() {
        assertTrue(new AuthoringGeneratorRegistry().providers().stream()
                .anyMatch(provider -> provider.descriptor().generatorId().equals(new StableId(SampleAssetGenerator.GENERATOR_ID))));
    }

    @Test
    void qualitySelectsBoxOrCylinderAndScaleIsDirect() throws Exception {
        SampleAssetGenerator generator = new SampleAssetGenerator();
        assertEquals("sample.scale", generator.descriptor().parameters().get(1).id().value());
        AuthoringGenerationRequest request = request("sample", -1, 2.5);
        var result = generator.generate(request, output);
        assertTrue(result.success(), result.message());
        assertTrue(Files.isRegularFile(result.outputPath()));
        assertTrue(result.message().contains("box"));
        var cylinder = generator.generate(request("cylinder", 1, 1.25), output);
        assertTrue(cylinder.success(), cylinder.message());
        assertTrue(cylinder.message().contains("cylinder"));
    }

    private static AuthoringGenerationRequest request(String name, double qualityX, double scale) {
        TreeMap<StableId, SemanticWheelValue> wheels = new TreeMap<>();
        wheels.put(new StableId("game.quality"),
                new SemanticWheelValue(qualityX, 0, 1, Optional.empty(), Optional.empty(), java.util.List.of()));
        TreeMap<String, String> parameters = new TreeMap<>();
        parameters.put("sample.cylinder", "true");
        parameters.put("sample.scale", Double.toString(scale));
        return new AuthoringGenerationRequest(name, 7, parameters,
                new SemanticProfile(new ContractVersion(1, 0), wheels), Set.of());
    }
}
