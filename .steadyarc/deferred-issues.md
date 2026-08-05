# Deferred Issues

These require explicit promotion before implementation.

## Package and runtime

- Exact packed asset-library format and patching strategy.
- Persistent generated mesh/material cache format.
- Whether signed generator bundles need hot loading without restart.
- Whether repeated adapter rules justify a constrained declarative graph.

## Rendering and budgets

- Final perceptual palette space and gamut mapping.
- Exact jME PBR integration and shared semantic parameters.
- Exact per-family triangle, draw-call, generation-time, and memory budgets,
  established from representative 10-island near and 100-island overview scenes.
- **Want to have:** evaluate a reusable compute-shader particle system for large
  effect counts, GPU simulation, and indirect rendering. Promote only after the
  ordinary shared VFX plugin/runtime path is working and profiling shows a
  material benefit; retain a compatible fallback for unsupported hardware.

## Cross-repository contract

- Final SPI boundary and release coupling between runtime, exported data, and client.

## Blender authoring interchange

- Add an optional lossless ProtoMesh-to-Blender authoring path that preserves
  quad loops, polygon faces, semantic groups, sockets, and possibly spline
  control data. Likely approaches are a Blender importer/add-on, a generated
  Blender Python reconstruction script, or headless `.blend` production.
- Normal GLB/glTF export remains the triangulated runtime path. This deferred
  authoring feature must not delay the initial glTF adapter or POC generator.

## Legacy asset-library reconciliation

- **Partially resolved:** the legacy library is explicitly scoped out of the
  `PlaneGuardianAssetInterface` boundary — it always writes an empty/non-provider
  `generatorId` and a valid fallback GLB, so `RuntimePackageResolver` always
  falls back rather than resolving a trusted provider (see roadmap item 16 and
  `docs/architecture/generation-platform.md` "Known drift against this
  boundary"). Full reconciliation or retirement of the manual per-asset
  shader-ref workflow (`com.planeguardian.assets.db`,
  `com.planeguardian.assets.model`, `com.planeguardian.assets.export.ExportManager`,
  `AssetIndexEntry`, `GltfExtrasInjector`'s `custom_shader_id`/`shader_parameters`
  extras shape) against the generic generator/provider path
  (`pg.asset-index/1`, `PackageManifestWriter`, `RuntimePackageResolver`,
  `RuntimeAssetProvider`) remains deferred. Both still write `asset_index.json`
  independently; do not assume they are already unified beyond the fallback
  scoping above.

## Geometry toolkit extensions

- Add snapshot append/merge with explicit transforms and deterministic ID
  remapping when asset composition requires combining independently generated
  ProtoMeshes. Until promoted, compose within one builder or retain components.
- Add seam-aware UV and scalar-layer policies for collars, unequal transitions,
  and junctions. Current topology is valid, but production materials will need
  deliberate unwrap rules rather than anonymous empty corner channels.
- Add arbitrary loop resampling only if reviewed fixed transitions cannot cover
  a real asset. It must not become a heuristic remesher hidden in a generator.
- Add adaptive spline-patch tessellation, neighboring-patch stitching, and
  shared seam-normal policy when fixed grids produce a demonstrated silhouette
  or budget problem.
- Add further reviewed junction patterns, including higher-resolution parent
  faces or true three-port pair-of-pants topology, only if bounded hidden tube
  overlap fails at gameplay scale.
- Add controlled vertex repositioning, edge collapse, loop insertion, and face
  subdivision as shared transactional operations when an asset proves the need.
- Evaluate stronger geometric predicates for highly non-planar or nearly
  coincident polygons before relying on ProtoMesh validation for arbitrary
  imported geometry; current validation targets generated, well-conditioned
  construction topology.
