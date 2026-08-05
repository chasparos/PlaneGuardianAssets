package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import java.nio.file.Path;

/** Swing-neutral authoring provider discovered by the desktop workbench. */
public interface AuthoringGeneratorProvider {
    GeneratorDescriptor descriptor();
    default java.util.Optional<com.planeguardian.assets.generation.semantics.AssetSemanticAdapter> semanticAdapter() {
        return java.util.Optional.empty();
    }
    GenerationResult generate(AuthoringGenerationRequest request, Path outputDirectory);
}
