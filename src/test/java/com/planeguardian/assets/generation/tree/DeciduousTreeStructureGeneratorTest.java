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

    @Test
    void compositionProducesStableNamedEngineNeutralComponents() {
        TreeStructure structure = TreeStructure.defaults();
        TreeComposition composition = structure.composition();

        TreeStructuralProduct first = DeciduousTreeStructureGenerator.generate(structure, composition, 43);
        TreeStructuralProduct second = DeciduousTreeStructureGenerator.generate(structure, composition, 43);

        assertEquals(first.fingerprint(), second.fingerprint());
        assertEquals("e6b112bb8e657629a09bf272e10f4ae659a7670ee9160f8bbc26a8df8bd118fa",
                first.fingerprint().hex());
        assertEquals(2 + composition.branchLevels().get(0).maximumChildren() + composition.roots().rootCount(),
                first.parts().size());
        assertTrue(first.parts().keySet().stream().anyMatch(id -> id.value().equals("tree.trunk")));
        assertTrue(first.parts().keySet().stream().anyMatch(id -> id.value().equals("tree.hollow")));
        assertTrue(first.parts().values().stream().allMatch(part -> part.mesh().isValid()));
        assertTrue(first.parts().values().stream().allMatch(part -> part.renderMesh().hasTextureCoordinates()
                && part.renderMesh().hasTangents()
                && part.renderMesh().triangleCount() == part.mesh().faces().size() * 2));
        assertTrue(first.parts().values().stream().filter(part -> part.role().value().equals("tree.root"))
                .allMatch(TreeStructuralPart::hostContact));
        assertEquals(first.parts().size() + 1, first.sockets().size());
        assertTrue(first.sockets().stream().anyMatch(socket -> socket.socketId().value().equals("tree.socket.hollow")
                && socket.role().value().equals("tree.hollow")
                && socket.transform().translation().y() > 0));
    }

    @Test
    void compositionRejectsUnboundedOrUnsupportedControls() {
        assertThrows(IllegalArgumentException.class, () -> new TreeBranchLevel(0, 0, 1, .2, .1, .3, 8, 8));
        assertThrows(IllegalArgumentException.class, () -> new TreeRootSettings(3, 1, .5, .5, 8, 8));
        assertThrows(IllegalArgumentException.class, () -> new TreeComposition(2,
                java.util.List.of(new TreeBranchLevel(1, 0, 1, .2, .1, .3, 8, 8)),
                new TreeRootSettings(4, 1, .5, .5, 8, 8), new TreeLodSettings(0, 1), 8));
    }

    @Test
    void laterLevelsAttachToStableParentPathsAndRespectTheComponentBudget() {
        TreeStructure structure = TreeStructure.defaults();
        TreeComposition recursive = new TreeComposition(1,
                java.util.List.of(
                        new TreeBranchLevel(2, .35, .75, .35, .12, .4, 8, 8),
                        new TreeBranchLevel(2, .5, .9, .25, .45, .25, 8, 8)),
                new TreeRootSettings(4, 1.4, .3, .5, 8, 8),
                new TreeLodSettings(0, 2), 7);

        TreeStructuralProduct first = DeciduousTreeStructureGenerator.generate(structure, recursive, 99);
        TreeStructuralProduct repeated = DeciduousTreeStructureGenerator.generate(structure, recursive, 99);

        assertEquals(first.fingerprint(), repeated.fingerprint());
        assertEquals(7, first.parts().size());
        assertTrue(first.parts().containsKey(new com.planeguardian.assets.generation.api.StableId(
                "tree.branch.1.trunk.0.0")));
        assertTrue(first.parts().values().stream().allMatch(part -> part.mesh().isValid()));
    }

    @Test
    void hollowIsAStableBudgetedSuffixAndDoesNotDisplaceStructuralComponents() {
        TreeStructure structure = TreeStructure.defaults();
        TreeComposition constrained = new TreeComposition(1,
                java.util.List.of(new TreeBranchLevel(2, .3, .8, .3, .1, .4, 8, 8)),
                new TreeRootSettings(4, 1.4, .3, .5, 8, 8), new TreeLodSettings(0, 1), 3);

        TreeStructuralProduct product = DeciduousTreeStructureGenerator.generate(structure, constrained, 100);

        assertEquals(3, product.parts().size());
        assertTrue(product.parts().containsKey(new com.planeguardian.assets.generation.api.StableId("tree.trunk")));
        assertTrue(product.parts().containsKey(new com.planeguardian.assets.generation.api.StableId("tree.branch.0.trunk.0")));
        assertTrue(product.parts().containsKey(new com.planeguardian.assets.generation.api.StableId("tree.branch.0.trunk.1")));
        assertTrue(product.parts().keySet().stream().noneMatch(id -> id.value().equals("tree.hollow")));
    }
}
