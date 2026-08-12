package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.material.MaterialValue;
import com.planeguardian.assets.generation.api.StableId;
import java.util.List;

/** Crystal-specific jME material boundary adapter. */
public final class CrystalMaterialAdapter {
    private CrystalMaterialAdapter() { }

    public static Material create(com.jme3.asset.AssetManager assets, MaterialRecipe recipe, double defaultEmissionStrength) {
        return createFrontFace(assets, recipe, defaultEmissionStrength);
    }

    public static Material createFrontFace(com.jme3.asset.AssetManager assets, MaterialRecipe recipe,
                                           double defaultEmissionStrength) {
        return createLayer(assets, recipe, defaultEmissionStrength, RenderState.FaceCullMode.Back,
                new ColorRGBA(1f, 1f, 1f, 1f), "front");
    }

    public static Material createBackFace(com.jme3.asset.AssetManager assets, MaterialRecipe recipe,
                                          double defaultEmissionStrength) {
        // A cool secondary layer supplies a stable, renderer-friendly approximation of
        // dispersion without asking the asset package to regenerate geometry.
        return createLayer(assets, recipe, defaultEmissionStrength * .65, RenderState.FaceCullMode.Front,
                new ColorRGBA(.55f, .78f, 1f, 1f), "back");
    }

    private static Material createLayer(com.jme3.asset.AssetManager assets, MaterialRecipe recipe,
                                         double defaultEmissionStrength, RenderState.FaceCullMode cullMode,
                                         ColorRGBA layerTint, String layerName) {
        Material material = new Material(assets, "MatDefs/Crystal/CrystalApprox.j3md");
        ColorRGBA base = color(recipe, "base-color", new ColorRGBA(.7f, .8f, .9f, .6f));
        material.setColor("BaseColor", new ColorRGBA(base.r * layerTint.r, base.g * layerTint.g,
                base.b * layerTint.b, base.a));
        ColorRGBA emissive = color(recipe, "emissive", ColorRGBA.Black);
        float power = (float) scalar(recipe, "emission-strength", defaultEmissionStrength);
        material.setColor("Emissive", new ColorRGBA(emissive.r * layerTint.r, emissive.g * layerTint.g,
                emissive.b * layerTint.b, emissive.a).mult(power));
        material.setFloat("Roughness", (float) scalar(recipe, "roughness", .1));
        material.setFloat("Metallic", (float) scalar(recipe, "metallic", 0.02));
        material.setFloat("CrystalOpacity", (float) scalar(recipe, "crystal-opacity", base.a));
        material.setFloat("CrystalFresnel", (float) scalar(recipe, "crystal-fresnel", .5));
        material.setFloat("CrystalRefraction", (float) scalar(recipe, "crystal-refraction", .12));
        material.setFloat("CrystalNoiseScale", (float) scalar(recipe, "crystal-noise-scale", 4));
        material.setFloat("CrystalNoiseStrength", (float) scalar(recipe, "crystal-noise-strength", .1));
        material.setFloat("CrystalGlintStrength", (float) scalar(recipe, "crystal-glint-strength", .25));
        material.setFloat("CrystalGlintPower", (float) scalar(recipe, "crystal-glint-power", 5));
        material.setFloat("AlphaDiscardThreshold", 0.001f);
        material.setTransparent(true);
        RenderState state = material.getAdditionalRenderState();
        state.setBlendMode(RenderState.BlendMode.Alpha);
        state.setFaceCullMode(cullMode);
        state.setDepthWrite(false);
        state.setDepthTest(true);
        material.setName("crystal-transparent-" + layerName + "." + recipe.fingerprint().hex());
        return material;
    }

    private static double scalar(MaterialRecipe recipe, String id, double fallback) {
        var value = recipe.inputs().get(new StableId(id));
        return value instanceof MaterialValue.Numeric numeric && numeric.components().size() == 1 ? numeric.components().get(0) : fallback;
    }
    private static ColorRGBA color(MaterialRecipe recipe, String id, ColorRGBA fallback) {
        var value = recipe.inputs().get(new StableId(id));
        if (!(value instanceof MaterialValue.Numeric numeric) || numeric.components().size() < 3) return fallback;
        List<Double> c = numeric.components();
        return new ColorRGBA(c.get(0).floatValue(), c.get(1).floatValue(), c.get(2).floatValue(), c.size() > 3 ? c.get(3).floatValue() : 1f);
    }
}
