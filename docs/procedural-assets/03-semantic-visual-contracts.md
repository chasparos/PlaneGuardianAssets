# Semantic Inputs and Visual Contracts

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 5. Semantic model available to the asset repository

### 5.1 Common wheel representation

Two-axis wheels use normalized Cartesian coordinates and a separate salience:

```java
public record WheelSample(
        float x,             // -1..1
        float y,             // -1..1
        float salience,      // 0..1
        CenterRelation centerRelation) {

    public float radius() {
        return Math.min(1f, (float) Math.sqrt(x * x + y * y));
    }
}
```

The basic terms are:

```text
extremity = radius(x, y)
directional strength = salience * extremity
center strength = salience * (1 - extremity)
```

Low salience means the wheel contributes little even if its coordinate is extreme. High salience near the center is meaningful: it indicates that the center state is important rather than absent.

Where a high-salience center combines opposites, `centerRelation` describes how they coexist:

- `EQUILIBRIUM`
- `SYNTHESIS`
- `CONFLICT`
- `ALTERNATION`
- `SUPPRESSION`
- `PARADOX`

The visual resolver must preserve these modes. It must not average every center relationship into an undistinguished middle color.

### 5.2 Elemental Affinity

The Elemental wheel is the strongest direct visual influence for most Lands. Its eight poles, clockwise, are:

```text
Radiance -> Fire -> Air -> Aether -> Shadow -> Ice -> Water -> Earth -> Radiance
```

Opposites are:

- Radiance and Shadow
- Fire and Ice
- Air and Water
- Aether and Earth

Store the source as angle, extremity, and salience, or equivalently as normalized `x` and `y` plus salience. Resolve it into eight non-negative elemental weights for asset use.

One suitable initial kernel is:

```text
delta(i) = shortest angular distance from the sample to pole i
raw(i) = max(0, cos(delta(i))) ^ ELEMENT_LOBE_POWER
weight(i) = raw(i) * extremity * salience
```

Start with `ELEMENT_LOBE_POWER = 4` and tune visually. Adjacent elements may coexist; opposite elements only become jointly strong through an explicit center relationship or another semantic rule.

Elemental center strength represents elemental neutrality, equilibrium, or muted elemental expression according to `centerRelation`. It must not automatically make all eight elements visually active.

### 5.3 Ethos

Coordinate convention:

```text
x: Chaos (-1) <-> Order (+1)
y: Death (-1) <-> Life (+1)
```

For vegetation:

- Life increases biological fullness, healthy growth, foliage, flowers, and regenerative cues.
- Death increases bare structure, cavities, brittleness, decay, fungal cues, and funereal forms.
- Order increases regular spacing, balanced crowns, repeated branching, and controlled silhouettes.
- Chaos increases asymmetry, torsion, interrupted growth, and irregular branching.

Death is not identical to damage. A Death-aligned tree may be healthy and complete according to its own nature. Current damage belongs to `InstanceVisualState`.

### 5.4 World Relation

Coordinate convention:

```text
x: Preservation (-1) <-> Transformation (+1)
y: Annihilation (-1) <-> Creation (+1)
```

For vegetation:

- Preservation favors old, stable, strongly retained forms and conservative variation.
- Transformation favors mutation, grafting, dramatic transitions, unusual growth, and mixed materials.
- Creation favors sprouts, flowers, fruit, new branch tips, abundance, and generative motifs.
- Annihilation favors absence, erosion, missing volumes, ash, voids, and arrested growth.

Creation is not the same as Life, and Annihilation is not the same as Death. Life and Death describe the nature of living processes; Creation and Annihilation describe whether form is being brought forth or removed.

### 5.5 Manifestation

Coordinate convention:

```text
x: Incorporeal (-1) <-> Embodied (+1)
y: Wandering (-1) <-> Rooted (+1)
```

For vegetation:

- Embodied increases apparent mass, solidity, contact, and conventional material response.
- Incorporeal permits dissolution, translucent edges, spectral gaps, and reduced physical continuity.
- Rooted strengthens root flare, downward weight, terrain contact, and a stable vertical composition.
- Wandering permits leaning, drifting fragments, trailing forms, airborne roots, and directional motion.

Manifestation governs how an object occupies space. It must remain distinct from Earth affinity, which describes elemental substance and geology.

### 5.6 Magical Tradition

Coordinate convention:

```text
x: Primal (-1) <-> Arcane (+1)
y: Instinctive (-1) <-> Controlled (+1)
```

The center means Mundane: achieved through manual labor, ordinary growth, or "the hard way." Therefore:

```text
supernaturalism = salience * extremity
mundanity = salience * (1 - extremity)
```

For vegetation:

- Primal favors organic power, wild rituals, symbiosis, animalistic forms, and growth-based magic.
- Arcane favors geometry, runes, crystalline structures, deliberate magical channels, and abstract light.
- Controlled favors regular placement, bounded effects, cultivated forms, and precise ornament.
- Instinctive favors spontaneous outgrowths, irregular pulses, reactive motion, and untamed effects.
- Mundane suppresses overt supernatural effects without making the asset visually unimportant.

### 5.7 Cosmic Provenance

The initial asset-facing representation is:

```text
bias: Infernal (-1) <-> Divine (+1)
salience: 0..1
```

The final pantheon and infernal hierarchy are generated from the Universe seed. Those identities may later provide additional motif sets. Until then, the generic axis supplies only bounded secondary treatment:

- Divine: consecrated light, elevated ornament, clarity, halos, or ordered radiance.
- Infernal: oppressive emission, scars, hooks, brands, smoke, or hierarchical marks.

Ordinary Lands should usually have low provenance relevance. Do not recolor everything gold or red merely because this value is non-zero.

## 6. Resolved visual contracts

### 6.1 Land visual profile

The asset repository should depend on a plain, immutable profile rather than gameplay objects:

```java
public record LandVisualProfile(
        int schemaVersion,
        VisualPalette palette,
        ElementalWeights elements,
        float vitality,             // Death -1 .. Life +1
        float regularity,           // Chaos -1 .. Order +1
        float transformation,       // Preservation -1 .. Transformation +1
        float genesis,              // Annihilation -1 .. Creation +1
        float embodiment,           // Incorporeal -1 .. Embodied +1
        float anchoring,             // Wandering -1 .. Rooted +1
        float supernaturalism,      // 0 .. 1
        float mundanity,             // 0 .. 1
        float traditionX,           // Primal -1 .. Arcane +1
        float traditionY,           // Instinctive -1 .. Controlled +1
        float provenanceBias,       // Infernal -1 .. Divine +1
        float provenanceStrength,   // 0 .. 1
        CenterModes centerModes) {
}
```

Resolve and quantize these values on the CPU. Presentation shaders receive only the final colors and render-oriented parameters needed by their material contract.

### 6.2 Palette roles

```java
public record VisualPalette(
        ColorRGBA shadow,
        ColorRGBA base,
        ColorRGBA highlight,
        ColorRGBA accentPrimary,
        ColorRGBA accentSecondary,
        ColorRGBA emissive) {
}
```

Palette generation should happen centrally in a perceptual color representation and be converted to linear RGB for rendering. Preserve luminance separation between shadow, base, and highlight. Do not hue-shift an entire asset uniformly.

### 6.3 Intrinsic semantics, host, and mutable state

Apply layers in this order:

```text
Generator-native asset identity
    -> intrinsic card semantics
    -> bounded host-Land integration
    -> mutable instance condition
    -> jME lighting and scene effects
```

Examples of mutable condition include damage, dormancy, burning, temporary curses, and seasonal state. These must not overwrite the intrinsic Life-Death identity.
