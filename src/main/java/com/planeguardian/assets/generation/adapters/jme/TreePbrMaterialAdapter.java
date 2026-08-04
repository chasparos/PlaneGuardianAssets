package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.cache.CachedResourceArtifact;
import com.planeguardian.assets.generation.resources.cache.GeneratedResourceCache;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.material.MaterialValue;
import com.planeguardian.assets.generation.tree.TreePresentationSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** Converts immutable tree recipes to fresh jME PBR material instances at the renderer boundary. */
public final class TreePbrMaterialAdapter {
    public static final StableId BARK = new StableId("tree.bark");
    public static final StableId FOLIAGE = new StableId("tree.foliage");
    public static final StableId HOLLOW_RIM = new StableId("tree.hollow-rim");
    public static final StableId HOLLOW_INTERIOR = new StableId("tree.hollow-interior");

    private TreePbrMaterialAdapter() {
    }

    public static Material create(com.jme3.asset.AssetManager assets, StableId role, MaterialRecipe recipe,
                                  TreePresentationSettings settings) {
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(settings, "settings");
        Material material = new Material(assets, "Common/MatDefs/Light/PBRLighting.j3md");
        material.setColor("BaseColor", color(recipe, "base-color", defaultColor(role)));
        material.setFloat("Roughness", bounded(recipe, "roughness", .7, 0, 1, settings.roughnessBias()));
        material.setFloat("Metallic", bounded(recipe, "metallic", 0, 0, 1, 0));
        material.setColor("Emissive", color(recipe, "emissive", ColorRGBA.Black).mult((float) settings.emissionStrength()));
        material.setFloat("EmissivePower", (float) settings.emissionStrength());
        material.setName("tree-pbr." + role.value() + "." + recipe.fingerprint().hex());
        return material;
    }

    /**
     * Creates a fresh PBR material and binds only cache-verified PNG artifacts.
     * Recipe texture keys map to the standard PBR slots; foliage coverage is an
     * alpha-clipped base-color mask and host contact is realized as an AO light map.
     */
    public static Material create(com.jme3.asset.AssetManager assets, StableId role, MaterialRecipe recipe,
                                  TreePresentationSettings settings, GeneratedResourceCache cache) throws IOException {
        Objects.requireNonNull(cache, "cache");
        Material material = create(assets, role, recipe, settings);
        for (var entry : recipe.inputs().entrySet()) {
            if (entry.getValue() instanceof MaterialValue.Texture binding) {
                bind(material, role, entry.getKey(), binding, cache, settings);
            }
        }
        return material;
    }

    private static void bind(Material material, StableId role, StableId input, MaterialValue.Texture binding,
                             GeneratedResourceCache cache, TreePresentationSettings settings) throws IOException {
        CachedResourceArtifact cached = cache.find(binding.fingerprint()).orElseThrow(
                () -> new IllegalArgumentException("Missing generated texture artifact: " + binding.resource().resourceId()));
        if (!cached.generationFingerprint().equals(binding.fingerprint())
                || !cached.encodedArtifact().artifact().resource().equals(binding.resource())
                || !"image/png".equals(cached.encodedArtifact().artifact().mediaType())) {
            throw new IllegalArgumentException("Generated texture artifact identity does not match material binding: "
                    + binding.resource().resourceId());
        }
        Texture texture = png(cached);
        switch (input.value()) {
            case "base-color-map" -> material.setTexture("BaseColorMap", texture);
            case "normal-map" -> material.setTexture("NormalMap", texture);
            case "metallic-map" -> material.setTexture("MetallicMap", texture);
            case "roughness-map" -> material.setTexture("RoughnessMap", texture);
            case "metallic-roughness-map" -> material.setTexture("MetallicRoughnessMap", texture);
            case "emissive-map" -> material.setTexture("EmissiveMap", texture);
            case "host-contact-mask" -> {
                material.setTexture("LightMap", texture);
                material.setBoolean("LightMapAsAOMap", true);
                material.setFloat("AoStrength", (float) settings.hostContactBlend());
            }
            case "foliage-coverage-mask" -> {
                if (!role.equals(FOLIAGE)) {
                    throw new IllegalArgumentException("Foliage coverage masks require the foliage role");
                }
                material.setTexture("BaseColorMap", texture);
                material.setFloat("AlphaDiscardThreshold", .5f);
            }
            default -> throw new IllegalArgumentException("Unsupported tree PBR texture input: " + input);
        }
    }

    private static Texture png(CachedResourceArtifact cached) throws IOException {
        Texture2D texture = new Texture2D(new AWTLoader().load(
                new ByteArrayInputStream(cached.encodedArtifact().bytes()), true));
        texture.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        return texture;
    }

    private static float bounded(MaterialRecipe recipe, String id, double defaultValue, double min, double max, double bias) {
        double value = scalar(recipe, id, defaultValue) + bias;
        return (float) Math.max(min, Math.min(max, value));
    }

    private static double scalar(MaterialRecipe recipe, String id, double defaultValue) {
        MaterialValue value = recipe.inputs().get(new StableId(id));
        return value instanceof MaterialValue.Numeric numeric && numeric.components().size() == 1
                ? numeric.components().get(0) : defaultValue;
    }

    private static ColorRGBA color(MaterialRecipe recipe, String id, ColorRGBA fallback) {
        MaterialValue value = recipe.inputs().get(new StableId(id));
        if (!(value instanceof MaterialValue.Numeric numeric) || numeric.components().size() < 3) return fallback;
        List<Double> c = numeric.components();
        return new ColorRGBA(c.get(0).floatValue(), c.get(1).floatValue(), c.get(2).floatValue(),
                c.size() == 4 ? c.get(3).floatValue() : 1);
    }

    private static ColorRGBA defaultColor(StableId role) {
        if (role.equals(FOLIAGE)) return new ColorRGBA(.12f, .38f, .06f, 1);
        if (role.equals(HOLLOW_INTERIOR)) return new ColorRGBA(.08f, .045f, .02f, 1);
        if (role.equals(HOLLOW_RIM)) return new ColorRGBA(.22f, .12f, .05f, 1);
        return new ColorRGBA(.25f, .13f, .055f, 1);
    }
}
