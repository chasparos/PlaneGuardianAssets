package com.planeguardian.assets.generation.api;

/** Explicit compatibility version for a generation contract or algorithm. */
public record ContractVersion(int major, int minor) {
    public ContractVersion {
        if (major < 0 || minor < 0) {
            throw new IllegalArgumentException("Version components must be non-negative");
        }
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }
}
