package com.planeguardian.assets.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/** Resolves a compatible trusted provider or a package-local fallback without dynamic code loading. */
public final class RuntimePackageResolver {
    private final PackageCompatibility runtimeCompatibility;
    private final List<RuntimeAssetProvider> providers;

    public RuntimePackageResolver(PackageCompatibility runtimeCompatibility) {
        this(runtimeCompatibility, ServiceLoader.load(RuntimeAssetProvider.class).stream()
                .map(ServiceLoader.Provider::get).toList());
    }

    public RuntimePackageResolver(PackageCompatibility runtimeCompatibility, List<RuntimeAssetProvider> providers) {
        this.runtimeCompatibility = Objects.requireNonNull(runtimeCompatibility, "runtimeCompatibility");
        this.providers = List.copyOf(providers);
        if (this.providers.stream().anyMatch(provider -> provider.apiVersion() != runtimeCompatibility.providerApiVersion())) {
            throw new IllegalArgumentException("Discovered provider API version is incompatible with runtime");
        }
        long distinct = this.providers.stream().map(RuntimeAssetProvider::providerId).distinct().count();
        if (distinct != this.providers.size()) throw new IllegalArgumentException("Duplicate runtime provider ID");
    }

    public Resolution resolve(String generatorId, PackageCompatibility packageCompatibility, String fallbackGltf) {
        if (!runtimeCompatibility.isCompatibleWith(packageCompatibility)) {
            throw new IllegalArgumentException("Package compatibility does not match runtime");
        }
        RuntimeAssetProvider provider = providers.stream()
                .filter(candidate -> candidate.supports(generatorId))
                .findFirst().orElse(null);
        if (provider != null) return new Resolution(provider, null);
        return new Resolution(null, safeRelativePath(fallbackGltf));
    }

    private static String safeRelativePath(String path) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("A fallback asset is required");
        Path normalized = Path.of(path).normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..") || path.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Fallback asset path must be a safe forward-slash relative path");
        }
        return normalized.toString().replace('\\', '/');
    }

    public record Resolution(RuntimeAssetProvider provider, String fallbackGltf) {
        public Resolution {
            if ((provider == null) == (fallbackGltf == null)) {
                throw new IllegalArgumentException("Resolution must have exactly one provider or fallback");
            }
        }

        public boolean usesFallback() {
            return fallbackGltf != null;
        }
    }
}
