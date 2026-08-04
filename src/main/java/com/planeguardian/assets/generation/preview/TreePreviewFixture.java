package com.planeguardian.assets.generation.preview;

import com.planeguardian.assets.generation.api.RenderTier;
import com.planeguardian.assets.generation.api.Vector3;

import java.util.Objects;

/** Fixed gameplay-view validation fixture; renderer adapters consume but do not alter it. */
public record TreePreviewFixture(RenderTier renderTier, Vector3 cameraPosition, Vector3 cameraTarget,
                                 Vector3 lightDirection, double ambientIntensity, boolean shadowsEnabled) {
    public TreePreviewFixture {
        Objects.requireNonNull(renderTier, "renderTier");
        Objects.requireNonNull(cameraPosition, "cameraPosition");
        Objects.requireNonNull(cameraTarget, "cameraTarget");
        Objects.requireNonNull(lightDirection, "lightDirection");
        if (!Double.isFinite(ambientIntensity) || ambientIntensity < 0 || ambientIntensity > 1) {
            throw new IllegalArgumentException("ambientIntensity must be in [0, 1]");
        }
    }

    public static TreePreviewFixture gameplay() {
        return new TreePreviewFixture(RenderTier.GAMEPLAY, new Vector3(15, 9, 18), new Vector3(0, 6, 0),
                new Vector3(-.5, -1, -.5), .45, true);
    }
}
