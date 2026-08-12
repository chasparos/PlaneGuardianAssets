package com.planeguardian.assets.assetgenerator.sample.export;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Node;
import java.io.IOException;
import java.nio.file.Path;

/** Export boundary for the small sample asset generator. */
public final class SampleAssetExporter {
    private SampleAssetExporter() {}

    public static void write(Node scene, Path output) throws IOException {
        BinaryExporter.getInstance().save(scene, output.toFile());
    }
}
