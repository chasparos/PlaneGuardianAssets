package com.planeguardian.assets.generation.geometry.tube;

import com.planeguardian.assets.generation.api.Vector3;
import com.planeguardian.assets.generation.curves.ParametricCurve3;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.DoubleUnaryOperator;

public record SplineTubeRequest(
        ParametricCurve3 centerline,
        int ringCount,
        int verticesPerRing,
        int arcLengthSamples,
        Vector3 initialNormalHint,
        RadiusProfile radiusProfile,
        CrossSectionProfile crossSectionProfile,
        DoubleUnaryOperator rollRadiansByArcFraction,
        Set<String> semanticGroups) {

    public SplineTubeRequest {
        Objects.requireNonNull(centerline, "centerline");
        if (ringCount < 2) throw new IllegalArgumentException("A tube needs at least two rings");
        if (verticesPerRing < 8) throw new IllegalArgumentException("Ordinary tube rings need at least eight vertices");
        if (arcLengthSamples < ringCount) throw new IllegalArgumentException("Arc-length samples must cover every ring");
        Objects.requireNonNull(initialNormalHint, "initialNormalHint");
        Objects.requireNonNull(radiusProfile, "radiusProfile");
        Objects.requireNonNull(crossSectionProfile, "crossSectionProfile");
        Objects.requireNonNull(rollRadiansByArcFraction, "rollRadiansByArcFraction");
        Objects.requireNonNull(semanticGroups, "semanticGroups");
        TreeSet<String> groups = new TreeSet<>();
        semanticGroups.forEach(group -> {
            if (group == null || group.isBlank()) throw new IllegalArgumentException("Semantic group must not be blank");
            groups.add(group);
        });
        semanticGroups = Collections.unmodifiableSet(groups);
    }
}
