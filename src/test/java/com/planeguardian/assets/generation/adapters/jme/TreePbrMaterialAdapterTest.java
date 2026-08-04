package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.material.Material;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.cache.CachedResourceArtifact;
import com.planeguardian.assets.generation.resources.cache.GeneratedResourceCache;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.material.MaterialRecipeFingerprints;
import com.planeguardian.assets.generation.resources.material.MaterialValue;
import com.planeguardian.assets.generation.resources.texture.EncodedTextureArtifact;
import com.planeguardian.assets.generation.resources.texture.GeneratedTexture;
import com.planeguardian.assets.generation.resources.texture.GeneratedTextureFingerprints;
import com.planeguardian.assets.generation.resources.texture.GeneratedTextureProduct;
import com.planeguardian.assets.generation.resources.texture.PngTextureEncoder;
import com.planeguardian.assets.generation.resources.texture.TextureChannel;
import com.planeguardian.assets.generation.resources.texture.TextureColorSpace;
import com.planeguardian.assets.generation.resources.texture.TextureGenerationRequest;
import com.planeguardian.assets.generation.resources.texture.TexturePixelFormat;
import com.planeguardian.assets.generation.resources.texture.TexturePixels;
import com.planeguardian.assets.generation.preview.RuntimeWeatherInput;
import com.planeguardian.assets.generation.tree.TreePresentationSettings;
import com.planeguardian.assets.generation.tree.TreeWindResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreePbrMaterialAdapterTest {
    private static final ContractVersion VERSION = new ContractVersion(1, 0);

    @Test
    void bindsVerifiedFoliageCoverageAndHostContactArtifacts() throws IOException {
        CachedResourceArtifact coverage = artifact("texture.foliage-coverage", 1);
        CachedResourceArtifact contact = artifact("texture.host-contact", 2);
        MaterialRecipe recipe = recipe(Map.of(
                new StableId("foliage-coverage-mask"), texture(coverage),
                new StableId("host-contact-mask"), texture(contact)));

        Material material = TreePbrMaterialAdapter.create(new DesktopAssetManager(true), TreePbrMaterialAdapter.FOLIAGE,
                recipe, TreePresentationSettings.defaults(), cache(coverage, contact));

        assertNotNull(material.getParam("BaseColorMap"));
        assertNotNull(material.getParam("LightMap"));
        assertEquals(.5f, material.getParam("AlphaDiscardThreshold").getValue());
        assertEquals(Boolean.TRUE, material.getParam("LightMapAsAOMap").getValue());
        Texture coverageTexture = (Texture) material.getParam("BaseColorMap").getValue();
        assertEquals(Texture.MinFilter.Trilinear, coverageTexture.getMinFilter());
        assertEquals(Image.Format.RGBA8, coverageTexture.getImage().getFormat());
        int alpha = coverageTexture.getImage().getData(0).duplicate().get(3) & 0xff;
        assertTrue(alpha > 0 && alpha < 255);
    }

    @Test
    void rejectsMissingOrMismatchedGeneratedArtifacts() {
        CachedResourceArtifact artifact = artifact("texture.bark", 3);
        MaterialRecipe recipe = recipe(Map.of(new StableId("base-color-map"), texture(artifact)));

        assertThrows(IllegalArgumentException.class, () -> TreePbrMaterialAdapter.create(new DesktopAssetManager(true),
                TreePbrMaterialAdapter.BARK, recipe, TreePresentationSettings.defaults(), cache()));
        assertThrows(IllegalArgumentException.class, () -> TreePbrMaterialAdapter.create(new DesktopAssetManager(true),
                TreePbrMaterialAdapter.BARK, recipe, TreePresentationSettings.defaults(),
                cache(new CachedResourceArtifact(artifact.generationFingerprint(), artifact("texture.other", 4).encodedArtifact()))));
    }

    @Test
    void rejectsUnsupportedTextureInputsAndCoverageForNonFoliageRoles() {
        CachedResourceArtifact artifact = artifact("texture.bark", 3);

        assertThrows(IllegalArgumentException.class, () -> TreePbrMaterialAdapter.create(new DesktopAssetManager(true),
                TreePbrMaterialAdapter.BARK, recipe(Map.of(new StableId("unknown-map"), texture(artifact))),
                TreePresentationSettings.defaults(), cache(artifact)));
        assertThrows(IllegalArgumentException.class, () -> TreePbrMaterialAdapter.create(new DesktopAssetManager(true),
                TreePbrMaterialAdapter.BARK, recipe(Map.of(new StableId("foliage-coverage-mask"), texture(artifact))),
                TreePresentationSettings.defaults(), cache(artifact)));
    }

    @Test
    void bindsDeterministicResponseAndRuntimeWeatherToTheWindShader() {
        TreePresentationSettings settings = TreePresentationSettings.defaults();
        Material material = TreePbrMaterialAdapter.create(new DesktopAssetManager(true), TreePbrMaterialAdapter.BARK,
                recipe(Map.of()), settings);
        TreeWindResponse response = TreeWindResponse.forPart(TreePbrMaterialAdapter.BARK, settings, 42);

        TreeWindJmeAdapter.bind(material, response, settings,
                new RuntimeWeatherInput(new com.planeguardian.assets.generation.api.Vector3(2, 0, 0), .6, 12.5));

        assertEquals((float) response.weight(), material.getParam("WindWeight").getValue());
        assertEquals((float) response.phaseOffset(), material.getParam("WindPhase").getValue());
        assertEquals(.8f, material.getParam("WindFrequency").getValue());
        assertEquals(.6f, material.getParam("WindIntensity").getValue());
        assertEquals(12.5f, material.getParam("WindTime").getValue());
        assertEquals(1f, ((com.jme3.math.Vector3f) material.getParam("WindDirection").getValue()).x);
    }

    @Test
    void appliesReviewedPbrHostContactAndEmissionControls() {
        TreePresentationSettings settings = new TreePresentationSettings(
                com.planeguardian.assets.generation.api.RenderTier.GAMEPLAY, .7, .45, .8, .5, .15, 1, .5);
        Material material = TreePbrMaterialAdapter.create(new DesktopAssetManager(true), TreePbrMaterialAdapter.BARK,
                recipe(Map.of(
                        new StableId("roughness"), new MaterialValue.Numeric(java.util.List.of(.6)),
                        new StableId("metallic"), new MaterialValue.Numeric(java.util.List.of(.1)),
                        new StableId("emissive"), new MaterialValue.Numeric(java.util.List.of(.2, .4, .6)))),
                settings);

        assertEquals(.75f, material.getParam("Roughness").getValue());
        assertEquals(.1f, material.getParam("Metallic").getValue());
        assertEquals(.5f, material.getParam("EmissivePower").getValue());
        assertEquals(.1f, ((com.jme3.math.ColorRGBA) material.getParam("Emissive").getValue()).r);
    }

    private static GeneratedResourceCache cache(CachedResourceArtifact... artifacts) {
        Map<ReproducibilityFingerprint, CachedResourceArtifact> entries = new java.util.HashMap<>();
        for (CachedResourceArtifact artifact : artifacts) {
            entries.put(artifact.generationFingerprint(), artifact);
        }
        return new GeneratedResourceCache() {
            @Override
            public Optional<CachedResourceArtifact> find(ReproducibilityFingerprint fingerprint) {
                return Optional.ofNullable(entries.get(fingerprint));
            }

            @Override
            public CachedResourceArtifact store(ReproducibilityFingerprint fingerprint, EncodedTextureArtifact artifact) {
                CachedResourceArtifact cached = new CachedResourceArtifact(fingerprint, artifact);
                entries.putIfAbsent(fingerprint, cached);
                return entries.get(fingerprint);
            }
        };
    }

    private static MaterialRecipe recipe(Map<StableId, MaterialValue> inputs) {
        GeneratedResourceRef resource = new GeneratedResourceRef(new StableId("material.tree"), ResourceKind.MATERIAL, VERSION);
        return new MaterialRecipe(resource, VERSION, new StableId("pbr"), inputs,
                MaterialRecipeFingerprints.identity(resource.resourceId(), resource.version(), VERSION, new StableId("pbr"), inputs));
    }

    private static MaterialValue.Texture texture(CachedResourceArtifact artifact) {
        return new MaterialValue.Texture(artifact.encodedArtifact().artifact().resource(), artifact.generationFingerprint());
    }

    private static CachedResourceArtifact artifact(String resourceId, int marker) {
        GeneratedResourceRef resource = new GeneratedResourceRef(new StableId(resourceId), ResourceKind.TEXTURE, VERSION);
        TextureGenerationRequest request = new TextureGenerationRequest(resource.resourceId(), 1, 1, marker, Map.of(), java.util.List.of());
        ReproducibilityFingerprint fingerprint = GeneratedTextureFingerprints.cacheIdentity(
                new StableId("provider.test"), VERSION, VERSION, request);
        GeneratedTexture texture = new GeneratedTexture(resource, new StableId("provider.test"), VERSION, VERSION, request,
                fingerprint, TextureColorSpace.LINEAR, Map.of(TextureChannel.RED, new StableId("coverage")));
        EncodedTextureArtifact encoded = new PngTextureEncoder().encode(
                new GeneratedTextureProduct(texture, new TexturePixels(1, 1, TexturePixelFormat.R8_UNORM, new byte[]{(byte) marker})),
                new StableId("artifact." + marker), "textures/test-" + marker + ".png");
        return new CachedResourceArtifact(fingerprint, encoded);
    }
}
