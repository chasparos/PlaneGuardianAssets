# How to add a manual asset generator

This guide is a fast path for implementing a new procedural asset provider. It
describes the current registered authoring workflow, not the older
`AssetGenerator` Swing contract.

## 1. Pick the boundary

Keep the generator engine-neutral until the preview/export boundary:

```text
your provider
  -> reusable curves/surfaces/geometry/topology
  -> generated asset/product data
  -> jME or glTF adapter
```

Do not put jMonkeyEngine `Mesh` objects in geometry algorithms. The editable
mesh type in this repository is `ProtoMesh`; there is no class named
`GenerativeMesh`. Treat “generative mesh” as the construction stage that
produces a `ProtoMeshSnapshot` and, later, a processed `RenderMesh`.

Before inventing a primitive, read
[`docs/guides/geometry-toolkit.md`](docs/guides/geometry-toolkit.md) and compose
the existing operations.

## 2. Create the provider class

Add a class under the consolidated
`com.planeguardian.assets.assetgenerator` namespace implementing
`AuthoringGeneratorProvider`. Keep generator-specific code in a named package
such as `.assetgenerator.rock`, with semantic mapping in `.semantics` and file
writing in `.export`:

```java
public final class RockAssetGenerator implements AuthoringGeneratorProvider {
    public static final String GENERATOR_ID = "pg.rock/1";

    @Override
    public GeneratorDescriptor descriptor() {
        // Declare parameters, defaults, roles, sockets, and formats.
    }

    @Override
    public GenerationResult generate(AuthoringGenerationRequest request,
                                      Path outputDirectory) {
        // Parse, resolve, generate, adapt, and write the result.
    }
}
```

The provider should be responsible for asset-family policy and composition.
Reusable geometry belongs in `generation.geometry`, `generation.curves`,
`generation.surfaces`, or `generation.topology`, not in a workbench-specific
class. `SampleAssetGenerator` is the shortest current worked example;
`CrystalAssetGenerator` and `GreatTreeAssetGenerator` demonstrate larger
parameter schemas. New generator implementations should use the
`assetgenerator.[generator].[generation|semantics|export]` layout, with the
main `*AssetGenerator` class at `assetgenerator.[generator]`.

## 3. Publish the provider in the registry

The registry uses Java `ServiceLoader`. Add one fully qualified class name to:

```text
src/main/resources/META-INF/services/com.planeguardian.assets.tools.generator.AuthoringGeneratorProvider
```

For example:

```text
com.planeguardian.assets.assetgenerator.rock.RockAssetGenerator
```

`AuthoringGeneratorRegistry` loads providers, sorts them by
`descriptor().generatorId()`, and rejects duplicate IDs. Do not add
generator-specific `if` statements to the registry or generic workbench.

## 4. Define the descriptor

`GeneratorDescriptor` is the workbench-facing contract. Give the generator:

* a stable `generatorId` and bumped `ContractVersion`;
* an `assetFamily` ID shared by its semantic adapter;
* a display name;
* direct `Parameter` entries;
* optional named `Preset` values;
* declared runtime `capabilities`, preview/export formats, semantic `roles`,
  and `sockets`;
* the IDs of parameters that may be filled by semantic resolution.

Each `Parameter` needs a stable ID, display name, value type, default string,
allowed-value/range description, and advanced flag. Keep IDs stable after
release: saved authoring requests and presets use these strings.

Example:

```java
parameter("rock.radius", "Radius", "number", "0.8", "[0.05,4]", false)
```

The descriptor validates duplicate parameter and preset IDs, and validates that
every semantic-derived ID is also declared as a parameter.

## 5. Map request strings into typed model values

`AuthoringGenerationRequest` supplies:

* `assetName`;
* deterministic `visualSeed`;
* sorted `directParameters` (`Map<String, String>`);
* source `SemanticProfile`;
* `explicitOverrides`.

Parse the strings once at the provider boundary, validate ranges/enums, and
construct a typed model such as `CrystalParameters` or a new
`RockParameters` record. Do not pass string maps into geometry code.

The normal mapping sequence is:

1. Start with descriptor defaults or a selected preset.
2. Read direct request values by stable parameter ID.
3. Parse numbers with `Double.parseDouble`/`intValue` and enums with a
   controlled enum conversion.
4. Resolve semantics before constructing the typed generator parameters.
5. Build geometry from the typed parameters and the visual seed.
6. Create materials/resources from the same resolved profile.
7. Convert the final `RenderMesh` at the jME/glTF boundary and write output.

The seed belongs in deterministic generation and fingerprints. Output names
must be sanitized before creating files.

## 6. Differentiate semantic and direct parameters

Implement `semanticAdapter()` when the asset family has semantic behavior.
`AssetSemanticAdapter.resolve` translates the source Lore Map into a
`ResolvedVisualProfile` of visual channels. The generator descriptor identifies
which direct parameters can receive those channels through
`semanticDerivedParameters`.

Use `ParameterPrecedence.resolve`:

```java
Map<String, Double> resolved = ParameterPrecedence.resolve(
    descriptor(), directValues, semanticProfile, request.explicitOverrides());
```

The rule is:

1. ordinary direct values establish the baseline;
2. semantic channels replace only declared semantic-derived parameters;
3. an ID in `explicitOverrides` keeps the user’s direct value.

This distinguishes **meaning** from **shape controls**. For example, a semantic
profile may derive crystal opacity, emission, or size, while facet count and
cut style remain generator-specific direct choices. Do not let geometry,
materials, or VFX independently reinterpret the complete Lore Map. If a
parameter is semantic-derived, document its channel and fallback behavior.

Runtime environment values (wind, weather, elapsed time) are not semantic
identity and must not alter deterministic generation fingerprints.

## 7. Build the generative mesh

Use the following construction order:

1. Define a curve or surface in engine-neutral coordinates.
2. Sample curves by arc length when spacing matters.
3. Generate rings, patches, or faces using shared generators.
4. Add semantic groups and per-corner UV/normal data while topology is mutable.
5. Publish an immutable `ProtoMeshSnapshot`.
6. Validate topology; do not commit an invalid transaction.
7. Triangulate and process normals/tangents only after topology is complete.
8. Adapt the resulting `RenderMesh` to jME or glTF.

Use right-handed glTF coordinates: `+Y` up, `+Z` forward, metres, radians, and
counter-clockwise front faces.

## 8. Save and return a result

Create the output directory, write the primary artifact, and return
`GenerationResult.success(...)` with the generator ID and a reproducibility
fingerprint. Catch expected I/O and validation failures and return
`GenerationResult.failure(...)`; do not silently produce a partial artifact.

For a jME preview, `JmeMeshAdapter.convert(renderMesh)` is the boundary
conversion. Set stable user data such as `pg.generatorId` and asset/socket IDs
on the resulting scene. Keep preview-only code out of the generation library.

## Generative mesh / ProtoMesh quick reference

| Entry | Use it for |
| --- | --- |
| `ProtoMeshBuilder` | Mutable vertices, polygon faces, corner attributes, and semantic groups. |
| `ProtoMeshSnapshot` | Immutable, deterministic topology result with derived edges and diagnostics. |
| `ProtoMeshEditTransaction` | Safe topology edits with preview/commit/rollback. |
| `CornerAttributes` | Per-corner UVs, normals, and scalar layers; use for seams. |
| `SplineTubeGenerator` | Arc-length-spaced rings along a curve; quad sides and tube ends. |
| `QuadSurfacePatchGenerator` | Fixed-resolution UV-mapped quad surfaces. |
| `RingBridgeOperation` | Joining equal-resolution rings. |
| `UnequalRingBridgeOperation` | Reviewed transitions such as 4↔8 or 8↔16. |
| `RingCapOperation` / `RingFillOperation` | Closing open rings; choose winding deliberately. |
| `FaceInsetOperation` + `FaceExtrudeOperation` | Hard-surface collars and relief. |
| `VertexWeldOperation` | Merging coincident, non-adjacent seam vertices. |
| `ProtoMeshTriangulator` | Deterministic render-boundary triangles. |
| `MeshSurfaceProcessor` | Explicit flat/smooth/authored normals and optional tangents. |

Stable IDs are monotonic and retired IDs are never reused. Preserve semantic
groups when replacing faces. Prefer quads during authoring, but allow reviewed
triangles at caps, poles, and the final render boundary.

## Math library quick reference

The math package is engine-neutral and uses immutable `Vector3` values:

| Entry | Use it for |
| --- | --- |
| `VectorMath.add` / `subtract` | Point and direction arithmetic. |
| `VectorMath.scale` | Uniformly scaling a vector. |
| `VectorMath.dot` | Projection, angles, and alignment tests. |
| `VectorMath.cross` | A perpendicular vector and winding/orientation checks. |
| `VectorMath.length` / `lengthSquared` | Magnitude; use squared length for comparisons. |
| `VectorMath.distance` | Distance between two points. |
| `VectorMath.normalize` | Unit directions; rejects near-zero vectors. |
| `VectorMath.reject` | Remove the component along a unit axis (plane projection). |
| `VectorMath.rotateAroundUnitAxis` | Rodrigues rotation around a **unit** axis. |
| `VectorMath.clamp` | Bound a scalar parameter to a legal interval. |

Use `StrictMath`-based deterministic operations already used by the library,
check finite/positive inputs at request boundaries, and keep angles in radians
internally. `VectorMath.reject` and `rotateAroundUnitAxis` require a correctly
normalized axis; normalize it first when it is not guaranteed to be unit length.

## First-run checklist

- [ ] Provider implements `AuthoringGeneratorProvider`.
- [ ] Descriptor IDs, version, parameter ranges, roles, sockets, and formats are declared.
- [ ] Provider class is listed in the `ServiceLoader` file.
- [ ] Direct strings map to a typed parameter object.
- [ ] Semantic-derived IDs and explicit override behavior are tested.
- [ ] Geometry uses shared ProtoMesh/curve/surface facilities.
- [ ] Topology, finite values, deterministic seed, and fingerprint are validated.
- [ ] jME/glTF conversion occurs only at the boundary.
- [ ] Generation returns a success or failure result and writes no partial output.
