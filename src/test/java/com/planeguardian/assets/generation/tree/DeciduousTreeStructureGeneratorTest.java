package com.planeguardian.assets.generation.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeciduousTreeStructureGeneratorTest {
    @Test
    void trunkIsDeterministicAndUsesOnlySharedQuadTubeTopology() {
        TreeStructure structure = TreeStructure.defaults();

        TreeStructuralProduct first = DeciduousTreeStructureGenerator.generateTrunk(structure, 42);
        TreeStructuralProduct second = DeciduousTreeStructureGenerator.generateTrunk(structure, 42);

        assertEquals(first.fingerprint(), second.fingerprint());
        assertEquals(structure.trunkRingCount() * structure.trunkVerticesPerRing(), first.trunk().vertices().size());
        assertEquals((structure.trunkRingCount() - 1) * structure.trunkVerticesPerRing(), first.trunk().faces().size());
        assertTrue(first.trunk().isValid());
        assertTrue(first.trunk().faces().values().stream()
                .allMatch(face -> face.loops().size() == 4 && face.semanticGroups().equals(java.util.Set.of("tree.trunk"))));
    }

    @Test
    void seedChangesCurvedTrunkButNotItsDeclaredTopology() {
        TreeStructure structure = TreeStructure.defaults();

        TreeStructuralProduct first = DeciduousTreeStructureGenerator.generateTrunk(structure, 1);
        TreeStructuralProduct second = DeciduousTreeStructureGenerator.generateTrunk(structure, 2);

        assertNotEquals(first.fingerprint(), second.fingerprint());
        assertEquals(first.trunk().faces().size(), second.trunk().faces().size());
    }

    @Test
    void structureRejectsUnsupportedOrInvalidControls() {
        assertThrows(IllegalArgumentException.class, () -> new TreeStructure(
                0, 0.45, 1, 0, 0, 0, 0, 16, 12));
        assertThrows(IllegalArgumentException.class, () -> new TreeStructure(
                12, 0.45, 1, 0, 0, 0, 0, 16, 7));
    }
}
