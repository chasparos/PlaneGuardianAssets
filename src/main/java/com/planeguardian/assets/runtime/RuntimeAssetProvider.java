package com.planeguardian.assets.runtime;

/** Trusted compiled runtime provider, discovered only from the application classpath. */
public interface RuntimeAssetProvider {
    String providerId();

    int apiVersion();

    boolean supports(String generatorId);
}
