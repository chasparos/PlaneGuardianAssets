package com.planeguardian.assets.generation.crystal;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.palette.LinearColor;
import com.planeguardian.assets.generation.palette.NamedColorPalette;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.material.MaterialRecipeFingerprints;
import com.planeguardian.assets.generation.resources.material.MaterialValue;
import java.util.Map;

/** Builds engine-neutral material recipes for crystal preview/runtime adapters. */
public final class CrystalMaterialRecipeFactory {
    private static final ContractVersion VERSION = new ContractVersion(1, 0);
    private CrystalMaterialRecipeFactory() { }

    public static MaterialRecipe create(CrystalParameters parameters, CrystalSemanticAssetProfile profile) {
        LinearColor base = profile.colorCharacter().paletteEntry().color();
        GeneratedResourceRef resource = new GeneratedResourceRef(new StableId("material.crystal." + parameters.paletteEntryId().value()), ResourceKind.MATERIAL, VERSION);
        Map<StableId, com.planeguardian.assets.generation.resources.material.MaterialValue> inputs = Map.of(
                new StableId("base-color"), numeric(base.r(), base.g(), base.b(), (float) profile.radianceCharacter().opacity()),
                new StableId("emissive"), numeric(
                        clamp(base.r() * (float) profile.radianceCharacter().emissionTintStrength()),
                        clamp(base.g() * (float) profile.radianceCharacter().emissionTintStrength()),
                        clamp(base.b() * (float) profile.radianceCharacter().emissionTintStrength())),
                new StableId("roughness"), numeric(0.08 + (1 - profile.colorCharacter().clarity()) * 0.45),
                new StableId("metallic"), numeric(0.02),
                new StableId("crystal-opacity"), numeric(profile.radianceCharacter().opacity()),
                new StableId("crystal-fresnel"), numeric(0.35 + profile.shapeCharacter().facetSharpness() * 0.45),
                new StableId("crystal-refraction"), numeric(0.08 + (1 - profile.colorCharacter().clarity()) * 0.18),
                new StableId("crystal-noise-scale"), numeric(3.0 + profile.complexityCharacter().facetIrregularity() * 5.0),
                new StableId("crystal-noise-strength"), numeric(0.06 + profile.shapeCharacter().facetSharpness() * 0.12),
                new StableId("crystal-glint-strength"), numeric(0.18 + profile.radianceCharacter().intensity() * 0.32),
                new StableId("crystal-glint-power"), numeric(3.0 + profile.shapeCharacter().facetSharpness() * 5.0),
                new StableId("emission-strength"), numeric(profile.radianceCharacter().intensity()));
        return new MaterialRecipe(resource, VERSION, new StableId("pbr.crystal"), inputs,
                MaterialRecipeFingerprints.identity(resource.resourceId(), resource.version(), VERSION, new StableId("pbr.crystal"), inputs));
    }

    private static MaterialValue.Numeric numeric(double... values) { return new MaterialValue.Numeric(java.util.Arrays.stream(values).boxed().toList()); }
    private static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
}
