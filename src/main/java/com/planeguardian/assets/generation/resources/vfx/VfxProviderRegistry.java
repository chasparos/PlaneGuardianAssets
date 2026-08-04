package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.StableId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.TreeMap;

/** Deterministic registry over trusted classpath providers discovered with ServiceLoader. */
public final class VfxProviderRegistry {
    private final NavigableMap<StableId, VfxProvider> providers;

    private VfxProviderRegistry(Collection<? extends VfxProvider> providers) {
        TreeMap<StableId, VfxProvider> ordered = new TreeMap<>();
        for (VfxProvider provider : providers) {
            Objects.requireNonNull(provider, "provider");
            VfxProvider previous = ordered.putIfAbsent(provider.providerId(), provider);
            if (previous != null) throw new IllegalArgumentException("Duplicate VFX provider ID: " + provider.providerId());
        }
        this.providers = Collections.unmodifiableNavigableMap(ordered);
    }

    public static VfxProviderRegistry of(Collection<? extends VfxProvider> providers) {
        return new VfxProviderRegistry(List.copyOf(providers));
    }

    public static VfxProviderRegistry discover(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");
        List<VfxProvider> discovered = ServiceLoader.load(VfxProvider.class, classLoader)
                .stream().map(ServiceLoader.Provider::get).toList();
        return new VfxProviderRegistry(discovered);
    }

    public NavigableMap<StableId, VfxProvider> providers() {
        return providers;
    }

    public VfxProvider require(StableId providerId, StableId pluginId) {
        VfxProvider provider = providers.get(providerId);
        if (provider == null) throw new IllegalArgumentException("Unknown VFX provider: " + providerId);
        if (!provider.supports(pluginId)) {
            throw new IllegalArgumentException("Provider " + providerId + " does not support plugin " + pluginId);
        }
        return provider;
    }
}
