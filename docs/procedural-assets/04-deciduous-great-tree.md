# Deciduous Great Tree Generator

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 7. Deciduous Great Tree generator

### 7.1 Visual goal

The tree is a large silhouette-defining landmark viewed primarily from the Plane camera. Individual leaves are not expected to be readable. The generator should produce a believable trunk, branch hierarchy, and crown mass with restrained painterly detail under normal jME lighting.

The tree must remain recognizable across these semantic extremes:

- Healthy mundane oak-like tree.
- Ordered and cultivated living tree.
- Chaotic, ancient, twisted tree.
- Native Death-aligned leafless tree.
- Fey or Primal tree with unusual foliage.
- Incorporeal Aether-touched tree.
- Tree with a native identity planted in an off-color host Land.

It is not required to reproduce a botanically exact species. It should produce coherent high-fantasy deciduous forms.

### 7.2 Parameter groups

The parameter generator returns immutable `DeciduousTreeParameters` divided into structural, crown, surface, feature, and motion groups.

Every direct control that materially affects generation must also be declared in
the tree generator's versioned parameter schema. This includes structural,
crown, root, surface, feature, motion, randomization, resolution, and LOD values;
there must be no generator-private tuning constants that cannot be inspected,
serialized, reproduced, and manipulated by tooling. Presets may supply coherent
groups of values but do not replace the underlying parameters.

Derived semantic channels are not published as a second editable parameter set.
The editable inputs are the semantic wheels and their source context; the
resolver's bounded derived profile and contribution trace are read-only outputs.
A later visual-validation UI should provide semantic wheel controls, seed and
render-tier selection, live regeneration, and inspection of the derived values.

The tree parameter schema must additionally supply enough natural-language and
constraint metadata for an AI assistant to propose a coherent starting preset
from a description such as “great oak.” AI proposals include a rationale and
schema version, then pass through the normal parameter validator. They do not
introduce private parameters or directly edit resolver-derived semantic values.

```java
public record DeciduousTreeParameters(
        TreeStructure structure,
        CrownParameters crown,
        TreeSurfaceParameters surface,
        TreeFeatureParameters features,
        TreeMotionParameters motion,
        TreeLodParameters lod) {
}
```

Important structural parameters:

- Total height.
- Base trunk radius and root-flare radius.
- Trunk taper exponent.
- Trunk lean and curvature.
- Trunk twist.
- Primary branch count.
- Branching depth.
- Branch attachment height range.
- Mean branch elevation angle.
- Branch droop.
- Branch taper.
- Branch tortuosity.
- Crown width, height, asymmetry, and vertical offset.

Feature parameters:

- Hollow-trunk presence, position, width, and height.
- Root prominence.
- Moss coverage.
- Vine count and thickness.
- Flower density and scale.
- Fruit density, scale, and clustering.
- Fungal or bracket-growth density where appropriate.

Foliage parameters:

- Coverage.
- Crown-lobe count.
- Cluster density.
- Cluster size and anisotropy.
- Edge breakup.
- Interior darkness.
- Alpha cutoff.
- Color variation.

### 7.3 Deterministic streams

Derive independent streams from the root visual seed:

```text
tree.structure
tree.trunkSpline
tree.primaryBranches
tree.secondaryBranches
tree.roots
tree.hollow
tree.crownField
tree.foliageClusters
tree.moss
tree.vines
tree.flowers
tree.fruit
tree.materialVariation
```

Use a specified hash and pseudo-random generator rather than `java.util.Random` defaults. The algorithm and numeric quantization belong to the generator version.

### 7.3.1 Recursive branch-level controls

The structural tree is a parameterized recursive branch graph. Level 0 is the
trunk, level 1 contains primary branches, level 2 contains their branches, and
so on until the configured depth or generation budget is exhausted. Each level
has its own bounded controls for:

- branch frequency or child-count range;
- allowed attachment interval along the parent;
- elevation, azimuth, spread, and angle variation;
- length and radius relative to the parent;
- taper, curvature, twist, droop, and tortuosity;
- minimum separation, pruning, and termination probability;
- spline sample density and tube-ring resolution.

Every accepted trunk, branch, and major root is represented by a spline and
meshed through the shared spline-tube/loft library. Random choices come from
named streams scoped by structural path so changing a secondary level does not
move the trunk or unrelated primary branches.

### 7.4 Trunk spline

Represent the trunk as a cubic Hermite or Catmull-Rom centerline. Generate a small number of control points from height, lean, curvature, and low-frequency noise.

Extrude rings along the spline using a rotation-minimizing or parallel-transport frame. This avoids the sudden twisting that can occur with a Frenet frame near low-curvature segments.

For each ring:

```text
center = trunkSpline(t)
radius = baseRadius * taper(t) * localRadiusNoise(t)
vertex(i) = center + frameNormal(i) * radius
```

The radial profile should include low-frequency irregularity. Do not spend vertices on small bark noise; that belongs in normal maps and material masks.

Generate UVs from arc distance along the spline and angle around the ring. Generate normals and tangents after geometry is finalized.

### 7.5 Branch graph and branch splines

Create a branch graph before mesh construction. Primary branches attach within a configurable trunk-height interval. Use a golden-angle or phyllotactic starting distribution, then perturb it according to Chaos and the deterministic branch stream.

Each branch is a tapered spline with:

- Parent attachment position and frame.
- Initial direction.
- Length and radius.
- Gravitropic response.
- Phototropic/upward response.
- Droop.
- Tortuosity.
- Child-branch budget.

Order should increase angular regularity, spacing, balanced crown occupancy, and similarity between corresponding branches. Chaos should increase asymmetric omission, directional changes, unequal lengths, and local torsion. Chaos must not create obviously disconnected or self-intersecting branches without bounds.

Branch junctions may initially overlap slightly inside the parent volume instead of requiring expensive watertight union operations. The overlap must not be visible from normal viewing distances. Merge resulting branch meshes by material to reduce draw calls.

### 7.6 Root geometry

Root generation exposes root-flare width and height, major-root frequency,
radial distribution, thickness, taper, surface-following strength, and the
fraction of each root intended to remain above ground. Roots are spline-based
and use the same reusable tube/loft operations as branches, with contact and
burial information emitted separately for host integration.

A density field may later guide root or branch occupancy and collision
avoidance, but it is not required for the first structural proof. A controlled
quad-first junction that transitions a parent surface into an eight-vertex or
higher child ring is sufficient for the POC.

Generate four to eight major root splines from the base ring. Rooted Manifestation increases:

- Root-flare radius.
- Visible root length.
- Downward contact.
- Apparent weight and terrain penetration.

Wandering Manifestation may produce asymmetric trailing roots or a slight loss of contact, but attachment sockets and gameplay foundations remain stable.

Roots end below or inside the host-contact volume. The generated node exposes a `hostContact` mask or vertex channel so the host palette can blend into the lowest roots and nearby moss.

### 7.7 Hollow trunk

Do not begin with general-purpose constructive solid geometry. A convincing local hollow is sufficient at the target scale.

The initial hollow implementation should:

1. Select a lower-trunk location and orientation.
2. Deform or omit outer trunk faces in an oval aperture.
3. Generate a darker recessed inner shell.
4. Generate an irregular rim connecting the outer surface to the inner shell.
5. Optionally add a short interior floor and broken fibers.

Expose `hollowRim` and `hollowInterior` as separate semantic surfaces. The interior should be materially dark under ordinary lighting, not dependent on a pure-black unlit texture.

Death, Transformation, Chaos, and Annihilation may increase hollow probability or size. A generated random draw still decides whether the feature appears; semantic values bias the probability rather than mandating identical trees.

### 7.8 Crown density field

Build the crown from a continuous density field seeded by branch tips and selected interior branch positions. Each seed contributes an anisotropic ellipsoidal field:

```text
d_i(p) = weight_i * exp(-length(T_i * (p - center_i))^2)
density(p) = sum(d_i(p)) - exclusion(p)
```

`T_i` controls the ellipsoid orientation and squash. Exclusion fields keep foliage out of the trunk interior, hollows, major branch corridors, and deliberately open silhouette gaps.

The density field is used for:

- Crown-lobe placement.
- Foliage-cluster scale.
- Interior versus exterior shading masks.
- Placement probability for flowers and fruit.
- Crown silhouette validation.

It does not require generating a single marching-cubes canopy. A union of several irregular cluster shells is easier to control and optimize.

### 7.9 Foliage representation

Use a hybrid cluster-shell approach as the initial implementation.

Each foliage cluster is a low-poly, irregular, squashed ellipsoid aligned to its supporting branch and the crown field. Its material uses:

- An alpha-clipped leaf-mass texture rather than conventional alpha blending.
- A normal texture representing groups of leaves.
- A semantic color mask.
- Per-vertex or texture-based variation.
- A darkened interior factor based on crown-field depth.

Avoid using only two crossed billboards for the whole crown. They produce obvious viewing-angle changes and a weak silhouette. Bi-planar or tri-planar cards remain useful for:

- Small outer crown tufts.
- Hanging leaves.
- Moss curtains.
- Flowers and fruit clusters.
- Distant LODs.

Recommended LOD strategy:

- **LOD 0:** Trunk and branch mesh plus multiple irregular cluster shells and a small number of outer silhouette cards.
- **LOD 1:** Reduced branch depth and fewer, larger cluster shells merged by material.
- **LOD 2:** Coarse trunk silhouette plus a small crown mesh or pre-generated impostor.

Foliage geometry is omitted when coverage falls below the leafless threshold. Sparse foliage uses fewer clusters positioned toward viable branch tips, not a full crown with increased transparency.

### 7.10 Bark, moss, and vines

Bark uses standard jME PBR lighting with base-color, normal, and roughness inputs. Semantic colors modify role-based palette regions before the PBR lighting calculation.

Generate a moss suitability mask from:

- Moisture and Water influence.
- Life influence.
- Low height and protected creases.
- Surface orientation.
- Host contact.
- Deterministic low-frequency noise.

Low and moderate moss coverage should be a material blend. Only strong hanging moss should add card or ribbon geometry.

Vines are separate splines constrained toward the trunk and major branches. Render important vines as narrow tubes or camera-tolerant ribbons. Vine leaves use small clusters rather than individual geometry. Transformation, Life, Primal tradition, Water, and host suitability may increase vine presence.

### 7.11 Flowers and fruit

Flowers and fruit are optional feature groups and must remain legible at the target camera scale. Represent them as clustered color masses, small cards, or simple low-poly forms rather than many detailed objects.

Place clusters with deterministic Poisson-like sampling over crown-field surfaces or selected branch tips. Avoid uniform random distribution.

- Creation strongly supports both flowers and fruit.
- Life supports abundance.
- Preservation may favor fruit or seed forms over transient flowers.
- Transformation may produce unusual mixed or asymmetric forms.
- Arcane tradition may produce geometric or emissive fruit.
- Primal tradition may produce organic abundance and irregular clustering.
- Death may produce seed pods, pale blossoms, fungi, or funereal fruit rather than merely disabling the feature.

Flowers and fruit use separate semantic surfaces so their accents can remain distinct from foliage.

### 7.12 Wind and motion

The first version should animate foliage and small hanging features, not deform the full trunk substantially.

Store wind weight and phase offset in vertex attributes or an auxiliary mask. The shader combines these with the Plane's wind direction and intensity. Air affinity, Wandering Manifestation, and Instinctive tradition may increase response, but scene weather remains a separate runtime input.

Do not encode semantic affinity into the scene's physical wind value. Semantics determine how the asset responds; current weather determines the force applied.

## 8. Wheel-to-tree parameter conversion

### 8.1 Influence extraction

Given signed resolved channels, define positive components:

```text
life = max(0, vitality)
death = max(0, -vitality)
order = max(0, regularity)
chaos = max(0, -regularity)
transform = max(0, transformation)
preserve = max(0, -transformation)
create = max(0, genesis)
annihilate = max(0, -genesis)
embodied = max(0, embodiment)
incorporeal = max(0, -embodiment)
rooted = max(0, anchoring)
wandering = max(0, -anchoring)
```

Elemental weights are already non-negative. All inputs must include their source salience before reaching the tree adapter.

### 8.2 Baseline plus bounded modifiers

Start from a coherent deciduous-tree baseline. Semantic channels modify it within validated ranges. Do not generate every parameter independently from noise.

The initial formulas below are design defaults, not final balance constants:

```text
foliageCoverage = clamp01(
    0.72
    + 0.24 * life
    - 0.48 * death
    + 0.12 * create
    - 0.24 * annihilate
    + 0.10 * water
    - 0.12 * fire)

branchRegularity = clamp01(
    0.55
    + 0.38 * order
    - 0.38 * chaos
    + 0.12 * controlled
    - 0.10 * instinctive)

branchTortuosity = clamp01(
    0.22
    + 0.42 * chaos
    + 0.20 * transform
    + 0.12 * wandering
    - 0.16 * order)

rootProminence = clamp01(
    0.42
    + 0.40 * rooted
    + 0.12 * earth
    - 0.30 * wandering
    - 0.20 * incorporeal)

hollowBias = clamp01(
    0.08
    + 0.30 * death
    + 0.18 * transform
    + 0.16 * chaos
    + 0.18 * annihilate)

mossCoverage = clamp01(
    0.10
    + 0.26 * water
    + 0.18 * life
    + 0.12 * shadow
    + 0.12 * preserve
    - 0.20 * fire
    - 0.12 * radiance)

flowerDensity = clamp01(
    -0.05
    + 0.28 * life
    + 0.38 * create
    + 0.10 * radiance
    - 0.24 * death
    - 0.30 * annihilate)

fruitDensity = clamp01(
    -0.02
    + 0.22 * life
    + 0.28 * create
    + 0.12 * preserve
    + 0.08 * earth
    - 0.22 * annihilate)
```

Use named constants for every coefficient. Tests must be able to print a contribution breakdown explaining why a parameter received its value.

### 8.3 Semantic responsibilities

| Input | Strong tree effects | Secondary effects |
|---|---|---|
| Life | Foliage, growth, flowers, fruit | Moss and vine suitability |
| Death | Bare crown, hollows, deadwood forms | Pale, fungal, or funereal accents |
| Order | Balanced crown and branch spacing | Controlled material pattern |
| Chaos | Asymmetry, torsion, omission | Broken palette regions |
| Preservation | Stable mature silhouette | Conservative feature placement |
| Transformation | Mutation, grafting, mixed forms | Vines and material transitions |
| Creation | New shoots, flowers, fruit | Abundant crown lobes |
| Annihilation | Missing volumes and arrested growth | Ash, gaps, reduced crown |
| Embodied | Mass and conventional PBR solidity | Strong contact shadows |
| Incorporeal | Dissolution and spectral separation | Lower apparent density |
| Rooted | Root flare and vertical stability | Strong host contact |
| Wandering | Lean, trails, drifting elements | Greater motion response |
| Elemental | Palette and environmental morphology | Surface and VFX language |
| Magical Tradition | Form of supernatural expression | Ornament and motion style |
| Cosmic Provenance | Rare motifs and accents | Emission and VFX character |

### 8.4 Preserve special contrasts

Several combinations must remain distinct:

- **Death + Creation:** prolific fungi, seed pods, pale blossoms, or new forms arising through death.
- **Life + Annihilation:** vigorous growth interrupted by voids, burned-away volumes, or repeatedly erased branches.
- **Order + Chaos at high center salience:** deliberate conflict, alternating branch sectors, or a balanced splitÃ¢â‚¬â€not average randomness.
- **Mundane + high salience:** visually excellent natural growth with little emission or impossible geometry.
- **Incorporeal + Earth:** a stone-heavy or mineral tree whose edges dissolve; do not cancel the values into normal wood.
- **Native Life tree in a Death host:** retain the tree's living crown while applying restrained host tint, root-seam decay, or local mist.

### 8.5 Generation budget and feature admission

Semantic suitability does not by itself grant every feature. The asset or card generation budget determines how many additional feature groups may be admitted.

Suggested sequence:

1. Generate the coherent baseline tree.
2. Calculate suitability for optional features.
3. Rank features by suitability plus deterministic variation.
4. Admit features while budget remains.
5. Apply downsides or unusual relationships only when supplied by the card-generation model.

This prevents every Life/Creation tree from always having moss, vines, flowers, fruit, and magical particles simultaneously.
