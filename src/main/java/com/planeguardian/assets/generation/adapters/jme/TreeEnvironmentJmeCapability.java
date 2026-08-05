package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.runtime.EnvironmentReactive;
import com.planeguardian.assets.runtime.EnvironmentState;

/** Updates tree wind shader inputs from mutable scene environment state. */
public final class TreeEnvironmentJmeCapability implements EnvironmentReactive {
    private final Node root;
    public TreeEnvironmentJmeCapability(Node root) { this.root = java.util.Objects.requireNonNull(root); }
    @Override public StableId capabilityId() { return new StableId("runtime.tree.environment"); }
    @Override public void update(double deltaSeconds, EnvironmentState environment) {
        visit(root, spatial -> {
            if (spatial instanceof Geometry geometry && geometry.getMaterial() != null
                    && geometry.getMaterial().getMaterialDef().getMaterialParam("WindDirection") != null) {
                geometry.getMaterial().setVector3("WindDirection", new Vector3f((float) environment.windDirection().x(),
                        (float) environment.windDirection().y(), (float) environment.windDirection().z()));
                geometry.getMaterial().setFloat("WindIntensity", (float) environment.windIntensity());
                geometry.getMaterial().setFloat("WindTime", (float) environment.elapsedSeconds());
            }
        });
    }
    private static void visit(Spatial spatial, java.util.function.Consumer<Spatial> action) {
        action.accept(spatial); if (spatial instanceof Node node) node.getChildren().forEach(child -> visit(child, action));
    }
}
