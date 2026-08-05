# Geometry Construction Toolkit

## Purpose

This is the working manual for agents and developers constructing procedural
geometry in PlaneGuardianAssets. The default is to compose this toolkit. Do not
create an asset-specific mesh algorithm when an existing curve, patch, tube,
ring, or constructive operation can express the same work.

Create a new shared primitive only when composition cannot preserve the needed
topology, semantics, determinism, or performance. Keep it engine-neutral, place
it under the shared `generation` packages, and prove it with a non-asset-specific
fixture before an asset generator depends on it.

## Construction pipeline

1. Describe paths with `ParametricCurve3`: cubic Hermite, Catmull-Rom, or NURBS.
2. Use `ArcLengthTable` for distance-based sampling and
   `ParallelTransportFrames` for stable orientation and explicit twist.
3. Generate tubes with `SplineTubeGenerator`; keep at least eight vertices per
   ring and retain the returned `TubeEnd` boundaries.
4. Describe sheet-like forms with `ParametricSurface3` or `NurbsSurface`, then
   use `QuadSurfacePatchGenerator` for a UV-mapped quad grid.
5. Edit topology only inside `ProtoMeshEditTransaction` using the operations
   below. Preview or commit validates the result; rollback discards the draft.
6. Run triangulation and `MeshSurfaceProcessor` only after topology is final.
7. Convert to jME or glTF using adapters. Never author against a jME `Mesh`.

## ProtoMesh rules

- Snapshots are immutable. Begin an edit with `ProtoMeshEditTransaction.begin`.
- IDs are stable within a snapshot lineage. Removed IDs are retired, never reused.
- Faces are polygons with per-corner attributes, allowing UV and normal seams.
- Preserve semantic groups when replacing faces.
- Quad-first means construction topology uses rings, loops and patches where
  practical; it does not forbid controlled poles or final triangulation.
- A transaction that reports topology errors must not be committed.

## Available operations

| Need | Preferred tool | Important constraint |
| --- | --- | --- |
| Close an open ring | `RingCapOperation` / `RingFillOperation` / `RingCapOperation.pointCap` | Choose winding explicitly; `pointCap` emits a triangle fan to one apex. |
| Join equal rings | `RingBridgeOperation` | Corresponding vertex counts and phase. |
| Change loop resolution | `UnequalRingBridgeOperation` | Only reviewed 4↔8, 8↔12 and 8↔16 patterns. |
| Extend a tube end | `EndCollarOperation` | Uses the tube end frame and ring. |
| Add a face collar | `FaceInsetOperation` then `FaceExtrudeOperation` | Inset is centroid-directed and planar. |
| Create minimum branch outlet | `BranchJunctionOperation` | Parent quad to phase-aligned 8-vertex child ring. |
| Subdivide an edge | `EdgeSplitOperation` | Rewrites incident polygons; does not remesh them. |
| Merge seam vertices | `VertexWeldOperation` | Coincident, non-adjacent vertices only; collapse is rejected. |
| Create a spline tube | `SplineTubeGenerator` | Arc-spaced rings, quad sides, explicit radius/roll profiles. |
| Create a surface patch | `QuadSurfacePatchGenerator` | Fixed resolution; returns four ordered boundaries. |

## Choosing or adding an operation

Before adding code, search the shared geometry packages and attempt composition.
For example, a branch socket is inset + extrusion + a reviewed unequal-ring
bridge, not a tree-specific mesh builder. A root is a spline tube with different
profiles and placement rules, not a second tube implementation.

A new operation is justified when at least one of these is true:

- the required topology cannot be represented by current operations;
- composition would lose stable IDs, corner attributes, or semantic groups;
- a recurring pattern needs one reviewed invariant-preserving implementation;
- measured performance makes the composed route unsuitable.

When adding one, document ordering/winding, boundaries, attribute behavior,
identity retirement, failure conditions, and whether it may emit non-quads.
Add deterministic topology tests and a fixture unrelated to the requesting
asset family.

## Known boundaries

The toolkit deliberately does not yet provide arbitrary remeshing, boolean mesh
union, adaptive patch tessellation, automatic patch stitching, or general
three-port pair-of-pants junctions. Do not implement these incidentally inside
an asset generator. Check `.steadyarc/deferred-issues.md` and promote a bounded
shared-library item if one becomes necessary.

## Package map

- `generation.api`: engine-neutral values, IDs, requests and outputs.
- `generation.curves`: curves, arc length and transported frames.
- `generation.surfaces`: parametric and NURBS surfaces.
- `generation.topology`: ProtoMesh, identities, transactions and validation.
- `generation.geometry.tube`: spline tube profiles and generation.
- `generation.geometry.patch`: quad surface-patch generation.
- `generation.geometry.operations`: constructive and transition operations.
- `generation.triangulation`: controlled render triangulation.
- `generation.surface`: normals, tangents and render-mesh processing.
- `generation.adapters`: jME and glTF boundary adapters.

The authoritative behavior remains source and tests. This manual explains how
to select and compose that behavior.
