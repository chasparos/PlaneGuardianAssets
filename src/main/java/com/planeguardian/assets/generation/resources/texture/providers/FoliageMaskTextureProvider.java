package com.planeguardian.assets.generation.resources.texture.providers;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.texture.GeneratedTextureProduct;
import com.planeguardian.assets.generation.resources.texture.RasterGeneratedTextureProvider;
import com.planeguardian.assets.generation.resources.texture.TextureChannel;
import com.planeguardian.assets.generation.resources.texture.TextureColorSpace;
import com.planeguardian.assets.generation.resources.texture.TextureGenerationRequest;
import com.planeguardian.assets.generation.resources.texture.TexturePixelFormat;
import com.planeguardian.assets.generation.resources.texture.TexturePixels;

import java.util.Map;
import java.util.Set;

/** Deterministic clustered alpha-clip coverage mask for foliage cards and shells. */
public final class FoliageMaskTextureProvider implements RasterGeneratedTextureProvider {
    public static final StableId ID = new StableId("texture-provider.foliage-mask");
    private static final Set<String> PARAMETERS = Set.of("clusterScale", "coverage");

    @Override public StableId providerId() { return ID; }
    @Override public ContractVersion providerVersion() { return ProviderTextureSupport.VERSION; }
    @Override public ContractVersion parameterSchemaVersion() { return ProviderTextureSupport.VERSION; }

    @Override
    public GeneratedTextureProduct generate(TextureGenerationRequest request) {
        ProviderTextureSupport.requireParameters(request, PARAMETERS);
        double clusterScale = ProviderTextureSupport.number(request, "clusterScale", 1, 32);
        double coverage = ProviderTextureSupport.number(request, "coverage", 0.05, 0.95);
        byte[] pixels = new byte[ProviderTextureSupport.pixelBytes(request, 1)];
        for (int y = 0; y < request.height(); y++) {
            for (int x = 0; x < request.width(); x++) {
                double u = (x + 0.5) / request.width() - 0.5;
                double v = (y + 0.5) / request.height() - 0.5;
                double envelope = 1 - StrictMath.min(1, StrictMath.sqrt(u * u + v * v) * 2);
                double clustered = 0.5 + 0.24 * StrictMath.sin(u * clusterScale * StrictMath.PI)
                        + 0.24 * StrictMath.sin(v * clusterScale * StrictMath.PI * 1.31)
                        + 0.20 * (ProviderTextureSupport.noise(request.seed(), x / 3, y / 3) - 0.5);
                double threshold = 0.72 - coverage * 0.55;
                double mask = smoothstep(threshold - 0.08, threshold + 0.08, clustered * envelope);
                pixels[y * request.width() + x] = (byte) ProviderTextureSupport.byteValue(mask);
            }
        }
        var descriptor = ProviderTextureSupport.descriptor(ID, request, TextureColorSpace.LINEAR,
                Map.of(TextureChannel.RED, new StableId("channel.foliage-coverage")));
        return new GeneratedTextureProduct(descriptor,
                new TexturePixels(request.width(), request.height(), TexturePixelFormat.R8_UNORM, pixels));
    }

    private static double smoothstep(double low, double high, double value) {
        double t = StrictMath.max(0, StrictMath.min(1, (value - low) / (high - low)));
        return t * t * (3 - 2 * t);
    }
}
