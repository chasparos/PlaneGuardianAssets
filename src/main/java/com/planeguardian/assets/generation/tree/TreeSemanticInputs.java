package com.planeguardian.assets.generation.tree;

import java.util.Objects;

/** Separates native tree semantics from bounded host-land influence. */
public record TreeSemanticInputs(TreeSemanticProfile intrinsic, TreeSemanticProfile host, double hostInfluence) {
    public TreeSemanticInputs {
        Objects.requireNonNull(intrinsic, "intrinsic");
        Objects.requireNonNull(host, "host");
        if (!Double.isFinite(hostInfluence) || hostInfluence < 0 || hostInfluence > 1) {
            throw new IllegalArgumentException("hostInfluence must be in [0, 1]");
        }
    }

    public static TreeSemanticInputs intrinsicOnly(TreeSemanticProfile profile) {
        return new TreeSemanticInputs(profile, TreeSemanticProfile.ordinary(), 0);
    }
}
