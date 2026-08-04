package com.planeguardian.assets.generation.resources;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceArtifactTest {
    @Test
    void acceptsPortableRelativeArtifactMetadata() {
        ResourceArtifact artifact = new ResourceArtifact(new StableId("artifact.bark.png"),
                resource(), "image/png", "textures/generated/bark.png", 128, fingerprint());
        assertEquals("textures/generated/bark.png", artifact.relativePath());
    }

    @Test
    void rejectsTraversalAbsoluteAndPlatformSpecificPaths() {
        assertThrows(IllegalArgumentException.class, () -> artifact("../bark.png"));
        assertThrows(IllegalArgumentException.class, () -> artifact("/textures/bark.png"));
        assertThrows(IllegalArgumentException.class, () -> artifact("C:/textures/bark.png"));
        assertThrows(IllegalArgumentException.class, () -> artifact("textures\\bark.png"));
    }

    private static ResourceArtifact artifact(String path) {
        return new ResourceArtifact(new StableId("artifact.bark.png"), resource(),
                "image/png", path, 1, fingerprint());
    }

    private static GeneratedResourceRef resource() {
        return new GeneratedResourceRef(new StableId("texture.bark"), ResourceKind.TEXTURE, new ContractVersion(1, 0));
    }

    private static ReproducibilityFingerprint fingerprint() {
        return new ReproducibilityFingerprint(new byte[32]);
    }
}
