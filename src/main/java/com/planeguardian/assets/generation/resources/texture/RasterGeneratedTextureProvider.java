package com.planeguardian.assets.generation.resources.texture;

/** Generated texture provider that produces an immutable raw raster product. */
public interface RasterGeneratedTextureProvider extends GeneratedTextureProvider {
    GeneratedTextureProduct generate(TextureGenerationRequest request);

    @Override
    default GeneratedTexture describe(TextureGenerationRequest request) {
        return generate(request).descriptor();
    }
}
