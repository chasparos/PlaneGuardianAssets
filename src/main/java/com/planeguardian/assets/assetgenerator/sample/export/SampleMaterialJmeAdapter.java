package com.planeguardian.assets.assetgenerator.sample.export;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;
import com.planeguardian.assets.generation.resources.material.MaterialValue;

import java.util.List;
import java.util.Objects;

/**
 * jME render-boundary adapter that realizes a {@code SampleMaterialRecipeFactory}
 * recipe as a concrete {@code Common/MatDefs/Light/Lighting.j3md} material.
 *
 * <p>Soft contract: like every other jME adapter in this package, this class is the
 * only place allowed to touch the engine {@code Material} type for the sample
 * generator; the recipe itself must remain engine-neutral and fully describe the
 * result so the same recipe always produces the same material.
 */
public final class SampleMaterialJmeAdapter {
    private SampleMaterialJmeAdapter() {
    }

    public static Material create(AssetManager assets, MaterialRecipe recipe) {
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(recipe, "recipe");
        Material material = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
        ColorRGBA base = color(recipe, "base-color", ColorRGBA.White);
        ColorRGBA specular = color(recipe, "specular-color", ColorRGBA.Gray);
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Diffuse", base);
        material.setColor("Ambient", base);
        material.setColor("Specular", specular);
        material.setFloat("Shininess", (float) scalar(recipe, "shininess", 16));
        material.setName("sample-shape." + recipe.resource().resourceId().value() + "." + recipe.fingerprint().hex());
        return material;
    }

    private static double scalar(MaterialRecipe recipe, String id, double fallback) {
        var value = recipe.inputs().get(new StableId(id));
        return value instanceof MaterialValue.Numeric numeric && numeric.components().size() == 1
                ? numeric.components().get(0) : fallback;
    }

    private static ColorRGBA color(MaterialRecipe recipe, String id, ColorRGBA fallback) {
        var value = recipe.inputs().get(new StableId(id));
        if (!(value instanceof MaterialValue.Numeric numeric) || numeric.components().size() < 3) return fallback;
        List<Double> c = numeric.components();
        return new ColorRGBA(c.get(0).floatValue(), c.get(1).floatValue(), c.get(2).floatValue(),
                c.size() > 3 ? c.get(3).floatValue() : 1f);
    }
}
