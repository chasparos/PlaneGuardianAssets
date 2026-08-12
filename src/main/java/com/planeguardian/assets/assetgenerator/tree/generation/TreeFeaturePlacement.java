package com.planeguardian.assets.assetgenerator.tree.generation;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Vector3;

import java.util.Objects;

/** One semantic feature admission and engine-neutral placement anchor. */
public record TreeFeaturePlacement(StableId id, TreeFeatureGroup group, Vector3 anchor) {
    public TreeFeaturePlacement {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(anchor, "anchor");
    }
}
