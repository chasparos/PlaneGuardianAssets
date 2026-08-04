package com.planeguardian.assets.generation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerationPackageBoundaryTest {
    private static final List<String> FORBIDDEN_DEPENDENCIES = List.of(
            "com.jme3", "javax.swing", "java.awt", "com.planeguardian.assets.db",
            "com.planeguardian.assets.tools", "com.planeguardian.assets.export",
            "com.planeguardian.assets.gltf");

    @Test
    void sharedGenerationCoreRemainsEngineAndApplicationNeutral() throws IOException {
        Path generationRoot = Path.of("src", "main", "java", "com", "planeguardian", "assets", "generation");
        for (String corePackage : List.of("api", "math", "curves", "geometry", "determinism", "topology", "triangulation", "surface", "performance")) {
            try (var paths = Files.walk(generationRoot.resolve(corePackage))) {
                for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                    String content = Files.readString(source);
                    for (String forbidden : FORBIDDEN_DEPENDENCIES) {
                        assertFalse(content.contains(forbidden),
                                () -> source + " must not depend on " + forbidden);
                    }
                }
            }
        }
    }
}
