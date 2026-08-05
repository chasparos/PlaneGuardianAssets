package com.planeguardian.assets.generation.authoring;

import com.planeguardian.assets.generation.api.GeneratorDescriptor;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Applies resolved semantic values unless the user explicitly overrides them. */
public final class ParameterPrecedence {
    private ParameterPrecedence() {}
    public static Map<String, Double> resolve(GeneratorDescriptor descriptor, Map<String, Double> direct,
                                               ResolvedVisualProfile resolved, Set<String> explicitOverrides) {
        TreeMap<String, Double> result = new TreeMap<>(direct);
        descriptor.semanticDerivedParameters().forEach(id -> {
            Double semantic = resolved.channels().get(id.value());
            if (semantic != null && !explicitOverrides.contains(id.value())) result.put(id.value(), semantic);
        });
        return java.util.Collections.unmodifiableMap(result);
    }
}
