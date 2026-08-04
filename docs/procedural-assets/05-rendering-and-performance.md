# Rendering and Performance

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 9. jMonkeyEngine material and scene integration

### 9.1 PBR contract

Custom tree materials must preserve the normal jME PBR lighting path. Semantic processing modifies base color, roughness, metallic response, normal strength, opacity where supported, and emission before or within the PBR evaluation. It must not replace scene lighting with an unlit affinity color.

Standard material parameters should include:

```text
PgPaletteShadow
PgPaletteBase
PgPaletteHighlight
PgPaletteAccentPrimary
PgPaletteAccentSecondary
PgPaletteEmissive
PgEmissionStrength
PgRoughnessBias
PgNormalStrength
PgHostBlendStrength
PgDissolution
PgMotionStrength
```

Tree-specific additions may include:

```text
PgLeafDryness
PgLeafVariation
PgCrownInterior
PgWindAmplitude
PgWindFrequency
```

### 9.2 Semantic masks

Reserve a vertex-color set or compact mask texture for semantic material roles. One initial convention is:

```text
R = primary palette influence
G = secondary/accent influence
B = emissive or magical detail
A = host-Land blend strength
```

If vertex alpha is needed for another engine feature, use a dedicated mask texture instead. Record the chosen encoding in glTF material `extras`.

### 9.3 Material ownership and caching

Never mutate a shared material loaded from the asset cache. Clone it for a unique profile or cache material instances by:

```text
material template ID
resolved palette hash
resolved surface-parameter hash
shader version
render tier
```

Merge geometry that shares an identical resolved material where practical.

## 10. Performance and detail budget

The normal Plane view, not a close-up beauty render, determines the budget. Small bark cuts, individual leaves, tiny flowers, and thin roots should normally be textures, clustered shapes, or omitted.

The main stage has two explicit island-density endpoints:

| View condition | Island upper limit | Expected representation |
|---|---:|---|
| Maximum-detail zoom | 10 visible islands | Near/high-detail island LODs and selected detailed attachments |
| Overview zoom | 100 visible islands | Distant island LODs, simplified attachments, clusters, or impostors |

Other stage content is expected to be predominantly textures, billboards, and
UI elements. Island and attached-asset budgets should therefore be evaluated as
a complete stage composition at both endpoints. The overview case is not 100
copies of near-view geometry: projected screen size must select substantially
lighter topology, shadow participation, materials, and attachment density.

Do not turn these population limits into premature fixed triangle caps. First
preserve the silhouettes and large forms needed by the painterly direction,
then establish per-tier topology, draw-call, shadow, overdraw, memory, and
generation-time budgets through representative 10-island and 100-island scenes.

Initial profiling targets for a hero-quality Great Tree may begin around:

| Render tier | Trunk and branches | Foliage | Intended use |
|---|---:|---:|---|
| LOD 0 | 8k-20k triangles | 2k-8k triangles | Inspection/near view |
| LOD 1 | 25-40% of LOD 0 | 20-35% of LOD 0 | Normal Plane view |
| LOD 2 | 500-1,500 triangles or impostor | Included in silhouette | Distant Plane view |

These are starting ranges, not promises. Profile overdraw, draw calls, shadow rendering, material switches, generation time, and memory in the actual main-stage composition.

Prefer alpha clipping or dithered coverage over sorted alpha blending for the main foliage mass. Validate mipmaps and alpha thresholds at distance to avoid disappearing crowns.
