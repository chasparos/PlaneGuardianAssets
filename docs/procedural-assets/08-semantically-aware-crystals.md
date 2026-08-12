# Semantically Aware Crystals Generator

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 14. Purpose and scope

This document defines the technical design for the "Semantically aware
crystals" generator item of the "Parallel arc: Expand the generator library"
roadmap (`.steadyarc/roadmap.md`). It is a second registered generator that
exercises the same generic `AuthoringGeneratorProvider` platform proven by the
Deciduous Great Tree; it does not change platform contracts, the Great Tree
implementation, or Record 1's active roadmap items.

The generator produces a crystal or crystal-cluster asset whose color, shape,
size, complexity, radiance, VFX decorators, and host setting are all derived
from a bounded, inspectable semantic asset profile, rather than from
crystal-private ad hoc heuristics.

## 15. Semantic inputs

### 15.1 Existing wheels

The crystal semantic adapter consumes the standard Lore Map wheels already
defined in `StandardSemanticWheels` (Elemental Affinity, Ethos, World
Relation, Cosmic Provenance, Magical Tradition, Manifestation), each carrying
2D direction/extremity plus independent Salience as described in
[Semantic inputs and visual contracts](03-semantic-visual-contracts.md).

### 15.2 New wheels: Power, Rarity, Quality

Three new wheel dimensions are added because they are meaningful in the game
domain (item power level, drop rarity, craftsmanship/purity quality) and need
an explicit abstraction boundary before an asset generator may use them:

```text
Power:   Dormant (-1)   <-> Overwhelming (+1)
Rarity:  Common (-1)    <-> Legendary (+1)
Quality: Flawed (-1)    <-> Flawless (+1)
```

Each follows the same wheel contract as existing dimensions: a normalized
`x`/`y` (or angle/extremity) coordinate plus Salience, optionally with a
`CenterRelation` where a synthesis or conflict between the poles is
meaningful (for example, a flawed-but-powerful crystal is a `CONFLICT` center
relation on Quality vs. Power, not an averaged mid-value).

These wheels are **game-domain values**, not raw gameplay stats. The asset
generation platform never reads gameplay inventory/item state directly. The
abstraction boundary is:

- The game domain (or an authoring tool acting on its behalf) resolves an
  item's power/rarity/quality into wheel coordinates using its own rules.
- The asset repository receives only the resolved `SemanticProfile` wheel
  values, exactly as it already does for Elemental/Ethos/etc. No new coupling
  to gameplay item types, inventory, or economy code is introduced.
- The crystal semantic adapter maps these wheel values, alongside the
  existing wheels, into direct crystal parameters through the same
  `AssetSemanticAdapter`/contribution-trace mechanism used elsewhere.

Documentation: extend `docs/procedural-assets/03-semantic-visual-contracts.md`
section 5 with a "Power, Rarity, and Quality" subsection describing this
abstraction boundary and per-axis visual intent (see 15.3), and register the
wheels in `StandardSemanticWheels` alongside the existing Lore Map wheels.

### 15.3 Visual intent

| Wheel | Low pole | High pole | Suggested influence |
|---|---|---|---|
| Power | Dormant | Overwhelming | Radiance intensity, emissive strength, VFX decorator density/scale |
| Rarity | Common | Legendary | Facet complexity, cluster count, palette accent selection, setting elaborateness |
| Quality | Flawed | Flawless | Surface clarity/transparency, facet regularity, inclusion/crack decorators |

These are starting mappings to be tuned visually and refined by the semantic
adapter's contribution trace and golden cases, not literal shader formulas.

## 16. Crystal semantic asset profile

A new, engine-neutral, reusable abstraction —`CrystalSemanticAssetProfile`
(`com.planeguardian.assets.assetgenerator.crystal.semantics`) — is derived once per
generation request from the full resolved `SemanticProfile` (all wheels,
including Power/Rarity/Quality). It exposes bounded, inspectable channels that
every downstream aspect of the generator reads from, instead of each aspect
re-deriving its own semantics independently:

```text
CrystalSemanticAssetProfile
  colorCharacter      -> hue/saturation/value bias + refraction tint
  shapeCharacter       -> facet sharpness, growth direction/axis bias, cluster tendency
  sizeCharacter        -> scale/prominence
  complexityCharacter  -> facet count/irregularity, cluster member count
  radianceCharacter    -> emissive strength, glow falloff, pulse/flicker suitability
  vfxDecoratorCharacter -> which shared VFX providers are suitable and their intensity
  settingCharacter     -> host/setting family (natural rock, clean levitation + mist, etc.) and its strength
```

Each channel keeps a contribution trace back to the wheels/axes that produced
it, following the same "inspectable contribution trace" standard already
established for the Great Tree's semantic adapter. This profile itself is
generic and reusable: a later "crystal sword" or other gem-bearing generator
should be able to consume the same profile abstraction (and the global
palette in section 17) to keep its own color/material selection consistent
with the crystal generator, without depending on the crystal generator's
package.

## 17. Global named-color palette

A new shared, generator-agnostic facility is introduced: a global named-color
palette (expected under `com.planeguardian.assets.generation.palette`, e.g.
`NamedColorPalette` holding `PaletteEntry(StableId id, String displayName,
ColorRGBA color, String semanticIntent)` entries). Its purpose is to give
every current and future generator a shared vocabulary of named, curated
colors (for example `gem.ruby-core`, `gem.emerald-core`, `arcane.violet-glow`,
`neutral.quartz-clear`) instead of re-deriving one-off semantic colors inside
each generator.

- The palette is seeded initially with the entries the crystal generator
  needs (elemental/rarity-driven hues, neutral/clear variants, emissive
  accent variants), not an exhaustive color library speculatively built
  ahead of need.
- Palette entries are looked up by stable ID, never by generator-local
  literals, so a future crystal-sword (or any other) generator can reference
  the same named colors for visual consistency.
- The palette itself does not encode semantic-resolution logic; it is a
  lookup table. The crystal semantic adapter is responsible for selecting
  and blending palette entries based on the resolved `colorCharacter`.
- This is a genuinely new shared facility (not crystal-private), so it
  belongs beside the other reusable generated-resource systems described in
  [Reusable Generation Platform](../architecture/generation-platform.md), and
  should be documented there once implemented.

## 18. Geometry and topology

The crystal generator composes the existing reusable curve/constructive-
geometry library and ProtoMesh kernel (splines, rings, lofts, extrude/inset/
bridge/fill/cap/weld/split, loop transitions) documented in
[the geometry toolkit guide](../guides/geometry-toolkit.md). Before writing
any crystal-private mesh construction code, the implementation must check
whether an equivalent hard-surface/faceted operation already exists; if not,
add it to the shared library and its guide rather than embedding it in the
crystal package. Expected candidate gaps to evaluate:

- Planar facet cutting/faceting of a base solid or spline-lofted volume into
  a low-poly gem-like faceted shell.
- Deterministic clustering/packing of multiple crystal instances around a
  shared base without manual per-instance placement code.
- A pointed/tapered cap variant of the existing collar/cap operations for
  crystal tips.

Any additions are recorded in `docs/guides/geometry-toolkit.md` following its
existing style, and in `.steadyarc/engineering-notes.md` once implemented.

Structural output requirements match the standard already used by the Great
Tree: valid topology, normals, tangents, bounds, structural LODs, and stable
roles/sockets (for example a "gem" role and a "setting-attachment" socket).

### 18.1 Canonical faceted forms

Crystal bodies are hard-surface geometry, not organic spline tubes. The
generator emits bounded canonical silhouettes: 4-sided tetrahedral/prismatic,
6-sided hexagonal-prismatic, or 8-sided octahedral/brilliant forms. Taper and
the explicit `PRISM`, `CUSHION`, and `BRILLIANT` cut style modify those forms
without turning facet count into an unbounded mesh setting. Quality and Rarity
contribute to the resolved discrete form, while the published facet-count
channel remains inspectable and can be overridden by authoring.

Every exported crystal package contains the geometry variants needed for these
forms. Runtime semantic application selects and hides the pre-baked variant;
it must never call generation code or rebuild topology in the game.

## 19. Setting / host presentation

The crystal's setting — what it visually sits in or is presented by — is a
semantic-profile-derived, swappable component, selected via the resolved
`settingCharacter` channel. Initial setting families:

- **Natural rock**: an irregular rock/matrix host the crystal cluster grows
  from or out of, reusing shared geometry (rock-like faceted/organic hybrid
  forms) and shared surface texture providers.
- **Clean levitation**: the crystal floats free of a solid host, optionally
  accompanied by shared mist/smoke VFX decorators for ambience.

Both settings are implemented by composing existing/extended shared geometry,
texture, and VFX providers, never as crystal-private one-off mesh/texture
code. The setting choice must not become a hidden second parameter set;
whichever direct parameters control it must appear in the generator's
published parameter schema, with the semantic adapter only supplying the
default/derived selection.

## 20. Material: transparency, reflection hint, and emission

Visual goal: crystals must read as translucent and subtly reflective without
implementing real ray-traced or planar reflection, while still carrying
enough hue-true emissive color that a red gem reads as glowing red rather
than a washed-out white/black shape under lighting.

Material approach, extending the shared jME PBR/VFX conventions in
[Rendering and performance](05-rendering-and-performance.md):

- **Transparency**: alpha blending (or alpha-clip plus a secondary transparent
  pass for thin facets) driven by a `PgCrystalOpacity`-style parameter derived
  from `colorCharacter`/Quality, not a fixed constant.
- **Reflection hint**: a cheap, bounded technique — a static/shared
  environment (skybox) reflection sample combined with a Fresnel-driven rim
  term — is sufficient to hint at reflectivity. This is explicitly not a
  per-asset planar/ray-traced reflection; it reuses whatever shared
  environment-reflection resource the rendering path already exposes (or a
  new minimal shared cubemap resource if none exists, added to the reusable
  texture/material systems rather than crystal-private code).
- **Emission**: `PgPaletteEmissive`/`PgEmissionStrength` (existing shared
  material parameters) are driven by the resolved `radianceCharacter`, with
  the emissive color explicitly tinted to the resolved hue rather than
  defaulting toward white. Power (from section 15) is the primary driver of
  emission strength; Rarity/Quality modulate clarity and facet regularity.

Any new shared material parameters this requires are documented alongside the
existing `PgPalette*` list in
[Rendering and performance](05-rendering-and-performance.md) once implemented,
rather than invented ad hoc inside the crystal generator.

## 21. VFX decorators

VFX decorators (e.g. sparkle motes, ambient glow pulse, mist/smoke for the
levitation setting) are requested through the existing reusable VFX plugin
configuration/provider registry, selected and scaled by
`vfxDecoratorCharacter`. No generator-private VFX raster or plugin code is
added; new shared VFX providers are added to the reusable generated-resource
systems only if a genuine gap is demonstrated.

## 22. Validation

The crystal generator follows the same validation standard as the Great Tree
reference generator:

- Schema validity and determinism (same request/seed/semantic profile yields
  the same engine-neutral product).
- Topology/render validation (manifold checks, normals/tangents, bounds,
  export-buffer fixtures).
- Fixed semantic golden cases covering at least: a common/low-power/flawed
  crystal, a legendary/overwhelming/flawless crystal, an elementally-opposed
  pair (e.g. Fire vs. Ice affinity), a natural-rock setting, and a clean-
  levitation setting.
- Automated structural/regression tests are necessary but not sufficient;
  human visual validation (silhouette, transparency/reflection read, emissive
  hue correctness) is recorded separately, matching the Great Tree standard.

Runtime validation must also prove that changing a resolved semantic profile
selects a packaged geometry variant without changing the asset fingerprint or
invoking a generator.

## 23. Non-goals

- No real-time ray tracing, screen-space reflection, or planar reflection
  probes are introduced.
- No gameplay item/inventory/economy coupling is introduced; Power, Rarity,
  and Quality enter the asset repository only as resolved semantic wheel
  values.
- No workbench- or registry-level crystal-specific conditionals; the
  generator is discovered purely through `AuthoringGeneratorProvider`.
