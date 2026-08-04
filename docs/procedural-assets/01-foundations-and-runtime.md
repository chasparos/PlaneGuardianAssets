# Foundations and Runtime Architecture

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 1. Purpose

This document defines the initial architecture and visual contract for deterministic procedural assets in PlaneGuardian. It also specifies the first reference generator: a large deciduous tree suitable for use as a Land landmark or attachment.

The system must allow the game to:

- Select an asset or generator by stable identifier.
- Derive visual parameters from a card's semantic wheels.
- Produce deterministic geometry, materials, variants, and effects.
- Use the same semantic contract for vegetation, mountains, towers, and other asset families.
- Preserve jMonkeyEngine's normal PBR lighting and shadow behavior.
- Keep expensive detail appropriate to the game's normal camera distance.
- Blend an attachment's native identity with its host Land without allowing the host to overwrite it.
- Reproduce historical assets after generators evolve.

This document intentionally defines only the semantic subset needed by the asset repository. The GDD remains authoritative for gameplay and lore meaning.

## 2. Core principles

### 2.1 Semantics are resolved before rendering

Shaders must not independently interpret the complete lore-wheel model. A versioned semantic resolver converts card semantics into a stable `LandVisualProfile`. An asset-family adapter then converts that profile into parameters understood by a particular generator.

The intended pipeline is:

```text
CardSemanticProfile
    -> LandVisualProfile
    -> Asset-family parameter generator
    -> Geometry recipe + material parameters + VFX recipe
    -> jME scene subtree
```

The same `LandVisualProfile` can produce leaf coverage in a tree, erosion in a mountain, or symmetry in a mage tower. The common contract is the input language, not identical behavior from every asset.

### 2.2 Geometry, material, and effects are separate outputs

Structural facts belong in geometry generation or variant selection:

- Presence or absence of foliage.
- Branch count and crown shape.
- A hollow trunk.
- Large vines, roots, flowers, or fruit clusters.
- Floating or missing pieces.

Surface facts belong in materials:

- Bark and foliage palette.
- Dryness and discoloration.
- Roughness, normal strength, and restrained emission.
- Small moss coverage.
- Host-Land edge tinting.

Transient or spatial facts belong in VFX:

- Falling leaves.
- Pollen, spores, motes, and embers.
- Spectral drift.
- Magical pulses.

Do not generate a full leaf canopy and hide it with fragment discard to represent a leafless tree. That wastes rendering work, complicates shadows, and leaves the wrong silhouette.

### 2.3 Asset identity and host influence have dual authorship

An attachment owns its recognizable model, silhouette, native materials, and primary palette. The host Land supplies a bounded integration layer such as root contact color, edge tint, moss suitability, dust, snow, local emission, or a projected seam.

As a starting visual rule:

- Approximately 80-90% of an attachment should retain its native identity.
- Approximately 10-20% may express the host Land at contact regions and selected secondary surfaces.

### 2.4 Determinism is versioned

Visual output is deterministic only within an explicit set of versions. A cache key must include at least:

```text
Universe generation version
Card semantic schema version
Visual resolver version
Asset identifier
Asset-family adapter version
Geometry generator version
Shader/material-contract version
Render tier
```

All random choices use named substreams derived from the card or visual seed. Changing flower placement must not move the trunk or alter attachment sockets.

## 3. Deliverables and runtime code

### 3.1 Paired build artifacts

`PlaneGuardianAssets` should eventually build two paired artifacts:

1. `planeguardian-assets.pglib`
   - `asset_index.json`
   - glTF/GLB assets
   - Textures and masks
   - jME material definitions and shaders
   - Declarative asset metadata

2. `planeguardian-assets-runtime.jar`
   - Semantic adapters
   - Parameter generators
   - Runtime geometry generators where required
   - Asset controllers and validation code

A bundle manifest binds their hashes and compatibility versions.

For the first implementation, the runtime JAR should be an ordinary build dependency of the game client. Do not dynamically load executable code out of the binary asset library yet. This keeps startup, debugging, native packaging, and security straightforward.

### 3.2 Service-provider interface

Keep the shared API small and versioned. Providers can be registered through Java's standard `ServiceLoader` mechanism.

```java
public interface ProceduralAssetProvider {
    String providerId();

    int apiVersion();

    boolean supports(String generatorId);

    GeneratedAsset generate(AssetGenerationRequest request);
}
```

```java
public record AssetGenerationRequest(
        String assetId,
        String generatorId,
        long visualSeed,
        LandVisualProfile landProfile,
        InstanceVisualState instanceState,
        HostVisualProfile hostProfile,
        RenderTier renderTier) {
}
```

The provider should return a scene description or constructed jME `Node` together with diagnostic metadata and a reproducibility fingerprint.

### 3.3 Future hot-loaded bundles

If the game later needs downloadable generator bundles, use a dedicated class loader or module layer plus `ServiceLoader`. Only load code that is:

- Signed by a trusted project key.
- Hash-matched by the bundle manifest.
- Compatible with the exact asset SPI version.
- Loaded from a local verified bundle, never an arbitrary network URL.
- Isolated from application implementation packages as far as practical.

This is an update/plugin mechanism, not a security sandbox. Java class loading does not make untrusted code safe.

### 3.4 Why Byte Buddy is not the default

Byte Buddy creates or transforms JVM classes at runtime. The current problem does not require generating classes; it requires discovering and running authored implementations of stable interfaces. Compiled provider classes plus `ServiceLoader` solve that directly.

Byte Buddy would add complexity to caching, diagnostics, ahead-of-time packaging, compatibility, and trust without simplifying the generator algorithms. Reconsider it only if a future feature genuinely requires runtime-specialized bytecode.

### 3.5 Data-driven rules remain data

Simple asset-local selection may be expressed declaratively in JSON. Do not embed JavaScript in glTF `extras`.

An initial rule vocabulary may contain only:

- `select`
- `threshold`
- `clamp`
- `lerp`
- `curve`
- `add`
- `multiply`
- Seeded choice from an explicitly named random stream

Begin with Java adapters and introduce this rule vocabulary only after repeated patterns are known. An unrestricted script engine would weaken reproducibility, validation, security, tooling, and error reporting.
