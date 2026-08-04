package com.planeguardian.assets.generation.resources.texture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TexturePixelsTest {
    @Test
    void validatesPackingAndDefensivelyCopiesBytes() {
        byte[] source = new byte[]{1, 2, 3, 4};
        TexturePixels pixels = new TexturePixels(2, 1, TexturePixelFormat.RG8_UNORM, source);
        source[0] = 9;
        byte[] returned = pixels.bytes();
        returned[1] = 9;

        assertEquals(2, pixels.width());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, pixels.bytes());
        assertThrows(IllegalArgumentException.class, () ->
                new TexturePixels(2, 1, TexturePixelFormat.RGBA8_UNORM, new byte[4]));
    }
}
