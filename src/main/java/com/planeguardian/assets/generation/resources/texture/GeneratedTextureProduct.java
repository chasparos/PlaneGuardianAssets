package com.planeguardian.assets.generation.resources.texture;

import java.util.Objects;

/** Descriptor plus raw pixels before encoding or persistence. */
public record GeneratedTextureProduct(GeneratedTexture descriptor, TexturePixels pixels) {
    public GeneratedTextureProduct {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(pixels, "pixels");
        if (descriptor.request().width() != pixels.width() || descriptor.request().height() != pixels.height()) {
            throw new IllegalArgumentException("Texture descriptor and pixel dimensions must match");
        }
    }
}
