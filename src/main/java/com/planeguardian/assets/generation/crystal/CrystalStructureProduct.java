package com.planeguardian.assets.generation.crystal;

import com.planeguardian.assets.generation.api.GeneratedSocket;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Engine-neutral structural crystal output before renderer adaptation. */
public record CrystalStructureProduct(Map<StableId, CrystalStructuralPart> parts, List<GeneratedSocket> sockets,
                                      ReproducibilityFingerprint fingerprint) {
    public CrystalStructureProduct {
        parts = Map.copyOf(parts);
        sockets = List.copyOf(sockets);
        Objects.requireNonNull(fingerprint, "fingerprint");
    }
}
