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
        Material material = new Material(assets, "Common/MatDefs/Light/PBRLighting.j3md");
        material.setColor("BaseColor", color(recipe, "base-color", new ColorRGBA(.7f, .8f, .9f, .6f)));
        ColorRGBA emissive = color(recipe, "emissive", ColorRGBA.Black);
        float power = (float) scalar(recipe, "emission-strength", defaultEmissionStrength);
        material.setColor("Emissive", emissive.mult(power));
        material.setFloat("Roughness", (float) scalar(recipe, "roughness", .18));
        material.setFloat("Metallic", (float) scalar(recipe, "metallic", 0.02));
        material.setFloat("AlphaDiscardThreshold", 0.02f);
        material.setTransparent(true);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        material.setFloat("Glossiness", (float) scalar(recipe, "crystal-fresnel", .4));
        material.setName("crystal-pbr." + recipe.fingerprint().hex());
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
