# Reusable Generation Platform

## Purpose

The first asset generator is a proof of the platform that future generators
will share. A generator composes reusable geometry, texture, material, and VFX
capabilities; it must not hide generally useful algorithms inside an
asset-family implementation.

The dependency direction is:

```text
asset-family generator
  -> reusable geometry/texture/VFX libraries
  -> engine-neutral generated-asset description
  -> jME/glTF adapters
```

jMonkeyEngine types belong at preview/runtime boundaries. Explicit geometry
algorithms operate on an engine-neutral `ProtoMesh` representation.

## ProtoMesh

`ProtoMesh` is the editable topology representation used during generation. It
should support:

- stable vertex, edge, loop, and face identities while operations are active;
- polygon faces, with quads as the preferred generated topology;
- per-corner attributes where seams require different UVs, normals, or roles;
- named semantic groups, material regions, sockets, and generation provenance;
- deterministic traversal and serialization order;
- validation before conversion to immutable render buffers.

It is not a second scene graph and should not contain jME materials, spatials,
GPU buffers, or render-thread behavior. Conversion adapters triangulate only at
the render/export boundary and produce jME `Mesh` or glTF-ready buffers.

### Initial kernel contract

The implemented construction kernel assigns monotonically increasing stable IDs
to vertices, undirected edges, directed face-corner loops, and polygon faces.
Faces share geometric edges by canonical endpoint identity. Per-corner UVs,
normals, and named scalar layers remain independent, so a seam does not require
duplicating the authoring vertex.

Generators build through a mutable `ProtoMeshBuilder` and publish only immutable,
ID-ordered `ProtoMeshSnapshot` values. A snapshot contains derived edge uses,
boundary discovery, semantic face groups, and validation diagnostics. The first
validator detects isolated vertices, non-manifold edges, and adjacent faces with
inconsistent winding. It also projects each polygon onto its dominant plane to
reject non-adjacent edge crossings, including zero-area bow-tie polygons, before
triangulation. Geometry adapters and constructive operations consume this
contract in later passes rather than gaining privileged access to builder state.

Topology fingerprints serialize stable IDs, quantized positions, ordered faces
and loops, semantic groups, and per-corner attributes into the versioned
reproducibility fingerprint protocol. Sub-quantum numerical noise therefore does
not invalidate caches, while meaningful topology or attribute changes do.

The shared render boundary uses deterministic ear-clipping for planar convex or
concave polygons. It emits one render vertex per authoring loop, preserving UV,
normal, and scalar seams, and rejects invalid or self-intersecting topology rather
than guessing. Both jME and future glTF adapters consume this engine-neutral
triangulated result. The jME adapter is one-way and rejects values that cannot be
represented as finite GPU floats.

### Surface processing and adapter parity

Triangulation feeds one engine-neutral surface processor with three explicit
normal policies: preserve complete authored corner normals, generate normals
flat within each source polygon, or smooth across corners sharing a source
vertex. There is no implicit engine-side normal generation.

When requested, tangents are derived deterministically from complete,
non-degenerate UV triangles, orthogonalized against the selected normal, and
stored with a `-1` or `+1` bitangent handedness. Missing or degenerate UVs fail
the tangent request rather than emitting unstable vectors.

The resulting `RenderMesh` is the common source for jME preview buffers and
glTF accessor-ready arrays. The glTF boundary provides positions, normals,
optional tangents and UVs, indices, and position min/max bounds; actual GLB
container and material serialization remains a separate export concern.

The version-one golden quad fixture pins the topology fingerprint, deterministic
triangle order, positions, normals, tangents, UVs, and position bounds across the
complete ProtoMesh-to-glTF-ready path. Intentional changes to any pinned output
require an explicit compatibility/version decision rather than a casual snapshot
update.

## Coordinates and interchange

ProtoMesh and generated-asset contracts use the native glTF convention:

- right-handed coordinates;
- `+Y` up and `+Z` forward;
- metres for linear measurements and radians for angles;
- primary ground contact at the asset origin;
- counterclockwise front-face winding under positive transforms.

Blender's glTF importer/exporter owns conversion between glTF's Y-up convention
and Blender's native Z-up workspace. Generated geometry must not add a manual
axis-correction rotation. Blender exports keep **Y Up** enabled, avoid retained
negative scale, and apply intended object transforms before delivery.

GLB/glTF is the triangulated runtime interchange representation. It preserves
render buffers, materials, hierarchy, and versioned `extras`, but not the
editable quad loops, polygon topology, spline controls, or constructive history
held by ProtoMesh. ProtoMesh therefore retains polygon and per-corner data even
though its normal glTF adapter triangulates at the boundary.

Core delivery materials use glTF metallic/roughness PBR inputs. Custom
PlaneGuardian shader identity and semantic roles travel through the versioned
`extras` envelope alongside a standard preview material. Stable behavior never
depends solely on Blender collection membership or display names.

References:

- [glTF 2.0 coordinate system and units](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#coordinate-system-and-units)
- [Blender glTF importer/exporter](https://docs.blender.org/manual/en/3.6/addons/import_export/scene_gltf2.html)
- [jMonkeyEngine Blender/glTF guidance](https://wiki.jmonkeyengine.org/docs/3.8/tutorials/how-to/modeling/blender/blender_gltf.html)

## Great Tree structural composition

The first item-7 proof is an engine-neutral structural product in
`generation.tree`. `TreeStructure` retains bounded trunk dimensions, taper,
lean, curvature, twist, and tube resolution. Version-one `TreeComposition`
adds bounded branch-level, root-flare/root, structural-LOD, and component-budget
controls without coupling the tree to an engine or exporter.
`TreeParameterSchema` version one publishes each current direct trunk, branch,
root, LOD, and component-budget control with a stable ID, type, unit, allowed
range, and default. It is the source for future tool, manifest, and proposal
adapters; derived semantic values remain outside its writable controls.

`DeciduousTreeStructureGenerator` composes `CubicHermiteCurve` and
`SplineTubeGenerator` into independent quad-sided `ProtoMeshSnapshot` parts
for the trunk, named branches, and major roots. Every branch and root uses a
path-scoped named random stream; component IDs, semantic roles, host-contact
flags, and sockets are stable. The product fingerprint canonically includes
the quantized topology fingerprint of every stable-ID-ordered part.

The structural product deliberately has no jME scene, material, cache, or
export responsibility. Branch/root overlap is an explicit bounded POC choice;
watertight fusion, adaptive LOD selection, and engine adapters remain later
work.

Every structural part also carries its canonical engine-neutral `RenderMesh`.
It is derived only after immutable topology using the shared deterministic
triangulator and smooth-normal/tangent processor, so preview and export adapters
consume identical triangle data without allowing renderer types into the tree
generator. The current hollow is a bounded disconnected interior tube admitted
last under the component budget. It provides a stable provisional role and
socket, but it is not a carved hollow: a shared wall-recess operation,
configurable deterministic presence, and aperture/rim/interior tests are still
required. Boolean subtraction and watertight trunk fusion remain out of scope.

## Geometry library

The POC establishes reusable modules rather than tree-local helpers:

1. **Topology kernel** — creation, adjacency, boundary discovery, deterministic
   iteration, attribute layers, validation, and immutable snapshots.
2. **Curves and frames** — line/arc-length sampling, Hermite and Catmull-Rom
   splines, parallel-transport frames, closed-loop frames, and spline patches.
3. **Constructive operations** — extrusion, inset, bridge, fill, cap, ring/loop
   generation, loop resampling, welding, splitting, and controlled taper/twist.
4. **Topology transitions** — tested standard patterns for increasing or
   decreasing loop resolution while retaining mostly quad topology.
5. **Surface processing** — normals, tangents, UV parameterization, semantic
   masks, bounds, triangulation, simplification hooks, and finite-data checks.
6. **Primitive generators** — tubes along splines, lofts, patches, roots,
   branches, rocks, cards/shells, and other reusable constructions.

“Quad-first” means authoring coherent rings, loops, and patches wherever the
shape permits. It does not prohibit triangles at poles, transition singularities,
or the final GPU/export representation.

### Curve and frame foundation

All reusable curves implement an engine-neutral normalized `[0, 1]` parametric
contract with position and derivative evaluation. The initial family includes:

- cubic Hermite segments for direct endpoint/tangent control;
- multi-segment Catmull–Rom splines with uniform, centripetal, or chordal knots;
- rational B-spline (NURBS) curves using positive weights and homogeneous De
  Boor evaluation.

Hermite and centripetal Catmull–Rom are the normal choices for organic trunks,
branches, and roots. NURBS is not required to make those organic forms; it is
available for precise weighted art direction, imported control data, and the
future spline-patch system. This pass implements NURBS curves, not NURBS surfaces.

Sampled arc-length tables provide deterministic distance-to-parameter inversion,
so branch spacing, ring spacing, and detail frequency are measured along the
curve rather than in parameter space. Accuracy is explicitly controlled by the
table's sample count and therefore participates in generator compatibility.

Tube orientation uses minimal-rotation parallel transport. An initial normal
hint is projected into the first tangent plane with deterministic fallback axes;
subsequent frames transport that unrolled normal between tangents. Twist is a
separate absolute roll profile in radians over normalized arc length. Applying
roll after transport keeps the centerline unchanged and makes the number of
turns independent of ring density.

### Spline tube lofting

The initial tube generator samples rings at equal arc-length fractions, requires
at least eight vertices per ring, and lofts consecutive rings exclusively with
quad side faces. Radius, transported-frame roll, and angular cross-section
variation are independent profiles. This permits taper, twist, root flare,
faceting, and restrained radial irregularity without coupling those concerns.

The tube remains open in this stage. Its result includes every stable ring ID
plus start/end frames and nominal radii, forming the contract for later caps,
collars, branching junctions, and bridges. Circumferential UVs split from zero
to one in per-corner data while retaining shared geometric seam vertices;
longitudinal UVs and scalar provenance use normalized arc length.

## Generator composition

Asset generators own semantic adaptation and composition. They choose and
parameterize reusable generators, assign roles and sockets, and assemble the
generated asset description. For example, the Great Tree coordinates trunk,
branch, root, foliage-cluster, texture, material, and wind plugins; spline tube
generation itself remains independent of trees.

### Initial crown and semantic proof

The initial item-8 crown proof adds a reusable closed, UV-mapped ellipsoidal
foliage shell under `generation.geometry.foliage`. It is an engine-neutral
cluster-mass primitive, not a tree mesh algorithm. The Great Tree composes
bounded, independently named `tree.crown.cluster.*` products using path-scoped
random streams; they remain separate from the structural component budget.

`TreeSemanticAdapter` consumes already-resolved intrinsic and bounded host
profiles, returns direct crown settings plus independently named moss, vine,
flower, fruit, and fungal suitability, and emits a `Contribution` trace for
every crown coefficient and feature surface. All suitability values are bounded
to `[0, 1]`. `DeciduousTreeFeatureGenerator` consumes those resolved values and
versioned per-group/total budgets to produce stable-ID engine-neutral anchors for
each independent feature group. Feature geometry and renderer-specific adapters
remain separate consumers of those admissions. The first geometry consumer uses
the shared foliage-shell primitive with bounded role-specific proportions to
derive deterministic, separately named, UV/tangent-ready mesh products; it does
not alter placement identity or semantic resolution. Renderer-specific adapters
remain deferred.
Native tree crown coverage is not overwritten by an off-color host. The initial
golden cases pin ordinary, opposed, centered, Death+Creation, and native-life
against Death-host behavior. Later item-8 work can consume bounded admissions
for feature geometry without changing semantic resolution or placement identity.

### Item-9 presentation boundary

Version-two `TreeParameterSchema` adds direct presentation controls for
host-contact blend, PBR response, semantic wind response, and preview render
tier. `TreePresentationSettings` validates those values without making derived
semantic surfaces writable. `TreeParameterManifest` projects only current public
schema fields and editable source wheels; `TreeParameterProposal` rejects unknown
or derived IDs before they can reach a generator.

`TreeWindResponse` derives deterministic per-part weight and phase attributes
from a named stream. It describes how a generated asset responds; scene wind
force remains a separate runtime input. `TreePreviewFixture` is an engine-neutral
fixed gameplay camera/light/shadow contract, so renderers can be validated
consistently without moving visual policy into generators.

`RuntimeWeatherInput` carries normalized mutable-scene wind direction, intensity,
and elapsed time independently of generated state. `TreeWindJmeAdapter` combines
that input with the immutable per-part response and direct presentation frequency
on the `TreeWindPbr` vertex-shader path. The PBR material definition remains a
renderer adapter; weather is neither fingerprinted nor accepted by semantic or
parameter-proposal contracts. Its matching shadow prepass applies the same
displacement, so moving tree silhouettes retain aligned shadow casters.

jME realization remains under `generation.adapters.jme`: it creates fresh PBR
materials from immutable recipes and presentation settings, and realizes the
trusted pollen-motes configuration only after resolving each requested stable
socket. Texture recipe bindings resolve only through the generated-resource cache:
the adapter verifies the pinned generation fingerprint, resource reference, and
PNG artifact metadata before decoding. Standard PBR input IDs select texture
slots; foliage coverage is alpha-clipped through `BaseColorMap`, while
host-contact uses the AO light-map path and direct host-contact blend. No jME type
enters material, VFX, tree, or preview contracts.

`TreePreviewJmeAdapter` realizes that immutable data as a fixed gameplay review
scene only at the renderer boundary. It applies the fixture camera and light,
marks every admitted structural and crown mesh as a shadow caster/receiver, and
provides the directional shadow renderer. Its focused review fixture verifies
the admitted silhouette and selected LOD are preserved as renderable meshes,
while material review verifies trilinear minification, foliage alpha coverage,
PBR scalar response, host-contact AO, and emission strength.

Reusable generators must accept explicit immutable inputs and deterministic
random streams. They return data plus diagnostics and must not reach into UI,
database, global randomness, asset-family state, or jME scene objects.

Asset-family generators must publish a versioned parameter schema containing
every directly controllable generation value. Implementations may group values
and provide presets, but must not hide structural, surface, motion, feature, or
LOD constants that materially alter output. Resolved semantic profiles are the
exception: users edit the source semantic wheels, while derived channel values
are resolver output exposed for diagnostics rather than independent controls.

### AI-readable parameter manifests

The same versioned parameter schema should be exportable as an AI-readable
manifest. Each parameter entry needs a stable ID, plain-language purpose,
visual effect, type, units, allowed range or enum, default, dependencies,
interaction warnings, and a few representative values. Group descriptions and
generator-level art-direction guidance provide the context needed to interpret
large parameter sets coherently.

An AI assistant may turn a request such as “a broad ancient great oak with a
low crown and heavy root flare” into a proposal envelope containing generator
and schema versions, parameter values, optional semantic-wheel inputs, and a
plain-language rationale. The proposal is ordinary data: unknown IDs, invalid
ranges, missing compatibility information, and illegal combinations are rejected
by the same validator used by the UI and saved presets. AI output never writes
resolver-derived semantic channels or bypasses deterministic generation.

This manifest can also drive reference documentation, search, tooltips, preset
diffs, and the later semantic-wheel visual-validation interface. Choice of local
or remote model and prompt orchestration is intentionally deferred; the durable
contract is the self-describing validated schema and proposal format.

### Isolated topology transactions and constructive operations

Constructive operations begin from an immutable snapshot through an isolated
transaction. Surviving vertex, edge, loop, and face IDs are preserved; removed
identities are retired and never reused, while new identities continue
monotonically. Preview is allowed while the transaction is open, rollback
discards the draft, and commit rejects topology with error diagnostics. Source
snapshots never change.

The first generic operations cap an ordered ring with deterministic winding and
planar UVs, bridge equally sampled corresponding rings with quads, and extend a
forward tube end with a scaled circular collar. Unequal-ring transitions and
true branch fusion remain separate operations because they require explicit,
tested topology patterns rather than implicit resampling.

The face-edit foundation can remove a polygon and its loops, retiring any newly
orphaned edges. Generic ring fill, centroid-directed planar inset, and
vector-offset extrusion preserve source winding, per-corner attributes, and
semantic groups. A tree-independent collar proof composes inset and extrusion;
it deliberately does not claim to solve tube-to-host fusion or branch topology.

Edge split inserts one vertex at a deterministic fraction of a manifold edge,
interpolates compatible per-corner UV, normal, and scalar data, and rewrites
only the incident polygons. It intentionally permits an intermediate n-gon
rather than choosing a hidden remeshing pattern. Vertex weld is similarly
conservative: it merges coincident, non-adjacent vertices within an explicit
tolerance, preserves corner data on rewritten faces, and rejects any weld that
would collapse or repeat a face vertex. Replaced vertices, edges, loops, and
faces retain the same no-ID-reuse rule as other destructive edits.

### Parametric surfaces and spline patches

Parametric surfaces use the normalized `[0,1] x [0,1]` domain independently of
the underlying representation. The first implementation is a rational
tensor-product NURBS surface evaluated in four-dimensional homogeneous space,
with independent degrees and knot vectors for U and V. This avoids the weight
loss that would occur if one dimension were evaluated as ordinary dehomogenized
curves before evaluating the other.

The quad patch generator samples explicit U/V segment counts, emits a regular
quad grid with normalized per-corner UVs, and returns all four ordered boundary
rings for later stitching. It does not adaptively tessellate, weld neighboring
patches, or invent transition topology. Normals and tangents remain the concern
of the shared post-topology surface-processing pass so adjacent patches can
choose their seam policy together.

The first unequal-loop catalog entries are reversible 8↔12 and 8↔16 strips.
They retain quad-only faces by distributing respectively two or four
extraordinary wedge quads evenly around the lower-resolution loop, with regular
one-to-one quads between them. Each catalog entry pins expected ring sizes,
face count, wedge count, ordering, and reverse winding. Unlisted size pairs fail
instead of falling back to an unreviewed triangulation or heuristic remesh.

The initial branch-junction catalog entry transitions a quad parent face through
an inset/extruded collar and a reviewed 4-to-8 strip into the minimum supported
child tube ring. The child ring must be phase-aligned with the collar; the
operation rejects a cheaper rotated correspondence rather than emitting twisted
quads. This is the controlled quad-first POC junction requested by the Great
Tree design. General boolean union and a three-port pair-of-pants surface are
not prerequisites because hidden bounded branch overlap is explicitly allowed.

Great Tree structural composition retains independently generated tube parts
rather than silently merging their topology. Branch levels recurse from a stable
parent path; each child uses a path-scoped random stream and stable component
ID. The component budget admits this ordered traversal only up to its declared
limit, preserving the identity and mesh of every already admitted component.
This makes budget-limited LOD selection reproducible without implying a
watertight branch union.

### Determinism protocol

Named random streams are part of the compatibility contract, not a convenience
wrapper around JDK randomness. Protocol version 1 derives a stream seed using
SHA-256 over a fixed domain, the big-endian 64-bit root seed, and a stable UTF-8
stream name, then advances a specified SplitMix64 implementation. Consequently,
adding or opening one subsystem's stream cannot perturb another subsystem.

Fingerprints use typed, length-delimited SHA-256 input under their own versioned
domain. Continuous numeric results must be quantized with an explicit step
before hashing. Generator, resolver, geometry, material, semantic, and request
schema versions travel together so an intentional algorithm change can invalidate
cached results without weakening repeatability inside a version.

## Generated textures as assets

Generated textures are first-class library entities with stable IDs, generator
and schema versions, input parameters, seed/fingerprint, color-space and channel
semantics, dimensions, source provenance, and exported artifacts. Geometry and
materials reference texture entities; they do not privately regenerate or
embed anonymous textures.

Texture generators are reusable providers—for example bark, cracked rock,
foliage masks, smoke noise, and semantic masks—and participate in caching and
deduplication like mesh generators.

The initial storage-neutral contract separates a canonical generation request
from its generated texture descriptor. A request contains stable texture ID,
dimensions, root seed, sorted normalized parameters, and sorted fingerprinted
upstream resources. Provider ID/version and parameter-schema version join those
inputs in a typed SHA-256 cache identity. The descriptor validates that identity
instead of accepting an arbitrary cache key, declares linear or sRGB treatment,
and assigns extensible stable semantics to explicit RGBA channels. Pixel buffers,
encoded artifacts, preview generation, and persistence are later boundaries and
must not alter this identity contract.

The first raster providers are a painterly longitudinal bark base and a clustered
foliage coverage mask. Both require exact bounded parameter sets, generate
deterministic immutable pixels from the request seed, and are registered as
trusted classpath services. Bark emits sRGB RGBA albedo/opacity channels;
foliage emits a linear single-channel coverage mask suitable for alpha clipping.
They are reference shared algorithms with versioned replacement points, not
final production art or permission for asset-family-private variants.

Raw texture pixels cross a separate immutable boundary as tightly packed,
dimension-checked byte data with an explicit channel format. Encoded artifacts
carry a safe portable relative path, media type, byte length, owning resource,
and content fingerprint; they do not own bytes or persistence behavior.

The initial export codec deterministically encodes all supported 8-bit pixel
formats as PNG. Single-channel, RGB, and RGBA payloads retain their native PNG
representation; two-channel payloads expand to RGBA for portable preview
interoperability. A local generated-resource cache is keyed by the validated
generation fingerprint and publishes artifact metadata only after content bytes
are in place. Lookups verify byte length and SHA-256 content identity, treating
missing or corrupt metadata/content pairs as cache misses.

Material recipes are engine-neutral generated resources. They select a stable
material-model ID and bind sorted stable input IDs to one-to-four-component
numeric values, flags, or texture references. A texture binding pins both the
texture resource reference and its generation fingerprint, so changing a seed
or source cannot silently reuse a material identity. Recipe/schema versions and
all typed inputs participate in the material fingerprint. jME material creation
is an adapter concern.

## VFX plugins

Particles and other effects are reusable plugin definitions. An asset requests
an effect by stable plugin ID and supplies bounded parameters, sockets, and
semantic colors. Smoke, flame, motes, pollen, falling leaves, and similar
effects should have a small number of shared implementations rather than
per-asset copies.

The initial plugin contract is trusted and repository-owned. It describes
configuration and attachment; dynamic third-party code loading remains deferred.

A VFX configuration is a versioned generated resource built from stable provider,
plugin, and configuration IDs; provider/schema versions; seed; sorted typed
parameters; and sorted socket attachments with local transforms. Numeric vectors,
flags, stable choices, and fingerprint-pinned resource references form the closed
engine-neutral value set. All inputs participate in a validated reproducibility
fingerprint.

Trusted compiled `VfxProvider` implementations are discovered with Java
`ServiceLoader` and placed in a deterministic stable-ID registry. Duplicate IDs,
unknown providers, and unsupported plugin/provider pairs are rejected. Discovery
does not imply dynamic downloaded-code loading, and the contract does not select
jME particle classes or a CPU/GPU simulation strategy.

The first concrete shared effect provider configures ambient pollen motes with
bounded rate, lifetime, size, RGBA color, and one or more stable socket
attachments. It produces only the validated engine-neutral configuration; a
later adapter owns actual emitter construction and rendering.

## Boundary and validation rules

- Geometry libraries do not depend on asset-family packages.
- Asset-family packages may depend on shared generation libraries.
- jME and glTF adapters depend on `ProtoMesh`; `ProtoMesh` does not depend on them.
- Engine-neutral triangulation and surface processing are shared by output
  adapters. Adapter packages are excluded from the core dependency ban; the API,
  topology, determinism, performance, triangulation, and surface packages remain
  jME-free.
- Database repositories persist metadata and entities but do not perform generation.
- UI code invokes services and previews results but contains no generation logic.
- Every constructive operation has topology invariant tests and deterministic
  fixtures before it is used in a family generator.
- Render conversion validates finite attributes, index ranges, winding,
  manifold expectations, semantic groups, and reproducible buffer ordering.

## Screen-space and detail target

Generated assets target a painterly high-fantasy presentation and will normally
occupy no more than roughly 200×200 screen pixels. Geometry should establish
silhouette, large overlaps, convincing taper, and enough irregularity to imply
scale. Fine bark, cracks, leaf breakup, and similar detail should primarily come
from textures, masks, normal response, and restrained vertex jitter.

This is not authorization for visibly faceted structural forms. A generated
tube uses at least eight vertices per ordinary ring unless a deliberately
coarser distant LOD has visual evidence supporting fewer. Ring resolution may
increase for broad trunks, junctions, hollows, or strongly silhouetted roots.

The target stage envelope is up to 10 visible islands at maximum-detail zoom and
up to 100 visible islands at overview zoom. Non-island stage content is primarily
textures, billboards, and UI. Asset detail tiers must therefore be selected by
projected screen size and validated in aggregate: near islands may retain richer
geometry and attachments, while overview islands require substantially reduced
topology, attachment density, shadow cost, and material complexity.

These endpoints also exist as executable `StagePerformanceTarget` contracts.
Their population limits are fixed; numeric triangle, draw-call, shadow, memory,
and frame-time budgets remain deliberately unset until representative stage
profiling produces evidence.
