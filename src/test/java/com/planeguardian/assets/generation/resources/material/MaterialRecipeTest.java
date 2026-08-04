package com.planeguardian.assets.generation.resources.material;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaterialRecipeTest {
    private static final ContractVersion VERSION = new ContractVersion(1, 0);
    private static final StableId RECIPE = new StableId("material.oak-bark");
    private static final StableId MODEL = new StableId("material-model.pbr-metallic-roughness");

    @Test
    void canonicalFingerprintIgnoresMapInsertionOrder() {
        Map<StableId, MaterialValue> first = new LinkedHashMap<>();
        first.put(new StableId("roughness"), new MaterialValue.Numeric(List.of(0.8)));
        first.put(new StableId("albedo"), new MaterialValue.Texture(texture(), textureFingerprint(1)));
        Map<StableId, MaterialValue> second = new LinkedHashMap<>();
        second.put(new StableId("albedo"), new MaterialValue.Texture(texture(), textureFingerprint(1)));
        second.put(new StableId("roughness"), new MaterialValue.Numeric(List.of(0.8)));

        assertEquals(fingerprint(first), fingerprint(second));
        MaterialRecipe recipe = new MaterialRecipe(material(), VERSION, MODEL, first, fingerprint(first));
        assertEquals(List.copyOf(second.keySet()).stream().sorted().toList(), List.copyOf(recipe.inputs().keySet()));
    }

    @Test
    void typedChangesAffectIdentityAndInvalidReferencesAreRejected() {
        Map<StableId, MaterialValue> scalar = Map.of(new StableId("roughness"), new MaterialValue.Numeric(List.of(0.8)));
        Map<StableId, MaterialValue> flag = Map.of(new StableId("roughness"), new MaterialValue.Flag(true));
        assertNotEquals(fingerprint(scalar), fingerprint(flag));
        assertNotEquals(
                fingerprint(Map.of(new StableId("albedo"), new MaterialValue.Texture(texture(), textureFingerprint(1)))),
                fingerprint(Map.of(new StableId("albedo"), new MaterialValue.Texture(texture(), textureFingerprint(2)))));
        assertThrows(IllegalArgumentException.class, () -> new MaterialValue.Texture(material(), textureFingerprint(1)));
        assertThrows(IllegalArgumentException.class, () -> new MaterialRecipe(
                material(), VERSION, MODEL, scalar, new ReproducibilityFingerprint(new byte[32])));
    }

    private static ReproducibilityFingerprint fingerprint(Map<StableId, MaterialValue> inputs) {
        return MaterialRecipeFingerprints.identity(RECIPE, VERSION, VERSION, MODEL, inputs);
    }

    private static GeneratedResourceRef material() {
        return new GeneratedResourceRef(RECIPE, ResourceKind.MATERIAL, VERSION);
    }

    private static GeneratedResourceRef texture() {
        return new GeneratedResourceRef(new StableId("texture.oak-bark"), ResourceKind.TEXTURE, VERSION);
    }

    private static ReproducibilityFingerprint textureFingerprint(int marker) {
        byte[] bytes = new byte[32];
        bytes[0] = (byte) marker;
        return new ReproducibilityFingerprint(bytes);
    }
}
