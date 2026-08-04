package com.planeguardian.assets.generation.resources.texture;

import java.util.Arrays;
import java.util.Objects;

/** Immutable tightly packed pixel payload prior to codec/export handling. */
public final class TexturePixels {
    private final int width;
    private final int height;
    private final TexturePixelFormat format;
    private final byte[] bytes;

    public TexturePixels(int width, int height, TexturePixelFormat format, byte[] bytes) {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Pixel dimensions must be positive");
        this.format = Objects.requireNonNull(format, "format");
        Objects.requireNonNull(bytes, "bytes");
        long expected = (long) width * height * format.bytesPerPixel();
        if (expected > Integer.MAX_VALUE || bytes.length != expected) {
            throw new IllegalArgumentException("Pixel byte count does not match dimensions and format");
        }
        this.width = width;
        this.height = height;
        this.bytes = bytes.clone();
    }

    public int width() { return width; }
    public int height() { return height; }
    public TexturePixelFormat format() { return format; }
    public byte[] bytes() { return bytes.clone(); }

    @Override
    public boolean equals(Object other) {
        return other instanceof TexturePixels pixels && width == pixels.width && height == pixels.height
                && format == pixels.format && Arrays.equals(bytes, pixels.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, format, Arrays.hashCode(bytes));
    }
}
