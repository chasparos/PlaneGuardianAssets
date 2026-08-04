# Procedural Asset Generation

This documentation defines PlaneGuardianAssets' deterministic semantic asset
pipeline and its first reference generator, the Deciduous Great Tree. The game
design repository remains authoritative for gameplay, lore, and production-art
meaning; this repository owns implementation contracts and generator details.

## Reading order

1. [Foundations and runtime architecture](01-foundations-and-runtime.md)
2. [Asset package contract](02-asset-package-contract.md)
3. [Semantic inputs and visual contracts](03-semantic-visual-contracts.md)
4. [Deciduous Great Tree generator](04-deciduous-great-tree.md)
5. [Rendering and performance](05-rendering-and-performance.md)
6. [Generator algorithm and validation](06-algorithm-and-validation.md)
7. [Implementation roadmap and decisions](07-implementation-roadmap.md)

## Authority boundary

- These documents specify asset-runtime interfaces, deterministic generation,
  geometry, materials, LOD, packaging, and validation.
- `PlaneGuardianGDD` specifies semantic-wheel meaning, Land/attachment visual
  ownership, attachment slots, and overall Main Stage direction.
- [GDD references](../gdd-references.md) records the exact upstream sources used
  when reconciling this design.

The former root-level `PROCEDURAL_ASSET_GENERATION.md` remains as a compatibility
pointer so existing links lead readers here.

The implementation architecture is defined separately in
[Reusable Generation Platform](../architecture/generation-platform.md).
