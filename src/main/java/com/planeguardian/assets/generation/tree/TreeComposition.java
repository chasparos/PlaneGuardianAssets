package com.planeguardian.assets.generation.tree;

import java.util.List;
import java.util.Objects;

/** Bounded, versioned controls for Great Tree components beyond its trunk. */
public record TreeComposition(
        int schemaVersion,
        List<TreeBranchLevel> branchLevels,
        TreeRootSettings roots,
        TreeLodSettings lod,
        int maximumComponents) {
    public TreeComposition {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException("Unsupported tree composition schema version");
        }
        branchLevels = List.copyOf(branchLevels);
        if (branchLevels.isEmpty() || branchLevels.size() > 3) {
            throw new IllegalArgumentException("Tree composition requires one to three branch levels");
        }
        Objects.requireNonNull(roots, "roots");
        Objects.requireNonNull(lod, "lod");
        if (maximumComponents < 2 || maximumComponents > 128) {
            throw new IllegalArgumentException("maximumComponents must be in [2, 128]");
        }
    }

    public static TreeComposition defaultsFor(TreeStructure structure) {
        return new TreeComposition(1,
                List.of(new TreeBranchLevel(6, 0.30, 0.82, 0.36, 0.09, 0.42, 8, 8)),
                new TreeRootSettings(5, 1.55, 0.30, 0.45, 8, 8),
                new TreeLodSettings(0, 1),
                32);
    }
}
