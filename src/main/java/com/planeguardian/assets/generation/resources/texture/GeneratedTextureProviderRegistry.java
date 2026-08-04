package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.StableId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.TreeMap;

/** Deterministic registry over trusted classpath texture providers. */
public final class GeneratedTextureProviderRegistry {
    private final NavigableMap<StableId, GeneratedTextureProvider> providers;

    private GeneratedTextureProviderRegistry(Collection<? extends GeneratedTextureProvider> providers) {
        TreeMap<StableId, GeneratedTextureProvider> ordered = new TreeMap<>();
        for (GeneratedTextureProvider provider : providers) {
            Objects.requireNonNull(provider, "provider");
            GeneratedTextureProvider previous = ordered.putIfAbsent(provider.providerId(), provider);
            if (previous != null) throw new IllegalArgumentException("Duplicate texture provider ID: " + provider.providerId());
        }
        this.providers = Collections.unmodifiableNavigableMap(ordered);
    }

    public static GeneratedTextureProviderRegistry of(Collection<? extends GeneratedTextureProvider> providers) {
        return new GeneratedTextureProviderRegistry(List.copyOf(providers));
    }

    public static GeneratedTextureProviderRegistry discover(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");
        return new GeneratedTextureProviderRegistry(ServiceLoader.load(GeneratedTextureProvider.class, classLoader)
                .stream().map(ServiceLoader.Provider::get).toList());
    }

    public NavigableMap<StableId, GeneratedTextureProvider> providers() { return providers; }

    public GeneratedTextureProvider require(StableId providerId) {
        GeneratedTextureProvider provider = providers.get(providerId);
        if (provider == null) throw new IllegalArgumentException("Unknown texture provider: " + providerId);
        return provider;
    }
}
