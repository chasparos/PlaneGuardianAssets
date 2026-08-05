package com.planeguardian.assets.runtime;

import com.planeguardian.assets.generation.api.GenerationDiagnostic;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.ResolvedVisualProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Default loaded-asset facade dispatching only to installed capabilities. */
public final class ComposableLoadedAsset<R> implements LoadedAsset<R> {
    private final StableId assetId;
    private final R root;
    private final List<RuntimeCapability> capabilities;

    public ComposableLoadedAsset(StableId assetId, R root, Collection<? extends RuntimeCapability> capabilities) {
        this.assetId = Objects.requireNonNull(assetId); this.root = Objects.requireNonNull(root);
        this.capabilities = List.copyOf(capabilities);
        long unique = this.capabilities.stream().map(RuntimeCapability::capabilityId).distinct().count();
        if (unique != this.capabilities.size()) throw new IllegalArgumentException("Duplicate runtime capability ID");
    }
    @Override public StableId assetId() { return assetId; }
    @Override public R root() { return root; }
    @Override public Collection<RuntimeCapability> capabilities() { return capabilities; }

    @Override public SemanticApplicationResult applySemantics(ResolvedVisualProfile profile, SemanticContext context) {
        Objects.requireNonNull(profile); Objects.requireNonNull(context);
        boolean changed = false; List<GenerationDiagnostic> diagnostics = new ArrayList<>();
        for (RuntimeCapability capability : capabilities) {
            if (capability instanceof SemanticReactive semantic) {
                SemanticApplicationResult result = semantic.applySemantics(profile, context);
                changed |= result.changed(); diagnostics.addAll(result.diagnostics());
            }
        }
        return new SemanticApplicationResult(changed, diagnostics);
    }
}
