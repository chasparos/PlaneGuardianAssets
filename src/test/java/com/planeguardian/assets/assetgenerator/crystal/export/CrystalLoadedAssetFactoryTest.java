package com.planeguardian.assets.assetgenerator.crystal.export;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.runtime.LoadedAsset;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CrystalLoadedAssetFactoryTest {
    @Test
    void runtimeSwapsPackagedCrystalVariantWithoutRegeneration() {
        Node root = new Node("crystal.preview");
        root.attachChild(new Node("crystal.variant.4"));
        root.attachChild(new Node("crystal.variant.6"));
        root.attachChild(new Node("crystal.variant.8"));
        var loaded = CrystalLoadedAssetFactory.wrap(new StableId("asset.crystal"), root);
        loaded.applySemantics(profile(10), LoadedAsset.SemanticContext.intrinsicOnly());
        assertEquals(Spatial.CullHint.Always, root.getChild("crystal.variant.4").getCullHint());
        assertNotEquals(Spatial.CullHint.Always, root.getChild("crystal.variant.8").getCullHint());
        loaded.applySemantics(profile(4), LoadedAsset.SemanticContext.intrinsicOnly());
        assertNotEquals(Spatial.CullHint.Always, root.getChild("crystal.variant.4").getCullHint());
    }

    private static ResolvedVisualProfile profile(double sides) {
        return new ResolvedVisualProfile(new ContractVersion(1, 0),
                new TreeMap<>(Map.of("crystal.facet-count", sides)), List.of(),
                new com.planeguardian.assets.generation.api.ReproducibilityFingerprint(
                        fingerprintBytes(sides)));
    }

    private static byte[] fingerprintBytes(double sides) {
        byte[] bytes = new byte[32];
        bytes[0] = (byte) sides;
        return bytes;
    }
}
