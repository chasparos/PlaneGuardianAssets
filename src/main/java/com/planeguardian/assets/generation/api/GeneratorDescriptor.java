package com.planeguardian.assets.generation.api;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Registry metadata required by a generic authoring workbench. */
public record GeneratorDescriptor(StableId generatorId, ContractVersion generatorVersion, StableId assetFamily,
                                  String displayName, List<Parameter> parameters, List<Preset> presets,
                                  Set<StableId> capabilities, Set<String> previewFormats, Set<String> exportFormats,
                                  Set<StableId> roles, Set<StableId> sockets, Set<StableId> semanticDerivedParameters) {
    public GeneratorDescriptor {
        Objects.requireNonNull(generatorId); Objects.requireNonNull(generatorVersion); Objects.requireNonNull(assetFamily);
        requireText(displayName, "displayName"); parameters = List.copyOf(parameters); presets = List.copyOf(presets);
        capabilities = Set.copyOf(capabilities); previewFormats = Set.copyOf(previewFormats);
        exportFormats = Set.copyOf(exportFormats); roles = Set.copyOf(roles); sockets = Set.copyOf(sockets);
        semanticDerivedParameters = Set.copyOf(semanticDerivedParameters);
        if (!parameters.stream().map(Parameter::id).collect(java.util.stream.Collectors.toSet()).containsAll(semanticDerivedParameters))
            throw new IllegalArgumentException("Semantic-derived parameter is not declared");
        if (parameters.stream().map(Parameter::id).distinct().count() != parameters.size()) throw new IllegalArgumentException("Duplicate parameter ID");
        if (presets.stream().map(Preset::id).distinct().count() != presets.size()) throw new IllegalArgumentException("Duplicate preset ID");
    }
    private static void requireText(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank"); }

    public record Parameter(StableId id, String displayName, String valueType, String defaultValue,
                            String allowedValues, boolean advanced) {
        public Parameter { Objects.requireNonNull(id); requireText(displayName, "displayName"); requireText(valueType, "valueType"); Objects.requireNonNull(defaultValue); requireText(allowedValues, "allowedValues"); }
    }
    public record Preset(StableId id, String displayName, java.util.SortedMap<String, String> parameterValues) {
        public Preset { Objects.requireNonNull(id); requireText(displayName, "displayName"); parameterValues = java.util.Collections.unmodifiableSortedMap(new java.util.TreeMap<>(parameterValues)); }
    }
}
