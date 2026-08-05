package com.planeguardian.assets.runtime;

import com.planeguardian.assets.generation.api.GenerationDiagnostic;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Generic receiving-end facade over an engine asset and its optional behaviors. */
public interface LoadedAsset<R> {
    StableId assetId();
    R root();
    Collection<RuntimeCapability> capabilities();
    SemanticApplicationResult applySemantics(ResolvedVisualProfile profile, SemanticContext context);

    default void update(double deltaSeconds, EnvironmentState environment) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0) throw new IllegalArgumentException("deltaSeconds must be finite and non-negative");
        Objects.requireNonNull(environment, "environment");
        capabilities().stream().filter(EnvironmentReactive.class::isInstance).map(EnvironmentReactive.class::cast)
                .forEach(capability -> capability.update(deltaSeconds, environment));
    }

    record SemanticContext(ResolvedVisualProfile hostProfile, double hostInfluence) {
        public SemanticContext {
            if (!Double.isFinite(hostInfluence) || hostInfluence < 0 || hostInfluence > 1) throw new IllegalArgumentException("hostInfluence must be in [0, 1]");
            if (hostInfluence > 0) Objects.requireNonNull(hostProfile, "hostProfile");
        }
        public static SemanticContext intrinsicOnly() { return new SemanticContext(null, 0); }
    }

    record SemanticApplicationResult(boolean changed, List<GenerationDiagnostic> diagnostics) {
        public SemanticApplicationResult { diagnostics = List.copyOf(diagnostics); }
    }
}
