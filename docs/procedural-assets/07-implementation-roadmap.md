# Implementation Roadmap and Decisions

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 13. Initial implementation milestones

These design milestones are realized through the repository's active
**First Asset Generator POC** roadmap. Shared topology, constructive geometry,
texture entities, and VFX plugins are established before tree-specific
composition; see [Reusable Generation Platform](../architecture/generation-platform.md).

### Milestone 1: contracts

- Define asset SPI records and versioning.
- Define semantic wheel DTOs.
- Define `LandVisualProfile` and `DeciduousTreeParameters`.
- Implement contribution tracing and deterministic substreams.

### Milestone 2: structural tree

- Trunk spline and ring extrusion.
- Branch graph and branch splines.
- Root flare and major roots.
- Normals, tangents, UVs, bounds, and one PBR bark material.

### Milestone 3: crown

- Crown density field.
- None, sparse, and full foliage states.
- Cluster-shell foliage and initial LODs.
- Semantic foliage palette and wind response.

### Milestone 4: optional features

- Local hollow-trunk topology.
- Moss masks and hanging moss.
- Vine splines.
- Flower and fruit clusters.
- Feature admission through generation budget.

### Milestone 5: package integration

- Export metadata into `asset_index.json` and glTF `extras`.
- Package data library and runtime JAR together.
- Register the generator provider through `ServiceLoader`.
- Add caching, golden tests, and reference renders.

## 14. Deferred decisions

- Exact binary format of `planeguardian-assets.pglib`.
- Whether runtime geometry is serialized into a persistent client cache.
- PBR shader integration/fork strategy for the current jME 3.7 baseline.
- Final triangle and draw-call budgets after main-stage profiling.
- Final perceptual palette space and gamut-mapping implementation.
- Universe-generated divine and infernal motif interfaces.
- Whether a constrained declarative parameter graph is needed.
- Whether signed generator bundles must be hot-loadable without restarting the client.

## 15. Foundational decision summary

The Great Tree is the reference implementation for the entire procedural asset system. It must demonstrate that one stable semantic profile can govern geometry, materials, and effects without placing lore interpretation inside individual shaders.

The asset library remains data-oriented. Trusted generator implementations are shipped in a paired runtime JAR and initially linked as a normal client dependency. `ServiceLoader` registers implementations. Dynamic class loading is deferred until downloadable, signed generator bundles provide a real benefit. Byte Buddy and embedded JavaScript are not part of the initial architecture.

The success criterion is not the number of generated features. It is whether a small set of coherent parameters can produce readable, performant, deterministic silhouettes across radically different card identities while still looking like members of the same PlaneGuardian visual system.

## 16. Technical references

- [Java `ServiceLoader` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)
- [Java `URLClassLoader` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URLClassLoader.html)
- [Byte Buddy project documentation](https://bytebuddy.net/)
- [glTF 2.0 specification](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html)
- [jMonkeyEngine shader documentation](https://wiki.jmonkeyengine.org/docs/3.7/core/shader/jme3_shaders.html)
- [jMonkeyEngine material-definition documentation](https://wiki.jmonkeyengine.org/docs/3.7/core/material/material_definitions.html)
