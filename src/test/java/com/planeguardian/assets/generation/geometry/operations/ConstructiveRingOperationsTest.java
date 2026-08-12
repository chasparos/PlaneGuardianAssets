package com.planeguardian.assets.generation.geometry.operations;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.curves.CubicHermiteCurve;
import com.planeguardian.assets.generation.geometry.tube.CrossSectionProfile;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeGenerator;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeRequest;
import com.planeguardian.assets.generation.geometry.tube.SplineTubeResult;
import com.planeguardian.assets.generation.topology.ProtoMeshEditTransaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstructiveRingOperationsTest {
    @Test
    void transactionPreservesSourceIdsAndCanCloseBothTubeEnds() {
        SplineTubeResult tube = tube();
        long originalLastVertex = tube.mesh().vertices().lastKey().value();
        ProtoMeshEditTransaction transaction = ProtoMeshEditTransaction.begin(tube.mesh());
        transaction.apply(builder -> RingCapOperation.cap(builder, tube.start(), false, Set.of("cap.start")));
        transaction.apply(builder -> RingCapOperation.cap(builder, tube.end(), true, Set.of("cap.end")));

        var closed = transaction.commit();

        assertEquals(tube.mesh().vertices().size(), closed.vertices().size());
        assertEquals(originalLastVertex, closed.vertices().lastKey().value());
        assertEquals(tube.mesh().faces().size() + 2, closed.faces().size());
        assertEquals(0, closed.boundaryEdges().size());
        assertTrue(closed.isValid());
        assertEquals(8, tube.mesh().boundaryEdges().size() / 2);
    }

    @Test
    void rollbackAndCommittedCollectionsCannotMutateSourceSnapshot() {
        SplineTubeResult tube = tube();
        ProtoMeshEditTransaction transaction = ProtoMeshEditTransaction.begin(tube.mesh());
        transaction.apply(builder -> RingCapOperation.cap(builder, tube.end(), true, Set.of("temporary")));
        assertEquals(tube.mesh().faces().size() + 1, transaction.preview().faces().size());
        transaction.rollback();

        assertEquals(8, tube.mesh().faces().size());
        assertThrows(IllegalStateException.class, transaction::preview);
    }

    @Test
    void endCollarAddsScaledRingAndReusesQuadBridge() {
        SplineTubeResult tube = tube();
        ProtoMeshEditTransaction transaction = ProtoMeshEditTransaction.begin(tube.mesh());
        EndCollarOperation.CollarResult collar = transaction.apply(builder ->
                EndCollarOperation.extendForward(builder, tube.end(), 0.5, 1.25, Set.of("collar")));
        var mesh = transaction.commit();

        assertEquals(tube.mesh().vertices().size() + 8, mesh.vertices().size());
        assertEquals(tube.mesh().faces().size() + 8, mesh.faces().size());
        assertEquals(16, mesh.boundaryEdges().size());
        assertEquals(1.25, collar.end().nominalRadius());
        assertEquals(4.5, collar.end().frame().position().z(), 1.0e-12);
        assertEquals(8, collar.faces().size());
        assertTrue(mesh.faces().values().stream()
                .filter(face -> face.semanticGroups().contains("collar"))
                .allMatch(face -> face.loops().size() == 4));
    }

    @Test
    void bridgeRejectsMismatchedRingsBeforeAddingFaces() {
        SplineTubeResult tube = tube();
        ProtoMeshEditTransaction transaction = ProtoMeshEditTransaction.begin(tube.mesh());
        assertThrows(IllegalArgumentException.class, () -> transaction.apply(builder ->
                RingBridgeOperation.bridge(builder, tube.start().ringVertices(),
                        tube.end().ringVertices().subList(0, 7), Set.of())));
        assertEquals(tube.mesh().faces().size(), transaction.preview().faces().size());
    }

    @Test
    void capCarriesPlanarUvCoordinates() {
        SplineTubeResult tube = tube();
        ProtoMeshEditTransaction transaction = ProtoMeshEditTransaction.begin(tube.mesh());
        var capId = transaction.apply(builder -> RingCapOperation.cap(builder, tube.end(), true, Set.of("cap")));
        var mesh = transaction.commit();
        var cap = mesh.faces().get(capId);

        assertEquals(8, cap.loops().size());
        assertFalse(mesh.loops().get(cap.loops().get(0)).attributes().textureCoordinate().isEmpty());
        assertEquals(1.0, mesh.loops().get(cap.loops().get(0)).attributes().textureCoordinate().orElseThrow().x(), 1.0e-12);
        assertEquals(0.5, mesh.loops().get(cap.loops().get(0)).attributes().textureCoordinate().orElseThrow().y(), 1.0e-12);
    }

    @Test
    void pointCapClosesRingToAnApex() {
        SplineTubeResult tube = tube();
        ProtoMeshEditTransaction transaction = ProtoMeshEditTransaction.begin(tube.mesh());
        var faces = transaction.apply(builder -> RingCapOperation.pointCap(builder, tube.end(), true, 0.75, Set.of("point-cap")));
        var mesh = transaction.commit();

        assertEquals(8, faces.size());
        assertTrue(mesh.isValid());
        assertEquals(8, mesh.faces().values().stream().filter(face -> face.semanticGroups().contains("point-cap")).count());
        assertEquals(8, mesh.boundaryEdges().size());
    }

    private static SplineTubeResult tube() {
        CubicHermiteCurve centerline = new CubicHermiteCurve(
                Vector3.ZERO, new Vector3(0, 0, 4),
                new Vector3(0, 0, 4), new Vector3(0, 0, 4));
        return SplineTubeGenerator.generate(new SplineTubeRequest(
                centerline, 2, 8, 33, new Vector3(1, 0, 0),
                fraction -> 1, CrossSectionProfile.circular(), fraction -> 0, Set.of("tube")));
    }
}
