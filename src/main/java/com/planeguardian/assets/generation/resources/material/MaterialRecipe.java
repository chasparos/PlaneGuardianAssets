package com.planeguardian.assets.generation.resources.material;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Immutable engine-neutral material recipe referencing reusable resource entities. */
public record MaterialRecipe(
        GeneratedResourceRef resource,
        ContractVersion parameterSchemaVersion,
        StableId materialModel,
        Map<StableId, MaterialValue> inputs,
        ReproducibilityFingerprint fingerprint) {
    public MaterialRecipe {
        Objects.requireNonNull(resource, "resource");
        if (resource.kind() != ResourceKind.MATERIAL) throw new IllegalArgumentException("Material recipe resource kind must be MATERIAL");
        Objects.requireNonNull(parameterSchemaVersion, "parameterSchemaVersion");
        Objects.requireNonNull(materialModel, "materialModel");
        TreeMap<StableId, MaterialValue> ordered = new TreeMap<>();
        ordered.putAll(inputs);
        if (ordered.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
            throw new IllegalArgumentException("Material input IDs and values must not be null");
        }
        inputs = Collections.unmodifiableMap(ordered);
        Objects.requireNonNull(fingerprint, "fingerprint");
        ReproducibilityFingerprint expected = MaterialRecipeFingerprints.identity(
                resource.resourceId(), resource.version(), parameterSchemaVersion, materialModel, inputs);
        if (!fingerprint.equals(expected)) throw new IllegalArgumentException("Material fingerprint does not match recipe inputs");
    }
}
