package com.planeguardian.assets.generation.semantics;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.Contribution;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/** Quantized resolver output consumed by asset-family adapters. */
public record ResolvedVisualProfile(ContractVersion resolverVersion, SortedMap<String, Double> channels,
                                    List<Contribution> contributionTrace, ReproducibilityFingerprint fingerprint) {
    public ResolvedVisualProfile {
        Objects.requireNonNull(resolverVersion, "resolverVersion");
        TreeMap<String, Double> copy = new TreeMap<>();
        channels.forEach((key, value) -> {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("channel ID must not be blank");
            if (value == null || !Double.isFinite(value)) throw new IllegalArgumentException("channel must be finite");
            copy.put(key, value);
        });
        channels = Collections.unmodifiableSortedMap(copy);
        contributionTrace = List.copyOf(contributionTrace);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
