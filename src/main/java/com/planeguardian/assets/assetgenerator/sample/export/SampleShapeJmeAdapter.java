package com.planeguardian.assets.assetgenerator.sample.export;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.planeguardian.assets.assetgenerator.sample.generation.SampleShapeProduct;
import com.planeguardian.assets.generation.adapters.jme.JmeMeshAdapter;
import com.planeguardian.assets.generation.resources.material.MaterialRecipe;

import java.util.Objects;

/**
 * jME render-boundary adapter for the sample shape generator; never authors geometry
 * itself.
 *
 * <p>Soft contract: this adapter only converts already-produced, engine-neutral
 * results ({@link com.planeguardian.assets.generation.surface.RenderMesh} via
 * {@link JmeMeshAdapter}, and a {@link MaterialRecipe} via
 * {@code SampleMaterialJmeAdapter}) into jME scene-graph state. It must not decide
 * geometry or material values on its own.
 */
public final class SampleShapeJmeAdapter {
    private SampleShapeJmeAdapter() {
    }

    public static Node create(AssetManager assets, SampleShapeProduct product, MaterialRecipe materialRecipe,
                              String generatorId) {
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(materialRecipe, "materialRecipe");
        Objects.requireNonNull(generatorId, "generatorId");
        Node root = new Node("sample.shape");
        Geometry shape = new Geometry(product.cylinder() ? "Cylinder" : "Box",
                JmeMeshAdapter.convert(product.renderMesh()));
        Material material = SampleMaterialJmeAdapter.create(assets, materialRecipe);
        shape.setMaterial(material);
        root.attachChild(shape);
        root.setUserData("pg.generatorId", generatorId);
        root.setUserData("pg.sample.cylinder", product.cylinder());
        return root;
    }
}
