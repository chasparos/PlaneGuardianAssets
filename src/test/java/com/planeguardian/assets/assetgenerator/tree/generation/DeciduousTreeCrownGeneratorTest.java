package com.planeguardian.assets.assetgenerator.tree.generation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeciduousTreeCrownGeneratorTest {
    @Test
    void compositionProducesDeterministicEngineNeutralFoliageShells() {
        TreeStructure structure = TreeStructure.defaults();
        TreeComposition composition = structure.composition();

        TreeCrownProduct first = DeciduousTreeStructureGenerator.generateCrown(structure, composition, 41);
        TreeCrownProduct repeated = DeciduousTreeStructureGenerator.generateCrown(structure, composition, 41);

        assertEquals(first.fingerprint(), repeated.fingerprint());
        assertEquals(6, first.parts().size());
        assertTrue(first.parts().entrySet().stream().allMatch(entry ->
                entry.getKey().value().equals(entry.getValue().id().value())
                        && entry.getValue().role().value().equals("tree.foliage")
                        && entry.getValue().mesh().isValid()
                        && entry.getValue().renderMesh().hasTextureCoordinates()
                        && entry.getValue().renderMesh().hasTangents()));
    }

    @Test
    void leaflessCoverageProducesNoCrownParts() {
        TreeCrownProduct crown = DeciduousTreeCrownGenerator.generate(TreeStructure.defaults(),
                new TreeCrownSettings(0, 8, .62, .38, .62, 4, 8), 4);

        assertTrue(crown.parts().isEmpty());
    }
}
