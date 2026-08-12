package com.planeguardian.assets.assetgenerator.tree.generation.texture.providers;

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

/** Small deterministic painterly bark base; production art direction can replace the algorithm behind its version. */
public final class PainterlyBarkTextureProvider implements RasterGeneratedTextureProvider {
    public static final StableId ID = new StableId("texture-provider.painterly-bark");
    private static final Set<String> PARAMETERS = Set.of("contrast", "grainScale");

    @Override public StableId providerId() { return ID; }
    @Override public ContractVersion providerVersion() { return ProviderTextureSupport.VERSION; }
    @Override public ContractVersion parameterSchemaVersion() { return ProviderTextureSupport.VERSION; }

    @Override
    public GeneratedTextureProduct generate(TextureGenerationRequest request) {
        ProviderTextureSupport.requireParameters(request, PARAMETERS);
        double contrast = ProviderTextureSupport.number(request, "contrast", 0, 1);
        double grainScale = ProviderTextureSupport.number(request, "grainScale", 1, 64);
        byte[] pixels = new byte[ProviderTextureSupport.pixelBytes(request, 4)];
        for (int y = 0; y < request.height(); y++) {
            for (int x = 0; x < request.width(); x++) {
                double u = (double) x / request.width();
                double v = (double) y / request.height();
                double noise = ProviderTextureSupport.noise(request.seed(), x / 2, y / 4) - 0.5;
                double grain = 0.5 + 0.5 * StrictMath.sin((u + 0.10 * noise
                        + 0.035 * StrictMath.sin(v * 13)) * grainScale * StrictMath.PI * 2);
                double crack = StrictMath.pow(1 - grain, 7) * contrast;
                double light = 0.76 + 0.30 * grain + 0.12 * noise - 0.55 * crack;
                int offset = (y * request.width() + x) * 4;
                pixels[offset] = (byte) ProviderTextureSupport.byteValue(0.42 * light);
                pixels[offset + 1] = (byte) ProviderTextureSupport.byteValue(0.21 * light);
                pixels[offset + 2] = (byte) ProviderTextureSupport.byteValue(0.085 * light);
                pixels[offset + 3] = (byte) 255;
            }
        }
        var descriptor = ProviderTextureSupport.descriptor(ID, request, TextureColorSpace.SRGB, Map.of(
                TextureChannel.RED, new StableId("channel.albedo.r"),
                TextureChannel.GREEN, new StableId("channel.albedo.g"),
                TextureChannel.BLUE, new StableId("channel.albedo.b"),
                TextureChannel.ALPHA, new StableId("channel.opacity")));
        return new GeneratedTextureProduct(descriptor,
                new TexturePixels(request.width(), request.height(), TexturePixelFormat.RGBA8_UNORM, pixels));
    }
}
