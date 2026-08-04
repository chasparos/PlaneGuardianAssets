package com.planeguardian.assets.generation.api;

/** UI-, persistence-, and engine-neutral generator service contract. */
public interface ProceduralAssetProvider {
    StableId providerId();

    ContractVersion apiVersion();

    boolean supports(StableId generatorId);

    GeneratedAsset generate(GenerationRequest request);
}
