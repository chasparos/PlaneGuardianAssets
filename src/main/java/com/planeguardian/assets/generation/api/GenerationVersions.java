package com.planeguardian.assets.generation.api;

import java.util.Objects;

/** Versions which participate in generation compatibility and fingerprints. */
public record GenerationVersions(
        ContractVersion requestSchema,
        ContractVersion semanticSchema,
        ContractVersion resolver,
        ContractVersion familyAdapter,
        ContractVersion geometryGenerator,
        ContractVersion materialContract) {

    public GenerationVersions {
        Objects.requireNonNull(requestSchema, "requestSchema");
        Objects.requireNonNull(semanticSchema, "semanticSchema");
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(familyAdapter, "familyAdapter");
        Objects.requireNonNull(geometryGenerator, "geometryGenerator");
        Objects.requireNonNull(materialContract, "materialContract");
    }
}
