package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.assetgenerator.crystal.CrystalAssetGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CrystalAssetGeneratorTest {
    @Test
    void registryDiscoversCrystalProvider() {
        AuthoringGeneratorRegistry registry = new AuthoringGeneratorRegistry(Thread.currentThread().getContextClassLoader());
        assertTrue(registry.providers().stream().anyMatch(provider -> provider.descriptor().generatorId().equals(new StableId(CrystalAssetGenerator.GENERATOR_ID))));
    }

    @Test
    void editorSupportsCrystalEnumAndStringParameters() throws Exception {
        GeneratorParameterEditor[] holder = new GeneratorParameterEditor[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> holder[0] = new GeneratorParameterEditor(new CrystalAssetGenerator()));

        AuthoringGenerationRequest request = holder[0].snapshot();
        assertEquals("NATURAL_ROCK", request.directParameters().get("crystal.setting-kind"));
        assertEquals("palette.gem.quartz-clear", request.directParameters().get("crystal.palette-entry-id"));
    }
}
