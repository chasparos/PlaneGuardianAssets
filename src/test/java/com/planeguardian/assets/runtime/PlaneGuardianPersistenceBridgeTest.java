package com.planeguardian.assets.runtime;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaneGuardianPersistenceBridgeTest {
    @Test
    void persistsCompletedAssetAndLoadsScenegraph() throws Exception {
        Node completedAsset = new Node("completed-tree");
        completedAsset.attachChild(new Geometry("trunk", new Box(1, 2, 1)));

        var path = Files.createTempFile("completed-tree", ".j3o");
        PlaneGuardianPersistenceBridge.persist(completedAsset, path);
        Node loaded = (Node) PlaneGuardianPersistenceBridge.loadAsset(new DesktopAssetManager(true), path);

        assertEquals("completed-tree", loaded.getName());
        assertEquals(1, loaded.getChildren().size());
    }
}
