package com.planeguardian.assets.model;

/**
 * Classifies an asset by its broad category.
 * More fine-grained metadata can be stored in Asset.metadata (JSON).
 */
public enum AssetType {
    MODEL,
    TEXTURE,
    SOUND,
    ANIMATION,
    MATERIAL,
    OTHER
}
