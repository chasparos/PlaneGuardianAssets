package com.planeguardian.assets.tools.generator;

import com.jme3.scene.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeciduousTreeGeneratorTest {

    @TempDir
    Path outputDirectory;

    @Test
    void generatesDeterministicLoadableTreeWithNamedMaterials() throws Exception {
        GenerationResult first = new DeciduousTreeGenerator().generate(outputDirectory.resolve("first"));
        GenerationResult second = new DeciduousTreeGenerator().generate(outputDirectory.resolve("second"));

        assertTrue(first.success(), first.message());
        assertTrue(second.success(), second.message());
        assertEquals("DeciduousTree_01", first.assetName());
        assertTrue(Files.isRegularFile(first.outputPath()));
        assertArrayEquals(Files.readAllBytes(first.outputPath()), Files.readAllBytes(second.outputPath()));

        Node tree = assertInstanceOf(Node.class,
                com.jme3.export.binary.BinaryImporter.getInstance().load(first.outputPath().toFile()));
        assertEquals("DeciduousTree", tree.getName());
        assertTrue(tree.getChildren().stream().anyMatch(child -> "Trunk".equals(child.getName())));
        assertTrue(tree.getChildren().stream().anyMatch(child -> child.getName().startsWith("Foliage_")));
    }
}
