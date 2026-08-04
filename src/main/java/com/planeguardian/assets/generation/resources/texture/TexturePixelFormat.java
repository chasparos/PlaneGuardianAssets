package com.planeguardian.assets.generation.resources.texture;

public enum TexturePixelFormat {
    R8_UNORM(1),
    RG8_UNORM(2),
    RGB8_UNORM(3),
    RGBA8_UNORM(4);

    private final int bytesPerPixel;

    TexturePixelFormat(int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
    }

    public int bytesPerPixel() {
        return bytesPerPixel;
    }
}
