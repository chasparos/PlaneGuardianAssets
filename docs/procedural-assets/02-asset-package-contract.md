# Asset Package Contract

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 4. Asset package contract

### 4.1 Responsibilities

The asset index describes the package-level recipe and dependencies. glTF `extras` annotate nodes, meshes, materials, and sockets local to a glTF asset.

Do not use display names as behavior identifiers. Names help artists and debugging; stable IDs in `extras` define behavior.

### 4.2 Example index entry

```json
{
  "id": "landmark.tree.deciduous.great-tree",
  "schema": "pg.asset/1",
  "generator": "pg.tree.deciduous/1",
  "semanticAdapter": "pg.vegetation/1",
  "materialContract": "pg.pbr-semantic/1",
  "fallbackGltf": "trees/great_tree_fallback.glb",
  "capabilities": [
    "generatedGeometry",
    "foliageVariants",
    "hollowTrunk",
    "moss",
    "vines",
    "flowers",
    "fruit",
    "hostBlend",
    "wind"
  ]
}
```

### 4.3 Example glTF node annotation

```json
{
  "name": "FoliageFull",
  "extras": {
    "planeGuardian": {
      "schema": "pg.gltf-node/1",
      "role": "foliage",
      "variantGroup": "foliageDensity",
      "variant": "full",
      "semanticSurface": "vegetation"
    }
  }
}
```

Recommended stable roles include:

- `trunk`
- `branch`
- `root`
- `hollowRim`
- `hollowInterior`
- `foliage`
- `moss`
- `vine`
- `flower`
- `fruit`
- `hostContact`
- `ambientVfxSocket`
- `attachmentSocket`

The exporter should generate index records and glTF annotations from a common intermediate representation rather than require each generator to assemble JSON manually.
