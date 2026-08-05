package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.api.StableId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CrystalAssetGeneratorTest {
    @Test
    void registryDiscoversCrystalProvider() {
        AuthoringGeneratorRegistry registry = new AuthoringGeneratorRegistry(Thread.currentThread().getContextClassLoader());
        assertTrue(registry.providers().stream().anyMatch(provider -> provider.descriptor().generatorId().equals(new StableId(CrystalAssetGenerator.GENERATOR_ID))));
    }
}
