package com.planeguardian.assets.generation.preview;

import com.planeguardian.assets.generation.api.RenderTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreePreviewFixtureTest {
    @Test
    void gameplayFixtureIsFixedAndShadowed() {
        TreePreviewFixture fixture = TreePreviewFixture.gameplay();

        assertEquals(RenderTier.GAMEPLAY, fixture.renderTier());
        assertTrue(fixture.shadowsEnabled());
        assertEquals(.45, fixture.ambientIntensity());
        assertEquals(15, fixture.cameraPosition().x());
    }
}
