package com.planeguardian.assets.generation.resources.texture;

import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.ResourceArtifact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

/** Deterministic, dependency-free PNG encoder for the supported 8-bit pixel formats. */
public final class PngTextureEncoder implements TextureEncoder {
    private static final byte[] SIGNATURE = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};

    @Override
    public EncodedTextureArtifact encode(GeneratedTextureProduct product, StableId artifactId, String relativePath) {
        byte[] bytes = encode(product.pixels());
        return new EncodedTextureArtifact(new ResourceArtifact(artifactId, product.descriptor().resource(),
                "image/png", relativePath, bytes.length, contentFingerprint(bytes)), bytes);
    }

    public byte[] encode(TexturePixels pixels) {
        try {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            png.write(SIGNATURE);
            int colorType = colorType(pixels.format());
            writeChunk(png, "IHDR", header(pixels.width(), pixels.height(), colorType));
            writeChunk(png, "IDAT", compressedRows(pixels));
            writeChunk(png, "IEND", new byte[0]);
            return png.toByteArray();
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static ReproducibilityFingerprint contentFingerprint(byte[] bytes) {
        try {
            return new ReproducibilityFingerprint(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by Java", exception);
        }
    }

    private static byte[] header(int width, int height, int colorType) throws IOException {
        ByteArrayOutputStream header = new ByteArrayOutputStream(13);
        writeInt(header, width);
        writeInt(header, height);
        header.write(8);
        header.write(colorType);
        header.write(0);
        header.write(0);
        header.write(0);
        return header.toByteArray();
    }

    private static byte[] compressedRows(TexturePixels pixels) throws IOException {
        byte[] source = pixels.bytes();
        int sourceStride = pixels.width() * pixels.format().bytesPerPixel();
        int targetStride = pixels.width() * channels(pixels.format());
        ByteArrayOutputStream filtered = new ByteArrayOutputStream((targetStride + 1) * pixels.height());
        for (int y = 0; y < pixels.height(); y++) {
            filtered.write(0);
            int offset = y * sourceStride;
            if (pixels.format() == TexturePixelFormat.RG8_UNORM) {
                for (int x = 0; x < pixels.width(); x++) {
                    filtered.write(source[offset + x * 2]);
                    filtered.write(source[offset + x * 2 + 1]);
                    filtered.write(0);
                    filtered.write(255);
                }
            } else {
                filtered.write(source, offset, sourceStride);
            }
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream output = new DeflaterOutputStream(compressed)) {
            output.write(filtered.toByteArray());
        }
        return compressed.toByteArray();
    }

    private static int colorType(TexturePixelFormat format) {
        return switch (format) {
            case R8_UNORM -> 0;
            case RGB8_UNORM -> 2;
            case RGBA8_UNORM, RG8_UNORM -> 6;
        };
    }

    private static int channels(TexturePixelFormat format) {
        return format == TexturePixelFormat.RG8_UNORM ? 4 : format.bytesPerPixel();
    }

    private static void writeChunk(ByteArrayOutputStream output, String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        writeInt(output, data.length);
        output.write(typeBytes);
        output.write(data);
        CRC32 checksum = new CRC32();
        checksum.update(typeBytes);
        checksum.update(data);
        writeInt(output, (int) checksum.getValue());
    }

    private static void writeInt(ByteArrayOutputStream output, int value) throws IOException {
        output.write((value >>> 24) & 0xff);
        output.write((value >>> 16) & 0xff);
        output.write((value >>> 8) & 0xff);
        output.write(value & 0xff);
    }
}
