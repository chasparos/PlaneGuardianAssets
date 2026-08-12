package com.planeguardian.assets.assetgenerator.sample.generation.material;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.material.MaterialRecipeFingerprints;
import com.planeguardian.assets.generation.resources.material.MaterialValue;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds an engine-neutral {@link MaterialRecipe} for the sample shape.
 *
 * <p>What: a minimal, deterministic Blinn-Phong-style recipe (base color, specular
 * tint, shininess) that any render-boundary adapter can turn into a concrete engine
 * material. Why: the sample generator exists to demonstrate the standard
 * generate to recipe to adapter flow other generators follow, so it must use the same
 * shared {@code MaterialRecipe}/{@code MaterialValue} contracts as the Great Tree and
 * Crystal generators instead of inlining engine material state.
 *
 * <p>Soft contract: this factory must stay engine-neutral (no jME imports) and
 * deterministic - the same {@code cylinder} flag always yields the same recipe, and
 * the recipe's fingerprint is derived solely from its declared inputs via
 * {@link MaterialRecipeFingerprints}. Only the render-boundary adapter
 * ({@code SampleMaterialJmeAdapter}) is allowed to translate these inputs into a
 * concrete jME {@code Material}.
 */
public final class SampleMaterialRecipeFactory {
    private static final ContractVersion VERSION = new ContractVersion(1, 0);

    private SampleMaterialRecipeFactory() {
    }

    public static MaterialRecipe create(boolean cylinder) {
        GeneratedResourceRef resource = new GeneratedResourceRef(
                new StableId("material.sample." + (cylinder ? "cylinder" : "box")), ResourceKind.MATERIAL, VERSION);
        // The two shapes get distinct, but equally simple, tints so the material is
        // visibly wired up without pretending to be a physically authored asset.
        Map<StableId, MaterialValue> inputs = cylinder
                ? inputs(0.80, 0.65, 0.35, 0.55, 0.55, 0.55, 24)
                : inputs(0.55, 0.60, 0.70, 0.55, 0.55, 0.55, 16);
        return new MaterialRecipe(resource, VERSION, new StableId("blinn-phong.sample"), inputs,
                MaterialRecipeFingerprints.identity(resource.resourceId(), resource.version(), VERSION,
                        new StableId("blinn-phong.sample"), inputs));
    }

    private static Map<StableId, MaterialValue> inputs(double r, double g, double b,
                                                         double specularR, double specularG, double specularB,
                                                         double shininess) {
        return Map.of(
                new StableId("base-color"), numeric(r, g, b, 1),
                new StableId("specular-color"), numeric(specularR, specularG, specularB, 1),
                new StableId("shininess"), numeric(shininess));
    }

    private static MaterialValue.Numeric numeric(double... values) {
        return new MaterialValue.Numeric(Arrays.stream(values).boxed().toList());
    }
}
