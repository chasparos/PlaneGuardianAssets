# Engineering Roadmap

## Foundation arc: First Asset Generator POC

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

8. **Crown, features, and semantic golden cases** — complete
   - Shared foliage cluster/shell generation plus tree composition of crown,
     moss, vines, flowers, fruit, fungi, and feature budgets.
   - Bounded semantic adapter with contribution traces and fixed ordinary,
     opposed, centered, Death+Creation, and off-color-host cases.
   - Initial deterministic shared UV-mapped foliage shells, bounded tree crown
     controls, crown composition, and semantic crown/feature suitability with
     contribution traces and golden cases — complete.
   - Bounded resolved suitability now covers independent moss, vine, flower,
     fruit, and fungal feature surfaces with inspectable contribution traces.
   - Deterministic, independently named engine-neutral feature-anchor admission
     now applies direct per-group and total budgets without renderer dependencies;
     a deterministic engine-neutral mesh consumer now derives UV/tangent-ready
     feature products without changing semantic resolution or admission identity.
     Renderer adapters remain deferred to item 9.

9. **PBR, motion, reusable VFX, and preview validation**
   - Role-based PBR materials, host-contact masks, wind, shared effect plugins,
     gameplay-camera fixtures, shadows, and silhouette/LOD review.
   - Semantic-wheel editor with seed/tier controls, live regeneration, and
     read-only derived-profile/contribution inspection for visual validation.
   - AI-readable parameter manifest and validated proposal import: natural
     language such as “great oak” yields versioned parameter/wheel settings and
     rationale, never unvalidated generator commands or derived semantic fields.
   - [x] Establish version-two direct presentation controls, AI-readable schema
    projection, validated proposal envelope, deterministic wind attributes, and
    engine-neutral gameplay preview fixture.
   - [x] Establish jME renderer-boundary adapters for fresh role-based PBR material
    instances and trusted pollen-mote configurations resolved through stable sockets.
   - [x] Bind cache-verified generated texture artifacts to role materials, with
    foliage coverage as an alpha-clipped semantic mask and host-contact as a PBR
    ambient-occlusion mask.
   - [x] Apply wind attributes in the jME shader path and expose runtime weather
    inputs without encoding weather into semantic state.
   - [x] Render fixed gameplay fixtures with shadows and perform silhouette, LOD,
    mip/alpha, PBR, host-contact, and emission review.
   - [x] Add the semantic-wheel desktop editor with seed/tier live regeneration and
    read-only resolved profile/contribution inspection.
   - [x] Complete adapter/UI/fixture regression coverage and item-9 validation.

10. **Package/runtime proof** — complete
    - [x] Define the versioned index and glTF extras contract, fallback assets,
      paired data/runtime artifacts, provider discovery, cache keys, and
      compatibility checks.
    - [x] Implement version-one `pg.asset-index/1` compatibility, root
      `planeGuardian` glTF extras, a SHA-256 package/runtime manifest, trusted
      classpath runtime-provider discovery, version-scoped cache keys, and safe
      fallback resolution.
    - [x] Prove that `GltfPersistenceFormat.loadAsset` turns a completed glTF
      export into a jME `Node`, and preview the loaded result with an outlined
      bounding box, neutral floor, off-black background, and three-point lighting.
    - [x] Run and publish compatible-JDK package/runtime regression evidence,
      including the PlaneGuardian import smoke test.

The final POC validation criteria have moved into the corrective arc below. The
foundation passes remain useful implementation evidence, but they do not by
themselves prove the intended generic authoring-to-runtime workflow.

## Active arc: Generic Asset Workflow Correction and Final POC Validation

The Deciduous Great Tree remains the reference asset, but it must exercise the
same registry, authoring session, semantic application, preview, and runtime
capability contracts that later island, rock, building, vegetation, and VFX
generators will use. Tree-specific UI and runtime shortcuts do not satisfy this
arc.

### 11. Correct the platform contracts and current-state documentation

- [x] Define the generic generator-provider contract: stable identity/version,
  asset family, parameter schema, defaults/presets, semantic adapter,
  capabilities, preview/export support, roles, and sockets.
- [x] Define a generic authoring-session contract joining direct parameters,
  seed, semantic source profile, resolved contribution trace, generated product,
  preview state, diagnostics, and export result.
- [x] Define the receiving-end loaded-asset contract for semantic application
  and time/environment updates without exposing generator-private types.
- [x] Define optional runtime capabilities so static assets do not implement
  meaningless per-frame behavior and reusable wind, weather, animation, and VFX
  controllers remain composable.
- [x] Correct the durable semantic-wheel documentation: a wheel value is a 2D
  direction/extremity coordinate plus independent Salience, with optional
  center relationship, focus, and secondary poles where supported.
- [x] Reconcile the active handoff, roadmap statuses, and validation evidence;
  archive closed assignment history instead of extending a progress journal.
- [x] Strengthen the agent entry path so issue identity, acceptance criteria,
  design excerpts, validation level, and current authority are restated before
  implementation when conversational history is unavailable.

### 12. Replace hard-wired generator discovery with a generic registry

- [x] Remove the Asset Generator tool's direct construction of the Great Tree
  implementation.
- [x] Discover generator providers through the versioned trusted registry and
  expose their declared metadata without asset-family conditionals.
- [x] Build parameter controls from provider schemas and group advanced controls
  without embedding tree parameter IDs in the generic workbench.
- [x] Add generic preset selection, reset-to-defaults, seed editing, and seed-only
  randomization.
- [x] Supply a usable, named Great Tree baseline preset such as `Great Oak` while
  retaining the complete versioned direct parameter set.
- [x] Prove registry behavior with the Great Tree and a small non-tree fixture.

### 13. Build reusable semantic-wheel editing components

- [x] Introduce an engine-neutral, deterministic wheel-coordinate value carrying
  point/direction, extremity, and Salience independently.
- [x] Preserve applicable relationship mode, focus, and secondary-pole data at
  the semantic boundary without forcing every editor or adapter to use them.
- [x] Implement a custom-painted Swing wheel component with a draggable point
  constrained to a labelled circle and a separate Salience slider.
- [x] Build a reusable semantic-profile editor from wheel metadata; it must not
  depend on the Great Tree or any other asset family.
- [x] Distinguish editable source values, resolver-derived values, and direct
  generator overrides in both the model and UI.
- [x] Define and test the precedence policy for manual direct edits versus
  semantic-derived parameter values.
- [x] Replace the detached, tree-specific scalar-slider diagnostics window with
  the reusable components integrated into the authoring workbench.

### 14. Establish generic loaded-asset semantics and runtime updates

- [x] Resolve raw wheel values centrally into a versioned visual profile before
  invoking an asset-family semantic adapter.
- [x] Apply the resolved profile through a generic loaded-asset facade to
  geometry variants, materials, and VFX without shaders interpreting the Lore
  Map independently.
- [x] Introduce a generic environment snapshot containing only runtime inputs
  such as elapsed time, wind, and weather; keep it outside semantic identity and
  deterministic generation fingerprints.
- [x] Dispatch per-frame updates only to installed runtime capabilities.
- [x] Adapt tree wind, materials, variants, sockets, and reusable effects through
  these contracts instead of tree-specific workbench calls.
- [x] Verify that a loaded asset can receive repeated semantic profiles and
  environment updates without resource leakage or stale derived state.

### 15. Integrate generation, semantics, and live visual preview

- [x] Place parameters, presets, semantic wheels, resolved contribution trace,
  diagnostics, and 3D preview in one generic generator workbench session.
- [x] Regenerate or reapply semantics after a bounded debounce and clearly show
  generation, validation, loading, and renderer failures.
- [x] Preview the same loaded-asset path intended for PlaneGuardian rather than a
  private editor-only tree representation.
- [x] Add an end-to-end smoke test covering default input, nonempty generated
  products, serialization, viewer-path loading, visible geometries, triangle
  counts, finite nonzero bounds, materials, and sockets.
- [x] Make camera framing, lighting, shadows, floor, background, and runtime wind
  sufficient for repeatable visual comparison.
- [ ] Perform human click tests of the Great Oak preset, seed variation, every
  wheel's point and Salience, centered high-Salience cases, direct overrides,
  render tiers, wind/weather response, generation, preview, and export.

### 16. Correct known structural debt

- [ ] Replace the disconnected hollow tube with a reusable wall-recess operation
  producing a deterministic aperture, rim, and recessed interior.
- [ ] Publish deterministic hollow presence and shape controls in the direct
  parameter schema.
- [ ] Add topology and visual tests for hollow absence, aperture, rim, interior,
  winding, normals, and component-budget behavior.
- [ ] Record the deprecated jME API use in `TreePbrMaterialAdapter` as deferred
  maintenance or remove it if the compatible replacement is local and safe.
- [x] Reconcile the legacy JDBC-backed asset library/`ExportManager` glTF
  `extras` shape with the `pg.asset-index/1` package contract, or explicitly
  scope the legacy path out of the `PlaneGuardianAssetInterface` boundary
  (see `docs/architecture/generation-platform.md` and
  `.steadyarc/deferred-issues.md`). Resolved by scoping: the legacy library
  always writes an empty/non-provider `generatorId` and a valid fallback GLB,
  so `RuntimePackageResolver` always falls back rather than resolving a
  trusted provider; full retirement of the manual shader-ref workflow remains
  deferred.

### 17. Final POC validation

- [ ] Great Tree golden cases are deterministic, visually readable, previewable,
  and exportable through the generic workflow.
- [ ] At least one non-tree fixture reuses ProtoMesh operations and the generic
  registry/workbench contracts, demonstrating that neither platform is
  tree-shaped.
- [ ] Generated texture identity and one reusable VFX plugin survive round-trip
  export and loaded-asset semantic/runtime application.
- [ ] No explicit geometry generator depends on the Great Tree or another asset
  family.
- [ ] jME meshes are products of adapters, never the mutable authoring
  representation.
- [ ] Registry discovery, defaults/presets, semantic resolution, live preview,
  loaded-asset application, environment updates, serialization, and export pass
  automated integration tests on the supported JDK.
- [ ] Required human visual checks are recorded separately from automated
  structural and integration evidence.
- [ ] The final compatible-JDK Maven regression passes through the Steady Arc
  relay and its evidence is published against the validated source revision.
- [ ] POC completion evidence and remaining aesthetic or production work are
  recorded without treating deferred work as completed implementation.
- [ ] The game implementation domain depends only on the documented
  `PlaneGuardianAssetInterface` surfaces (package/runtime compatibility,
  `RuntimeAssetProvider` discovery, `LoadedAsset`, and shared identity/version
  types); no generator, `ProtoMesh`, semantic-adapter, or authoring-tooling
  package is imported outside generation-time/tooling-time code. Enforced by
  `PlaneGuardianAssetInterfaceBoundaryTest`.

Current focus: item 15, integrated generation, semantics, and live visual preview.

## Parallel arc: Expand the generator library

This is an auxiliary arc that runs alongside the active arc above, not a
replacement for it. Its objective is to implement additional, individually
scoped asset generators that exercise the generic generator-provider platform
established by the Generic Asset Workflow Correction arc. It does not touch
the click-testing, hollow-tube-correction, or final POC validation work; that
work continues to be owned by the Great Tree reference generator under the
active arc above.

### Architectural boundary an asset generator must respect

Study these boundaries before adding or modifying a generator. They are drawn
from the currently established platform contracts (`docs/architecture/generation-platform.md`,
`com.planeguardian.assets.generation.api`, and `com.planeguardian.assets.tools.generator`):

- A generator is discovered only through the generic, versioned
  `AuthoringGeneratorProvider` service-loader registry
  (`AuthoringGeneratorRegistry`). It must never be constructed directly by the
  workbench, another generator, or asset-family-conditional logic.
- A provider publishes a `GeneratorDescriptor`: a stable generator ID, a
  `ContractVersion`, an asset-family stable ID, a display name, a versioned
  parameter schema (with advanced-parameter grouping), presets, declared
  capabilities, supported preview/export formats, roles, sockets, and the
  subset of parameters that are semantic-derived. The workbench must be able
  to build controls from this schema alone, with no generator-specific IDs
  embedded in generic tooling.
- Direct parameters, presets, and semantic-derived parameters are distinct.
  A generator publishes every direct control it exposes; semantic wheels
  remain the editable source for resolver-derived values, never a hidden
  side channel.
- A generator may optionally supply an `AssetSemanticAdapter` that resolves a
  versioned semantic profile (2D direction/extremity wheel coordinates plus
  independent Salience, with optional center relationship, focus, and
  secondary poles) into direct parameter values and an inspectable
  contribution trace. Runtime/environment inputs (time, wind, weather) never
  become semantic identity or enter the deterministic generation fingerprint.
- Generation is deterministic: the same request (parameters, seed, semantic
  profile) yields the same engine-neutral product. Named deterministic random
  substreams and numeric quantization are used instead of ambient randomness.
- Geometry authoring happens on the engine-neutral ProtoMesh topology kernel,
  composed from the reusable curve/constructive-geometry library (splines,
  rings, lofts, extrude/inset/bridge/fill/cap/weld/split, loop transitions).
  A generator must not embed reusable geometry logic that belongs in the
  shared toolkit, and must not depend on the Great Tree or any other asset
  family's package.
- Generated textures, material recipes, and VFX plugin configurations are
  requested through the reusable generated-resource systems (versioned,
  fingerprinted, cached) rather than generator-private raster/material code,
  except for genuinely generator-private parameter composition.
- jME (or any other engine) meshes, materials, and scene nodes are produced
  only by explicit adapters at the render/export boundary; the mutable
  authoring representation is always the engine-neutral ProtoMesh/product, and
  glTF/jME conversion happens at that boundary, never inside the generator's
  core logic.
- Optional runtime capabilities (wind, weather response, animation, VFX
  triggers) are composed onto a loaded asset through the generic loaded-asset
  facade; a generator must not implement per-frame behavior itself unless it
  declares and registers a capability through that contract.
- Every generator ships focused regression coverage (schema validity,
  determinism, topology/render validation, and semantic golden cases where a
  semantic adapter exists) before it is considered complete.

### 1. Semantically aware crystals

- [ ] Define the crystal asset family's stable identity, parameter schema,
  presets, capabilities, and roles/sockets as a `GeneratorDescriptor`, composed
  from the reusable curve/constructive-geometry and ProtoMesh kernel rather
  than private geometry logic.
- [ ] Define the semantic adapter mapping resolved wheel profiles (facet
  sharpness/growth direction, color/refraction character, and Salience-driven
  prominence, to be refined) to direct crystal parameters, with a contribution
  trace and fixed golden semantic cases.
- [ ] Produce deterministic, engine-neutral crystal cluster geometry (facets,
  clusters/growth groupings, base/host attachment) with valid topology,
  normals, tangents, bounds, and structural LODs.
- [ ] Reuse or extend the generated-resource systems for any crystal surface
  texture/material/VFX needs instead of adding generator-private raster code.
- [ ] Register the provider through the generic registry, prove it appears
  and is configurable in the generic workbench without workbench changes, and
  add regression coverage (schema, determinism, topology/render, semantic
  golden cases).
- [ ] Perform the same automated and human visual validation split used by the
  Great Tree reference generator; do not mark visual behavior complete from
  structural tests alone.
