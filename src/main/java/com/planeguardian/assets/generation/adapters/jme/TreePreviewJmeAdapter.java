package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.preview.RuntimeWeatherInput;
import com.planeguardian.assets.generation.preview.TreePreviewFixture;
import com.planeguardian.assets.generation.tree.TreeCrownProduct;
import com.planeguardian.assets.generation.tree.TreePresentationSettings;
import com.planeguardian.assets.generation.tree.TreeStructuralProduct;
import com.planeguardian.assets.generation.tree.TreeWindResponse;

import java.util.Objects;

/** Realizes a fixed engine-neutral tree gameplay fixture at the jME renderer boundary. */
public final class TreePreviewJmeAdapter {
    private TreePreviewJmeAdapter() {
    }

    public static PreviewScene create(AssetManager assets, TreeStructuralProduct structure, TreeCrownProduct crown,
                                      TreePreviewFixture fixture, TreePresentationSettings settings,
                                      RuntimeWeatherInput weather, long visualSeed) {
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(structure, "structure");
        Objects.requireNonNull(crown, "crown");
        Objects.requireNonNull(fixture, "fixture");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(weather, "weather");
        Node root = new Node("tree.gameplay-fixture");
        structure.parts().values().forEach(part -> root.attachChild(geometry(assets, part.id(), part.role(),
                part.renderMesh(), settings, weather, visualSeed)));
        crown.parts().values().forEach(part -> root.attachChild(geometry(assets, part.id(), part.role(),
                part.renderMesh(), settings, weather, visualSeed)));
        DirectionalLight sun = new DirectionalLight(toJme(fixture.lightDirection()).normalizeLocal(), ColorRGBA.White);
        AmbientLight ambient = new AmbientLight(ColorRGBA.White.mult((float) fixture.ambientIntensity()));
        root.addLight(sun);
        root.addLight(ambient);
        return new PreviewScene(root, sun, ambient, fixture);
    }

    private static Geometry geometry(AssetManager assets, StableId id, StableId role,
                                     com.planeguardian.assets.generation.surface.RenderMesh mesh,
                                     TreePresentationSettings settings, RuntimeWeatherInput weather, long visualSeed) {
        Geometry geometry = new Geometry(id.value(), JmeMeshAdapter.convert(mesh));
        var material = TreePbrMaterialAdapter.create(assets, materialRole(role), emptyRecipe(role), settings);
        TreeWindJmeAdapter.bind(material, TreeWindResponse.forPart(id, settings, visualSeed), settings, weather);
        geometry.setMaterial(material);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        return geometry;
    }

    private static StableId materialRole(StableId role) {
        return role.value().equals("tree.foliage") ? TreePbrMaterialAdapter.FOLIAGE
                : role.value().equals("tree.hollow") ? TreePbrMaterialAdapter.HOLLOW_INTERIOR
                : TreePbrMaterialAdapter.BARK;
    }

    private static com.planeguardian.assets.generation.resources.material.MaterialRecipe emptyRecipe(StableId role) {
        var resource = new com.planeguardian.assets.generation.api.GeneratedResourceRef(
                new StableId("material.preview." + role.value()),
                com.planeguardian.assets.generation.api.ResourceKind.MATERIAL,
                new com.planeguardian.assets.generation.api.ContractVersion(1, 0));
        return new com.planeguardian.assets.generation.resources.material.MaterialRecipe(resource, resource.version(),
                new StableId("pbr"), java.util.Map.of(),
                com.planeguardian.assets.generation.resources.material.MaterialRecipeFingerprints.identity(
                        resource.resourceId(), resource.version(), resource.version(), new StableId("pbr"), java.util.Map.of()));
    }

    private static Vector3f toJme(com.planeguardian.assets.generation.api.Vector3 vector) {
        return new Vector3f((float) vector.x(), (float) vector.y(), (float) vector.z());
    }

    public record PreviewScene(Node root, DirectionalLight sun, AmbientLight ambient, TreePreviewFixture fixture) {
        public PreviewScene {
            Objects.requireNonNull(root, "root");
            Objects.requireNonNull(sun, "sun");
            Objects.requireNonNull(ambient, "ambient");
            Objects.requireNonNull(fixture, "fixture");
        }

        public void applyCamera(Camera camera) {
            Objects.requireNonNull(camera, "camera");
            camera.setLocation(toJme(fixture.cameraPosition()));
            camera.lookAt(toJme(fixture.cameraTarget()), Vector3f.UNIT_Y);
        }

        public DirectionalLightShadowRenderer shadowRenderer(AssetManager assets) {
            Objects.requireNonNull(assets, "assets");
            if (!fixture.shadowsEnabled()) throw new IllegalStateException("Gameplay fixture does not enable shadows");
            DirectionalLightShadowRenderer shadows = new DirectionalLightShadowRenderer(assets, 1024, 3);
            shadows.setLight(sun);
            return shadows;
        }
    }
}
