package com.planeguardian.assets.assetgenerator.sample.export;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.planeguardian.assets.assetgenerator.sample.generation.SampleShapeProduct;
import com.planeguardian.assets.generation.adapters.jme.JmeMeshAdapter;

import java.util.Objects;

/** jME render-boundary adapter for the sample shape generator; never authors geometry itself. */
public final class SampleShapeJmeAdapter {
    private SampleShapeJmeAdapter() {
    }

    public static Node create(AssetManager assets, SampleShapeProduct product, String generatorId) {
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(generatorId, "generatorId");
        Node root = new Node("sample.shape");
        Geometry shape = new Geometry(product.cylinder() ? "Cylinder" : "Box",
                JmeMeshAdapter.convert(product.renderMesh()));
        Material material = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
        material.setColor("Diffuse", ColorRGBA.White);
        material.setBoolean("UseMaterialColors", true);
        shape.setMaterial(material);
        root.attachChild(shape);
        root.setUserData("pg.generatorId", generatorId);
        root.setUserData("pg.sample.cylinder", product.cylinder());
        return root;
    }
}
