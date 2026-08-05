# Engineering Notes

## Repository facts and invariants

- This repository intentionally carries two concurrent Steady Arc handoff
  records inside the single `.steadyarc/handoff.md` file (Record 1: the main
  Generic Asset Workflow Correction and Final POC Validation arc; Record 2:
  the auxiliary Expand the generator library arc). Each record has its own
  handoff ID, delegation, constraints, and return condition, and neither
  authorizes work in the other's scope. This is a project-level adjustment to
  support a genuinely parallel arc without one arc's return silently closing
  the other; see `.steadyarc/knowledge-adjustments.md` for the upstream
  proposal to formalize multi-record handoff files in the normative workflow.

- PlaneGuardianAssets is Java 17 with jMonkeyEngine 3.7, LWJGL3, Swing, H2,
  Gson, Lombok, SLF4J/Logback, and JUnit 5.
- Persistence is direct JDBC. `DatabaseManager` owns an H2 `JdbcDataSource` and
  schema creation; repositories use connections, prepared statements, and
  explicit row mapping. The project does not use JPA or Hibernate.
- JSON serialization and glTF mutation use Gson. The project does not use Jackson.
- Cross-repository delivery to PlaneGuardian requires a small, versioned
  runtime/data contract rather than source coupling.

## Procedural architecture

- Resolve semantics on the CPU before rendering: semantic profile → resolved
  visual profile → asset-family adapter → geometry, material, and VFX recipes.
- Determinism is versioned and uses named independent random substreams.
- The version-1 random protocol derives each named stream with SHA-256 over the
  domain `PlaneGuardianAssets.RandomStream/1`, a NUL separator, the root seed as
  a big-endian signed 64-bit value, and the UTF-8 stream name. The first eight
  digest bytes seed a specified SplitMix64 stream; opening order is irrelevant.
- Reproducibility fingerprints use typed, length-delimited SHA-256 input under
  the `PlaneGuardianAssets.Fingerprint/1` domain. Floating-point values enter a
  fingerprint only after explicit step quantization; negative zero is normalized.
- Shared `generation.api` and `generation.determinism` code is engine and
  application neutral. A source-boundary test rejects jME, Swing/AWT, JDBC
  repository, tool UI, exporter, and glTF package dependencies in that core.
- Structural identity belongs in geometry/variants, surface response in PBR
  material inputs, and transient or spatial behavior in VFX.
- Attachments retain native identity; a host contributes only a bounded contact
  layer. Mutable condition remains separate from intrinsic semantics.
- PlaneGuardianGDD is authoritative for semantic and visual meaning.
  `docs/gdd-references.md` records consumed sources; `docs/procedural-assets/`
  owns implementation contracts.
- The First Asset Generator POC proves a reusable generation platform; the
  Great Tree is a consumer and integration test, not the owner of shared algorithms.
- Explicit geometry generation targets an engine-neutral `ProtoMesh`. jME and
  glTF are output adapters; their mesh/scene types do not enter topology operations.
- The initial ProtoMesh kernel uses stable monotonic vertex, edge, directed-loop,
  and face IDs. Mutable builders publish immutable ID-ordered snapshots; shared
  edges are canonicalized by endpoints, while UVs, normals, and scalar layers
  remain per-corner for seams. Validation reports isolated vertices,
  non-manifold edges, and inconsistent adjacent winding.
- ProtoMesh fingerprints include quantized ordered topology and per-corner data.
  Deterministic ear-clipping converts valid concave or convex polygons to a
  seam-preserving engine-neutral triangle buffer. The jME adapter alone creates
  `Mesh` buffers and rejects double values that overflow finite GPU floats.
- Normals follow an explicit preserve-authored, flat-by-source-face, or
  smooth-by-source-vertex policy. UV-derived tangents are orthogonalized against
  those normals and carry glTF-compatible ±1 handedness. jME and glTF-ready
  buffers consume the identical immutable `RenderMesh`, including position bounds.
- Polygon validation detects projected non-adjacent edge crossings before
  triangulation. A version-one end-to-end golden quad pins the topology hash,
  triangle ordering, all render attributes, and glTF position bounds.
- Shared curves use a normalized parameter domain and include cubic Hermite,
  uniform/centripetal/chordal Catmull–Rom, and positive-weight NURBS evaluated
  with homogeneous De Boor interpolation. NURBS surfaces are not yet included.
- Distance-driven placement uses explicitly sampled arc-length tables.
  Parallel-transport frames minimize incidental rotation; tube twist is an
  independent absolute roll profile over normalized arc length, so tessellation
  density does not alter the requested twist.
- Spline tubes separate radius, roll, and angular cross-section profiles; sample
  rings at arc-length fractions; use at least eight vertices per ring; loft only
  quad sides; retain per-corner UV seams; and return stable rings/end frames for
  future caps, collars, bridges, and junctions.
- Asset-family generators publish all direct structural, surface, feature,
  motion, resolution, randomization, and LOD controls in a versioned parameter
  schema. Semantic wheels are editable inputs; resolved semantic channels and
  contribution traces are derived read-only outputs. A wheel manipulation UI is
  required later for visual validation.
- Parameter schemas must be exportable as AI-readable manifests with stable IDs,
  descriptions, visual effects, units, ranges, defaults, dependencies, warnings,
  and examples. AI “great oak” output is a versioned parameter/wheel proposal
  plus rationale and must pass the ordinary validator; derived resolver channels
  are never writable proposal fields.
- Isolated topology transactions clone immutable snapshots, preserve surviving
  IDs, retire removed identities without reuse, allocate new IDs monotonically,
  support preview/rollback, and reject invalid commits. Generic operations now
  include ring fill/caps, equal-ring quad bridges, planar face inset, vector
  extrusion, forward scaled collars, and an inset-plus-extrude collar proof.
- Edge split rewrites only incident polygons and interpolates compatible corner
  channels; it does not choose a remeshing pattern. Vertex weld is limited to
  coincident non-adjacent vertices inside an explicit tolerance and rejects face
  collapse. Both operations retire replaced identities monotonically.
- Parametric surfaces use a normalized two-dimensional domain. Tensor-product
  NURBS patches evaluate in homogeneous coordinates with independent U/V degree
  and knots. Fixed-resolution patch tessellation emits UV-mapped quad grids and
  ordered boundary rings; adaptive tessellation, patch welding, and seam-normal
  policy remain explicit later stages.
- Unequal loop bridges use an explicit reviewed pattern catalog. Initial
  reversible 8↔12 and 8↔16 transitions remain quad-only by evenly distributing
  two or four extraordinary wedge quads; unlisted size pairs are rejected.
- The initial branch junction is a parent quad inset/collar followed by the
  reviewed 4-to-8 transition into the minimum child tube ring. Child phase is
  validated to prevent twisted quads. General boolean fusion and pair-of-pants
  topology remain unnecessary for the POC because bounded hidden overlap is
  allowed by the Great Tree design.
- Generated topology is quad-first and organized around rings, loops, patches,
  and tested standard resolution transitions. Boundary triangulation and
  unavoidable poles/singularities may use triangles.
- `docs/guides/geometry-toolkit.md` is the operator entry point for shared
  geometry construction. Agents compose existing engine-neutral primitives
  before proposing asset-specific geometry; demonstrated recurring gaps are
  implemented and tested in the shared toolkit.
- Constructive operations, splines, frames, primitives, and surface processing
  live in asset-family-independent modules with immutable inputs and diagnostics.
- Generated textures are versioned Asset Library entities with provenance and
  cache identity. Reusable particle and VFX behaviors are stable-ID plugins,
  configured by assets rather than duplicated per generator.
- Generated texture cache identity canonically includes provider and parameter
  schema versions, stable texture ID, dimensions, seed, sorted normalized
  parameters, and sorted fingerprinted source resources. Descriptors validate
  the derived fingerprint and declare color space plus stable per-channel
  semantics; pixel storage and encoding are separate boundaries.
- Raw texture pixels are immutable tightly packed format/dimension-validated
  bytes. Encoded artifact metadata uses portable relative paths and content
  fingerprints without owning persistence. Engine-neutral material recipes use
  stable model/input IDs and typed numeric, flag, or texture values; texture
  bindings pin the generated texture fingerprint as well as its resource ref.
- The initial texture export boundary uses deterministic dependency-free PNG
  encoding. R8, RGB8, and RGBA8 retain their PNG channel layouts; RG8 expands to
  preview-safe RGBA. The local cache keys entries by validated generation
  fingerprint, publishes metadata after bytes, and verifies persisted byte length
  plus SHA-256 fingerprint on every hit; incomplete or corrupt entries are misses.
- VFX configurations use stable provider/plugin/configuration IDs, versions,
  seeds, sorted typed parameters, fingerprint-pinned resource references, and
  sorted socket/local-transform attachments. Trusted compiled providers use a
  deterministic duplicate-rejecting ServiceLoader registry; engine realization
  and simulation strategy remain adapters.
- Initial shared providers are classpath-discovered painterly bark (sRGB RGBA),
  clustered foliage coverage (linear R8), and bounded socket-attached pollen
  motes. Their deterministic algorithms are versioned reference implementations,
  not final art direction; asset families reuse or replace providers centrally.
- Normal presentation is painterly high fantasy at approximately 200×200 pixels
  or less per asset. Geometry prioritizes silhouette and large form; textures,
  masks, normal response, and restrained vertex jitter carry fine scale cues.
- The main-stage population envelope is 10 visible islands at maximum-detail
  zoom and 100 visible islands at overview zoom. Other stage content is mainly
  textures, billboards, and UI. LOD, attachments, shadows, and material cost are
  screen-size driven and must be profiled as complete 10/100-island scenes.
- Ordinary generated tube rings use at least eight vertices. Lower counts need
  explicit distant-LOD evidence; trunks and important junctions may use more.
- Great Tree trunks, branches, and major roots are spline-based. Branching is a
  recursive graph with bounded parameters per level; root flare and partial
  above-ground exposure are explicit controls. Density fields may guide later
  occupancy but are not required for the first topology proof.
- The item-7 trunk foundation is `generation.tree.TreeStructure` plus
  `DeciduousTreeStructureGenerator`. It uses only the named
  `tree.trunkSpline` SplitMix64 stream and shared Hermite/tube tooling, publishes
  a quad-sided `tree.trunk` ProtoMesh product, and fingerprints the quantized
  topology. It intentionally does not make jME, material, cache, or export
  decisions.
- Item 7 extends that proof with a version-one `TreeComposition`: bounded
  branch levels, root settings, LOD limits, and a component budget. Branches
  and roots are independent spline-tube parts keyed by stable IDs and
  path-scoped named streams; their aggregate fingerprint is ordered by stable
  ID. Roots explicitly carry host-contact intent. This is a bounded
  overlap-based composition proof, not a claim of watertight branch fusion or
  an engine/render adapter.
- Tree branch levels compose recursively: each level addresses children by its
  stable parent path, derives its stream from that complete path, and traverses
  parent/child order deterministically until the explicit component budget is
  reached. A budget therefore truncates a stable suffix rather than changing
  already admitted component identities.
- Item 8 begins with an engine-neutral, closed, UV-mapped ellipsoidal foliage
  shell shared primitive. Tree crown composition uses bounded direct settings
  and path-scoped cluster streams to create separately named foliage products;
  crown topology does not consume the structural component budget. The first
  semantic adapter resolves only bounded crown and feature suitability, preserves
  intrinsic crown identity against host semantics, and exposes each crown
  contribution. Moss, vines, flowers, fruit, and fungi are independent bounded
  semantic surfaces with their own contribution trace; geometry and renderer
  adapters consume those resolved surfaces without redefining their semantics.
  The first placement consumer is bounded deterministic feature-anchor admission:
  per-group and aggregate direct budgets yield stable-ID engine-neutral anchors,
  leaving feature geometry and renderer adapters independent. The initial
  geometry consumer turns each admitted anchor into a separately named,
  UV/tangent-ready ellipsoidal mesh through the shared foliage shell primitive;
  its role-specific bounded proportions are deterministic from the placement ID
  and seed, while renderer adapters remain separate.
- `TreeParameterSchema` version one is the engine-neutral public inventory of
  every current direct item-7 trunk, branch, root, LOD, and component-budget
  control. Its stable IDs, declared types, units, allowed ranges, and defaults
  support future manifests and tools without making derived semantic values
  independently writable.
- Item 7 now publishes a deterministic `RenderMesh` alongside every structural
  `ProtoMeshSnapshot`, generated through the shared triangulation and surface
  processing boundary with UVs, smooth normals, and tangents. The current
  bounded hollow is only a final optional disconnected interior-tube suffix:
  it cannot displace admitted trunk, branch, or root components under the
  declared budget, but it is not a valid carved hollow. Item 7 therefore retains
  a corrective follow-up for a shared wall-recess operation, deterministic hollow
  settings/schema, and aperture/rim/interior tests; boolean subtraction remains
  deferred.
- ProtoMesh and generated assets use glTF-native right-handed coordinates:
  `+Y` up, `+Z` forward, metres, radians, ground contact at the origin, and
  counterclockwise front faces under positive transforms. Blender performs its
  own Y-up/Z-up conversion; generators add no manual axis correction.
- GLB/glTF is a triangulated runtime interchange format, not the lossless
  ProtoMesh authoring format. Polygon topology and per-corner attributes remain
  available in ProtoMesh even after normal delivery adapters triangulate them.
- Item 9 starts with a version-two public tree presentation schema: host-contact,
  PBR response, wind response, and preview tier are direct controls, while
  resolved semantic output remains read-only. The manifest/proposal boundary
  accepts only these schema IDs and six source semantic wheels, and requires
  ordinary validation before use.
- Per-part wind data is deterministic named-stream output (`TreeWindResponse`);
  it represents response weight/phase, not world weather. A fixed engine-neutral
  gameplay preview fixture supplies camera, light, ambient, tier, and shadow
  intent. jME PBR/pollen realization stays in adapters and creates fresh material
  instances after resolving stable sockets.
- The jME tree PBR adapter resolves texture recipe bindings only through the
  generated-resource cache and verifies the requested generation fingerprint,
  resource reference, media type, and encoded-artifact metadata before PNG
  decoding. Its fixed input mapping uses standard PBR slots; foliage coverage
  becomes an alpha-clipped base-color mask, while host contact is an AO light map
  scaled by the direct host-contact presentation control.
- Item 9 binds deterministic per-part tree wind response and independent
  `RuntimeWeatherInput` values only at the jME `TreeWindPbr` shader boundary.
  Runtime direction is normalized and nonzero for moving weather; intensity and
  elapsed time remain mutable scene inputs, never semantic or fingerprinted state.
- `TreePreviewJmeAdapter` realizes the fixed gameplay fixture only at the jME
  boundary: it converts immutable structural and crown render products to
  cast-and-receive geometries, applies the fixed camera/light contract, and
  supplies a directional shadow renderer. Foliage coverage artifacts are expanded
  to white RGBA textures with coverage in alpha before alpha clipping; generated
  textures use trilinear minification for distance review.
- `SemanticWheelDesktopEditor` is a Swing visual-validation tool backed by the
  engine-neutral `SemanticWheelEditorModel`. It accepts only source semantic
  wheels, seed, and render tier; each valid edit regenerates resolved semantic,
  structural, and crown summaries. Derived profile fields and contributions are
  presented read-only, and tier selects an explicit bounded structural LOD.
- Item 9 regression coverage exercises cache-verified PBR bindings, wind values,
  shared pollen realization at resolved sockets, fixed shadowed and unshadowed
  gameplay fixtures, and semantic-wheel regeneration across every render tier.
- Item 10 packages use `pg.asset-index/1` with exact runtime/provider API
  compatibility. `package_manifest.json` binds every data file and the supplied
  runtime JAR by SHA-256; generated runtime cache keys additionally bind the
  compatibility tuple, generator ID, and generation fingerprint. Runtime
  providers are trusted compiled `ServiceLoader` implementations only, and
  unresolved generators use validated forward-slash package-local fallbacks.
- `GltfPersistenceFormat.loadAsset` is the repository-owned glTF persistence
  boundary: it registers the exported asset directory, loads its `.gltf`/`.glb`
  through jME, and returns a `Node`. The desktop viewer uses this flow for glTF
  previews; preview-only bounds, floor, and lighting are never part of the
  exported asset.
- The Great Tree is a reference consumer of a generic asset workflow, not a
  privileged platform subsystem. Generator providers declare versioned schemas,
  defaults/presets, semantic adapters, roles, sockets, runtime capabilities, and
  preview/export support through a registry. The generic authoring workbench
  must not construct or interpret a tree generator directly.
- A semantic wheel source value is not a scalar generator control. It carries a
  two-dimensional direction/extremity coordinate and an independent Salience;
  supported profiles may additionally carry a center relationship, focus, or
  secondary poles. A centralized versioned resolver produces a visual profile,
  and asset-family adapters translate that profile into geometry variants,
  materials, and VFX. Shaders and individual generators do not reinterpret the
  full Lore Map independently.
- The receiving runtime exposes a generic loaded-asset facade. Semantic profiles
  are applied through asset-family adapters, while elapsed time, wind, weather,
  and similar mutable environment inputs are dispatched to optional composable
  runtime capabilities. Runtime environment state is neither semantic identity
  nor part of deterministic generation fingerprints.
- Visual validation is an integrated authoring workflow: direct parameters,
  named defaults/presets, seed, reusable semantic-wheel controls, resolved
  contribution trace, generated product, and the PlaneGuardian-equivalent loaded
  preview share one session. Structural assertions, serialization tests, and
  human visual review remain distinct evidence levels.
- Desktop authoring providers are trusted classpath services discovered through
  `AuthoringGeneratorRegistry`. Providers expose a `GeneratorDescriptor` and a
  Swing-neutral generation operation; the generic workbench owns schema-driven
  controls, named presets, reset/default behavior, seed editing/randomization,
  and basic-versus-advanced presentation. The Great Tree's `Great Oak` preset is
  the first provider baseline, not a workbench special case.
- Semantic authoring uses `SemanticProfileEditor` and custom-painted
  `SemanticWheelComponent` instances embedded in the generic workbench. Source
  wheel values are editable, resolved visual channels and contributions are
  read-only, and generator parameters declared semantic-derived use the resolved
  value unless the user explicitly enables an override. `ParameterPrecedence`
  is the shared executable policy; ordinary direct controls are unaffected.
- `ComposableLoadedAsset` is the receiving facade and dispatches resolved
  profiles only to `SemanticReactive` capabilities and per-frame environment
  snapshots only to `EnvironmentReactive` capabilities. The jME tree installs
  semantic and environment capabilities through `TreeLoadedAssetFactory`:
  semantic application updates foliage variants/material state and existing VFX
  visibility idempotently, while environment updates bind wind direction,
  intensity, and elapsed time without changing fingerprints. Tree preview/export
  nodes now retain stable socket nodes and metadata for loaded runtime use.
- The generic workbench uses a 550 ms restartable debounce for live regeneration
  and automatically reloads successful output into the singleton preview window.
  Preview loading and integration tests share `AssetPersistenceLoader`, then
  `JmeLoadedAssetFactory` installs the same runtime capabilities intended for
  PlaneGuardian. The preview supplies automatic bounds framing, floor,
  three-point lighting, directional shadows, and a low-intensity live wind
  environment. Human click validation remains required evidence and is not
  inferred from the end-to-end persistence test.

- The GDD's "asset service-provider contract" (an explicit open question in
  `Technical/Semantic_Procedural_Asset_Architecture.md`) is documented as the
  `PlaneGuardianAssetInterface` boundary in
  `docs/architecture/generation-platform.md`. It is not one literal Java type;
  it is exactly four already-implemented surfaces the game implementation
  domain may depend on: `PackageCompatibility`/`RuntimePackageResolver`,
  `RuntimeAssetProvider` discovery, `LoadedAsset`/`ComposableLoadedAsset`
  (realized for jME by `JmeLoadedAssetFactory`), and the shared
  `StableId`/`ContractVersion`/`ReproducibilityFingerprint` identity types. The
  game must never import generator, `ProtoMesh`, semantic-adapter, or
  authoring-tooling (`AuthoringGeneratorProvider`) packages directly; those
  remain generation-time/tooling-time only.
- The legacy JDBC-backed asset library (`com.planeguardian.assets.db`,
  `com.planeguardian.assets.model`, `com.planeguardian.assets.export.ExportManager`)
  predates the generic generation platform and still writes its own
  `custom_shader_id`/`shader_parameters` glTF `extras` shape alongside the newer
  `pg.asset-index/1`/`pg.gltf/1` package contract. This divergence is recorded
  as a deferred reconciliation issue, not assumed to already be unified.

- Roadmap item 16's legacy-library scoping is resolved by explicit gating, not
  reconciliation: `ExportManager` always writes an empty or non-provider
  `generatorId` and a valid package-local fallback GLB for every library
  asset, so `RuntimePackageResolver` deterministically falls back rather than
  ever resolving a trusted `RuntimeAssetProvider` for a legacy entry. This
  keeps the legacy path out of the `PlaneGuardianAssetInterface` boundary
  without rewriting its JDBC/`GltfExtrasInjector` internals. Regression-tested
  by `LegacyLibraryPackageScopeTest`. Full retirement of the manual per-asset
  shader-ref workflow remains a separate deferred item
  (`.steadyarc/deferred-issues.md`).
- Roadmap item 17's game-domain dependency-boundary criterion is enforced by
  `PlaneGuardianAssetInterfaceBoundaryTest`, which asserts that
  `com.planeguardian.assets.runtime` and `com.planeguardian.assets.generation.api`
  never source-reference `db`, `export`, `tools`, `gltf`, or any non-`api`
  `generation` subpackage, `jME`, or Swing/AWT types.

## Tool behavior

- Builds and Steady Arc commands use the committed Maven Wrapper.
- `RunWidget.ps1` launches the widget through that wrapper; the relay exposes
  only its fixed operation catalogue.
- Managed scripts came from release `0.1.0-rc.1` and matched PlaneGuardian sources.

## Semantically aware crystals — design scoping

- Roadmap item 1 ("Semantically aware crystals") technical tasks and the
  design document `docs/procedural-assets/08-semantically-aware-crystals.md`
  define: new `Power`/`Rarity`/`Quality` semantic wheels (game-domain values
  entering the platform only as resolved wheel coordinates, never raw
  gameplay state), a reusable `CrystalSemanticAssetProfile` abstraction
  deriving color/shape/size/complexity/radiance/VFX-decorator/setting
  channels from the full resolved semantic profile, and a new shared global
  named-color palette facility so future generators (e.g. a crystal-sword
  generator) can reuse curated colors instead of inventing semantic-derived
  colors independently. Material design targets alpha transparency, a cheap
  Fresnel/environment-sample reflection hint (not real reflection), and
  hue-true emissive color driven by Power. No implementation has started;
  this is design/roadmap scoping only.
