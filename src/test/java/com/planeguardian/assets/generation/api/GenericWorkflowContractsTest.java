package com.planeguardian.assets.generation.api;

import com.planeguardian.assets.generation.authoring.AuthoringSession;
import com.planeguardian.assets.generation.authoring.ParameterPrecedence;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import com.planeguardian.assets.runtime.EnvironmentReactive;
import com.planeguardian.assets.runtime.EnvironmentState;
import com.planeguardian.assets.runtime.LoadedAsset;
import com.planeguardian.assets.runtime.RuntimeCapability;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericWorkflowContractsTest {
    @Test
    void wheelKeepsExtremitySeparateFromSalience() {
        SemanticWheelValue centered = SemanticWheelValue.centered(1);
        SemanticWheelValue incidental = new SemanticWheelValue(1, 0, .1,
                Optional.empty(), Optional.empty(), List.of());
        assertEquals(0, centered.extremity());
        assertEquals(1, centered.salience());
        assertEquals(1, incidental.extremity());
        assertEquals(.1, incidental.salience());
        assertThrows(IllegalArgumentException.class, () -> new SemanticWheelValue(1, 1, .5,
                Optional.empty(), Optional.empty(), List.of()));
    }

    @Test
    void authoringSessionOwnsInputsAndRequiresProductForReadyPreview() {
        TreeMap<String, String> direct = new TreeMap<>(Map.of("height", "12"));
        AuthoringSession session = new AuthoringSession(descriptor(), 42, direct,
                new SemanticProfile(new ContractVersion(1, 0), new TreeMap<>()),
                Optional.empty(), Optional.empty(), AuthoringSession.PreviewState.DIRTY, List.of());
        direct.put("height", "99");
        assertEquals("12", session.directParameters().get("height"));
        assertThrows(IllegalArgumentException.class, () -> new AuthoringSession(descriptor(), 42,
                new TreeMap<>(), session.sourceSemantics(), Optional.empty(), Optional.empty(),
                AuthoringSession.PreviewState.READY, List.of()));
    }

    @Test
    void loadedAssetDispatchesEnvironmentOnlyToReactiveCapabilities() {
        RecordingEnvironmentCapability reactive = new RecordingEnvironmentCapability();
        LoadedAsset<String> asset = loaded(List.of(reactive, () -> new StableId("runtime.static")));
        EnvironmentState environment = new EnvironmentState(5, new Vector3(1, 0, 0), .7,
                new TreeMap<>(Map.of("rain", .2)));
        asset.update(.016, environment);
        assertEquals(1, reactive.updates.size());
        assertEquals(environment, reactive.updates.get(0));
        assertThrows(IllegalArgumentException.class, () -> asset.update(-1, environment));
    }

    @Test
    void initializeSemanticsAppliesIntrinsicProfileOnceAtInstanceCreation() {
        var applied = new ArrayList<LoadedAsset.SemanticContext>();
        LoadedAsset<String> asset = new LoadedAsset<>() {
            @Override public StableId assetId() { return new StableId("asset.test"); }
            @Override public String root() { return "root"; }
            @Override public Collection<RuntimeCapability> capabilities() { return List.of(); }
            @Override public SemanticApplicationResult applySemantics(ResolvedVisualProfile profile,
                    SemanticContext context) {
                applied.add(context);
                return new SemanticApplicationResult(true, List.of());
            }
        };
        ResolvedVisualProfile profile = new ResolvedVisualProfile(new ContractVersion(1, 0),
                new TreeMap<>(Map.of("visual.coverage", .5)), List.of(), new ReproducibilityFingerprint(new byte[32]));
        LoadedAsset.SemanticApplicationResult result = asset.initializeSemantics(profile);
        assertEquals(1, applied.size());
        assertEquals(LoadedAsset.SemanticContext.intrinsicOnly(), applied.get(0));
        assertEquals(true, result.changed());
        assertThrows(NullPointerException.class, () -> asset.initializeSemantics(null));
    }

    @Test
    void explicitOverrideWinsOnlyWhenDeliberatelyEnabled() {
        GeneratorDescriptor descriptor = new GeneratorDescriptor(new StableId("generator.test/1"),
                new ContractVersion(1, 0), new StableId("asset-family.test"), "Test",
                List.of(new GeneratorDescriptor.Parameter(new StableId("visual.coverage"), "Coverage", "decimal", ".5", "[0, 1]", false)),
                List.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(new StableId("visual.coverage")));
        ResolvedVisualProfile resolved = new ResolvedVisualProfile(new ContractVersion(1, 0),
                new TreeMap<>(Map.of("visual.coverage", .8)), List.of(), new ReproducibilityFingerprint(new byte[32]));
        assertEquals(.8, ParameterPrecedence.resolve(descriptor, Map.of("visual.coverage", .2), resolved, Set.of()).get("visual.coverage"));
        assertEquals(.2, ParameterPrecedence.resolve(descriptor, Map.of("visual.coverage", .2), resolved, Set.of("visual.coverage")).get("visual.coverage"));
    }

    private static GeneratorDescriptor descriptor() {
        return new GeneratorDescriptor(new StableId("generator.test/1"), new ContractVersion(1, 0),
                new StableId("asset-family.test"), "Test", List.of(), List.of(), Set.of(),
                Set.of("j3o"), Set.of("glb"), Set.of(), Set.of(), Set.of());
    }

    private static LoadedAsset<String> loaded(Collection<RuntimeCapability> capabilities) {
        return new LoadedAsset<>() {
            @Override public StableId assetId() { return new StableId("asset.test"); }
            @Override public String root() { return "root"; }
            @Override public Collection<RuntimeCapability> capabilities() { return capabilities; }
            @Override public SemanticApplicationResult applySemantics(ResolvedVisualProfile profile,
                    SemanticContext context) { return new SemanticApplicationResult(true, List.of()); }
        };
    }

    private static final class RecordingEnvironmentCapability implements EnvironmentReactive {
        private final List<EnvironmentState> updates = new ArrayList<>();
        @Override public StableId capabilityId() { return new StableId("runtime.environment"); }
        @Override public void update(double deltaSeconds, EnvironmentState environment) { updates.add(environment); }
    }
}
