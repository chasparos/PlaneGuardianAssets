package com.planeguardian.assets.assetgenerator.tree.export;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeWindShaderSourceTest {
    private static final List<String> UNIFORMS = List.of("WindWeight", "WindPhase", "WindFrequency",
            "WindDirection", "WindIntensity", "WindTime");

    @Test
    void mainAndShadowPassDeclareEveryWindMaterialUniformTheyUse() throws Exception {
        for (String resource : List.of("MatDefs/Tree/TreeWindPbr.vert", "MatDefs/Tree/TreeWindPreShadow.vert")) {
            String source;
            try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
                source = new String(java.util.Objects.requireNonNull(stream, resource).readAllBytes(), StandardCharsets.UTF_8);
            }
            for (String uniform : UNIFORMS) {
                assertTrue(source.matches("(?s).*uniform\\s+(float|vec3)\\s+m_" + uniform + "\\s*;.*"),
                        () -> resource + " does not declare m_" + uniform);
                assertTrue(source.contains("m_" + uniform), () -> resource + " does not use m_" + uniform);
            }
        }
    }
}
