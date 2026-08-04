# Engineering Notes

## Repository facts and invariants

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
  semantic surfaces with their own contribution trace; geometry and placement
  consume those resolved surfaces later rather than redefining their semantics.
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

## Tool behavior

- Builds and Steady Arc commands use the committed Maven Wrapper.
- `RunWidget.ps1` launches the widget through that wrapper; the relay exposes
  only its fixed operation catalogue.
- Managed scripts came from release `0.1.0-rc.1` and matched PlaneGuardian sources.
