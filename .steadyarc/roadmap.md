# Engineering Roadmap

## Active arc: First Asset Generator POC

The Deciduous Great Tree proves a reusable asset-generation platform. Success
means the repository gains general geometry, texture, material, and VFX
capabilities that later generators can compose—not merely one convincing tree.

### Completed foundation

1. **Steady Arc repository bootstrap** — complete
2. **Design consolidation and authority map** — complete

### POC implementation passes

3. **Platform contracts and module boundaries** — complete
   - Immutable generation request/result, version, render-tier, role, socket,
     diagnostics, contribution-trace, and fingerprint contracts.
   - Package boundaries that prevent UI, JDBC, jME, and asset-family logic from
     leaking into reusable generation code.
   - Named deterministic random substreams and numeric quantization.

4. **ProtoMesh topology kernel** — complete
   - Engine-neutral editable vertices, edges, loops, polygon faces, per-corner
     attributes, semantic groups, adjacency, deterministic iteration, snapshots,
     and topology validation.
   - Explicit jME and glTF conversion adapters; triangulation occurs at boundaries.
   - Initial stable-ID builder, immutable snapshot, seam attributes, adjacency,
     boundary discovery, and manifold/winding validation — complete.
   - Quantized topology fingerprints, degenerate/coincident/non-planar checks,
     deterministic seam-preserving triangulation, and minimal jME adapter — complete.
   - Explicit normal policies, deterministic tangent frames, common render mesh,
     glTF accessor-ready buffers/bounds, and jME tangent parity — complete.
   - Projected polygon self-intersection validation and a version-one end-to-end
     topology/render/export-buffer golden fixture — complete.
   - Destructive edit semantics are owned by pass 5 constructive operations;
     actual GLB container integration is owned by pass 10 packaging/runtime proof.

5. **Reusable curve and constructive geometry library** — complete
   - Hermite/Catmull-Rom splines, arc-length sampling, parallel-transport frames,
     spline patches, ring/loop builders, and loft/tube generation.
   - Extrude, inset, bridge, fill, cap, weld, split, and loop-resampling operations.
   - Standard tested quad-first topology transitions for increasing/decreasing loops.
   - Tube rings default to at least eight vertices; finer resolution is driven
     by silhouette and junction needs rather than close-up modeling detail.
   - Shared vector math, cubic Hermite, three Catmull–Rom parameterizations,
     positive-weight NURBS curves, sampled arc-length inversion, and twistable
     parallel-transport frames — complete.
   - Independent radius/roll/cross-section profiles, arc-spaced rings, quad-only
     open spline-tube lofting, seam-safe UVs, and collar-ready ends — complete.
   - Snapshot-preserving additive transactions, deterministic planar-UV caps,
     equal-ring quad bridges, and forward scaled collars — complete.
   - Explicit reversible quad-only 8↔12 and 8↔16 loop-transition patterns with
     evenly distributed extraordinary wedge quads — complete.
   - Transactional face removal with retired identities, deterministic ring
     fill, centroid planar inset, vector extrusion, and a generic composed
     inset/extrude collar proof - complete.
   - Conservative edge split with corner-channel interpolation and coincident
     non-adjacent vertex weld with collapse rejection - complete.
   - Normalized parametric surfaces, homogeneous tensor-product NURBS patches,
     and fixed-resolution UV-mapped quad-grid tessellation with four ordered
     boundaries - complete.
   - Reviewed parent-quad inset/collar through a phase-validated 4-to-8 quad-only
     transition into the minimum child tube ring - complete.
   - General boolean fusion, pair-of-pants junctions, and adaptive patch
     tessellation remain demand-driven rather than POC prerequisites.

6. **Reusable generated-resource systems**
   - Generated textures as versioned Asset Library entities with provenance,
     caching, channel/color-space metadata, preview, and export.
   - Reusable material recipes and VFX plugin contracts identified by stable IDs.
   - Initial shared texture/VFX providers needed by the Great Tree, without
     generator-private smoke, flame, foliage-mask, or bark implementations.
   - Immutable generated-texture request/descriptor contracts with canonical
     cache fingerprint, dimensions, seed, normalized parameters, fingerprinted
     sources, color space, and channel semantics - complete.
   - Next: pixel/artifact boundary and reusable material recipe contracts,
     followed by VFX plugin configuration and provider discovery.
   - Immutable tightly packed pixel payloads, safe fingerprinted artifact
     metadata, and canonical typed material recipes with fingerprint-pinned
     texture references - complete.
   - Next: reusable VFX plugin configuration and provider discovery, then the
     initial Great Tree texture/VFX providers.
   - Canonical fingerprinted VFX configurations with typed parameters, exact
     resource dependencies, socket attachments, and deterministic trusted
     ServiceLoader provider registry - complete.
   - Next: initial reusable foliage-mask/bark texture providers and a minimal
     shared effect provider used by the Great Tree proof.
   - Deterministic classpath-discovered painterly bark and foliage-mask raster
     providers plus a bounded discoverable pollen-motes VFX provider - complete.
   - Deterministic PNG preview/export, content-fingerprinted artifact metadata,
     and an atomic local generated-resource cache with corruption-as-miss
     verification — complete.
   - Material/VFX engine adapters remain deferred until item 7 needs them.

7. **Great Tree structural composition**
   - Tree-specific semantic parameters and branch graph compose shared spline,
     tube, root, transition, and surface-processing generators.
   - Trunk, branches, roots, hollow, stable roles/sockets, UVs, normals, tangents,
     bounds, and structural LODs; no reusable geometry logic in the tree package.
   - Recursive per-level controls for branch frequency, angles, attachment
     ranges, length/radius ratios, taper, curvature, pruning, and detail.
   - Controllable root flare, radial root frequency, terrain following, and
     partially exposed spline roots.
   - Publish every direct generator control in the versioned parameter schema;
     semantic wheels remain the editable source for resolver-derived values.
   - Deterministic engine-neutral trunk, recursively composed path-addressed
     branch/root components, root-contact metadata, structural LOD controls,
     sockets, aggregate topology fingerprinting, component budget enforcement,
     provisional bounded hollow surface, and finalized engine-neutral render
     products — complete except for the corrected hollow boundary. The current
     hollow is a disconnected tube; a shared wall-recess operation, configurable
     deterministic hollow presence, and rim/interior tests remain required.

8. **Crown, features, and semantic golden cases**
   - Shared foliage cluster/shell generation plus tree composition of crown,
     moss, vines, flowers, fruit, fungi, and feature budgets.
   - Bounded semantic adapter with contribution traces and fixed ordinary,
     opposed, centered, Death+Creation, and off-color-host cases.
   - Initial deterministic shared UV-mapped foliage shells, bounded tree crown
     controls, crown composition, and semantic crown/feature suitability with
     contribution traces and golden cases — complete.
   - Next: use resolved suitability for bounded moss, vine, flower, fruit, and
     fungal feature admission; preserve those feature groups as independent
     semantic surfaces.

9. **PBR, motion, reusable VFX, and preview validation**
   - Role-based PBR materials, host-contact masks, wind, shared effect plugins,
     gameplay-camera fixtures, shadows, and silhouette/LOD review.
   - Semantic-wheel editor with seed/tier controls, live regeneration, and
     read-only derived-profile/contribution inspection for visual validation.
   - AI-readable parameter manifest and validated proposal import: natural
     language such as “great oak” yields versioned parameter/wheel settings and
     rationale, never unvalidated generator commands or derived semantic fields.

10. **Package/runtime proof**
    - Versioned index and glTF extras, fallback assets, paired data/runtime
      artifacts, provider discovery, cache keys, compatibility checks, and a
      repeatable PlaneGuardian import smoke test.

### POC completion criteria

- Great Tree golden cases are deterministic, readable, and exportable.
- At least one non-tree fixture reuses ProtoMesh operations to demonstrate the
  geometry library is not tree-shaped.
- Generated texture identity and one reusable VFX plugin survive round-trip export.
- No explicit geometry generator depends on the Great Tree or another asset family.
- jME meshes are products of adapters, never the mutable authoring representation.

Current item: 8. Crown, features, and semantic golden cases.
