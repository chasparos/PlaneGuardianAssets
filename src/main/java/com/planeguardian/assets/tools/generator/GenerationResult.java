package com.planeguardian.assets.tools.generator;

import java.nio.file.Path;

/**
 * Result returned by {@link AssetGenerator#generate(Path)}.
 *
 * @param success     {@code true} if generation completed without errors
 * @param outputPath  path to the generated file, or {@code null} on failure
 * @param assetName   suggested name for the asset in the library
 * @param message     human-readable status message (always non-null)
 */
public record GenerationResult(boolean success, Path outputPath, String assetName, String message) {

    public static GenerationResult success(Path outputPath, String assetName, String message) {
        return new GenerationResult(true, outputPath, assetName, message);
    }

    public static GenerationResult failure(String message) {
        return new GenerationResult(false, null, null, message);
    }
}
