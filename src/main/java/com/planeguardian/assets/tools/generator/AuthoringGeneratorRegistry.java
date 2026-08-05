package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.api.StableId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/** Deterministic registry of trusted classpath authoring providers. */
public final class AuthoringGeneratorRegistry {
    private final List<AuthoringGeneratorProvider> providers;

    public AuthoringGeneratorRegistry() { this(Thread.currentThread().getContextClassLoader()); }
    public AuthoringGeneratorRegistry(ClassLoader loader) {
        this(ServiceLoader.load(AuthoringGeneratorProvider.class, loader).stream()
                .map(ServiceLoader.Provider::get).toList());
    }
    public AuthoringGeneratorRegistry(List<AuthoringGeneratorProvider> providers) {
        this.providers = providers.stream().map(provider -> Objects.requireNonNull(provider, "provider"))
                .sorted(Comparator.comparing(provider -> provider.descriptor().generatorId())).toList();
        long unique = this.providers.stream().map(provider -> provider.descriptor().generatorId()).distinct().count();
        if (unique != this.providers.size()) throw new IllegalArgumentException("Duplicate authoring generator ID");
    }
    public List<AuthoringGeneratorProvider> providers() { return providers; }
    public AuthoringGeneratorProvider require(StableId id) {
        return providers.stream().filter(provider -> provider.descriptor().generatorId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown authoring generator: " + id));
    }
}
