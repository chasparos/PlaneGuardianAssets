package com.planeguardian.assets.assetgenerator.tree.export;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.assetgenerator.tree.generation.RuntimeWeatherInput;
import com.planeguardian.assets.assetgenerator.tree.generation.TreePreviewFixture;
import com.planeguardian.assets.generation.semantics.AssetSemanticAdapter;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import com.planeguardian.assets.assetgenerator.tree.generation.DeciduousTreeStructureGenerator;
import com.planeguardian.assets.assetgenerator.tree.semantics.GreatTreeSemanticAdapter;
import com.planeguardian.assets.assetgenerator.tree.generation.TreePresentationSettings;
import com.planeguardian.assets.assetgenerator.tree.generation.TreeStructure;
import com.planeguardian.assets.runtime.EnvironmentState;
import com.planeguardian.assets.runtime.LoadedAsset;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeLoadedAssetFactoryTest {
    @Test
    void repeatedSemanticsAreIdempotentAndEnvironmentUpdatesExistingMaterials() {
        TreeStructure structure = TreeStructure.defaults();
        var structural = DeciduousTreeStructureGenerator.generate(structure, structure.composition(), 42);
        var crown = DeciduousTreeStructureGenerator.generateCrown(structure, structure.composition(), 42);
        Node root = TreePreviewJmeAdapter.create(new DesktopAssetManager(true), structural, crown,
                TreePreviewFixture.gameplay(), TreePresentationSettings.defaults(), RuntimeWeatherInput.calm(), 42).root();
        LoadedAsset<Node> loaded = TreeLoadedAssetFactory.wrap(new StableId("asset.tree.test"), root);
        var resolved = new GreatTreeSemanticAdapter().resolve(lifeProfile(), AssetSemanticAdapter.ResolutionContext.intrinsicOnly());
        int children = root.getQuantity();

        assertTrue(loaded.applySemantics(resolved, LoadedAsset.SemanticContext.intrinsicOnly()).changed());
        assertFalse(loaded.applySemantics(resolved, LoadedAsset.SemanticContext.intrinsicOnly()).changed());
        assertEquals(children, root.getQuantity());
        assertEquals(resolved.fingerprint().hex(), root.getUserData("pg.semanticFingerprint"));

        EnvironmentState environment = new EnvironmentState(12, new Vector3(1, 0, 0), .8, new TreeMap<>());
        loaded.update(.016, environment);
        Geometry geometry = (Geometry) root.getChildren().stream().filter(Geometry.class::isInstance).findFirst().orElseThrow();
        assertNull(geometry.getMaterial().getParam("WindDirection"));
    }

    private static SemanticProfile lifeProfile() {
        TreeMap<StableId, SemanticWheelValue> wheels = new TreeMap<>();
        wheels.put(new StableId("lore.ethos"), new SemanticWheelValue(1, 0, 1,
                Optional.empty(), Optional.empty(), List.of()));
        return new SemanticProfile(new ContractVersion(1, 0), wheels);
    }
}
