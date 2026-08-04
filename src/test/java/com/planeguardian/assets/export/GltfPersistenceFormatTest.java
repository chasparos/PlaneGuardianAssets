package com.planeguardian.assets.export;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.scene.Node;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GltfPersistenceFormatTest {
    @Test
    void loadsExportedGltfIntoJmeScenegraph() throws Exception {
        var exportedGltf = Files.createTempFile("completed-asset", ".gltf");
        Files.writeString(exportedGltf, """
                {
                  "asset":{"version":"2.0"},
                  "buffers":[{"uri":"data:application/octet-stream;base64,AAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAAAAABAAIA","byteLength":42}],
                  "bufferViews":[{"buffer":0,"byteOffset":0,"byteLength":36,"target":34962},{"buffer":0,"byteOffset":36,"byteLength":6,"target":34963}],
                  "accessors":[
                    {"bufferView":0,"componentType":5126,"count":3,"type":"VEC3","min":[0,0,0],"max":[1,1,0]},
                    {"bufferView":1,"componentType":5123,"count":3,"type":"SCALAR"}
                  ],
                  "meshes":[{"name":"completed-asset","primitives":[{"attributes":{"POSITION":0},"indices":1}]}],
                  "nodes":[{"mesh":0,"name":"completed-tree"}],
                  "scenes":[{"nodes":[0]}],
                  "scene":0
                }
                """);

        Node scenegraph = GltfPersistenceFormat.loadAsset(new DesktopAssetManager(true), exportedGltf);

        assertEquals(1, scenegraph.getChildren().size());
        assertEquals("completed-tree", scenegraph.getChild(0).getName());
    }

    @Test
    void rejectsNonGltfPersistenceInput() throws Exception {
        var nonGltf = Files.createTempFile("not-an-export", ".j3o");
        assertThrows(java.io.IOException.class,
                () -> GltfPersistenceFormat.loadAsset(new DesktopAssetManager(true), nonGltf));
    }
}
