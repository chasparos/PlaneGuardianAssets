package com.planeguardian.assets.runtime;

import java.util.Objects;

/** Version agreement required between an exported asset package and its trusted runtime. */
public record PackageCompatibility(String indexSchema, int runtimeApiVersion, int providerApiVersion) {
    public PackageCompatibility {
        if (indexSchema == null || !indexSchema.matches("pg\\.asset-index/[1-9][0-9]*")) {
            throw new IllegalArgumentException("indexSchema must be a versioned pg.asset-index identifier");
        }
        if (runtimeApiVersion < 1 || providerApiVersion < 1) {
            throw new IllegalArgumentException("API versions must be positive");
        }
    }

    public boolean isCompatibleWith(PackageCompatibility runtime) {
        Objects.requireNonNull(runtime, "runtime");
        return indexSchema.equals(runtime.indexSchema)
                && runtimeApiVersion == runtime.runtimeApiVersion
                && providerApiVersion == runtime.providerApiVersion;
    }
}
