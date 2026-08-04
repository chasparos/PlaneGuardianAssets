package com.planeguardian.assets.generation.resources.material;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;

import java.util.Map;

public final class MaterialRecipeFingerprints {
    private MaterialRecipeFingerprints() {
    }

    public static ReproducibilityFingerprint identity(
            StableId recipeId, ContractVersion recipeVersion, ContractVersion schemaVersion,
            StableId materialModel, Map<StableId, MaterialValue> inputs) {
        FingerprintBuilder fingerprint = new FingerprintBuilder()
                .addString("material-recipe/1").addId(recipeId)
                .addLong(recipeVersion.major()).addLong(recipeVersion.minor())
                .addLong(schemaVersion.major()).addLong(schemaVersion.minor()).addId(materialModel);
        inputs.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            fingerprint.addId(entry.getKey());
            MaterialValue value = entry.getValue();
            if (value instanceof MaterialValue.Numeric numeric) {
                fingerprint.addString("numeric").addLong(numeric.components().size());
                numeric.components().forEach(component -> fingerprint.addLong(Double.doubleToLongBits(component)));
            } else if (value instanceof MaterialValue.Flag flag) {
                fingerprint.addString("flag").addLong(flag.value() ? 1 : 0);
            } else if (value instanceof MaterialValue.Texture texture) {
                fingerprint.addString("texture").addId(texture.resource().resourceId())
                        .addLong(texture.resource().version().major()).addLong(texture.resource().version().minor())
                        .addString(texture.fingerprint().hex());
            } else {
                throw new IllegalStateException("Unknown material value: " + value.getClass());
            }
        });
        return fingerprint.build();
    }
}
