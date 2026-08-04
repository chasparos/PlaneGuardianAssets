# Generator Algorithm and Validation

**Design version:** 0.1
**Status:** Foundational design draft
**Parent index:** [Procedural Asset Generation](README.md)

## 11. Generator algorithm

The initial generation sequence is:

1. Validate and quantize `DeciduousTreeParameters`.
2. Derive all named random streams.
3. Generate trunk control points and spline.
4. Generate the branch graph.
5. Generate branch splines and reject invalid intersections.
6. Generate root splines and host-contact data.
7. Select and generate the hollow feature if admitted.
8. Extrude trunk, branch, and major-root geometry.
9. Construct the crown density field.
10. Select foliage state: none, sparse, or full.
11. Generate foliage cluster shells and silhouette cards.
12. Generate moss mask and optional hanging moss.
13. Generate admitted vine splines.
14. Place admitted flower and fruit clusters.
15. Generate semantic masks, UVs, normals, and tangents.
16. Build LODs.
17. Bind semantic PBR materials.
18. Attach VFX and gameplay sockets.
19. Compute bounds and reproducibility fingerprint.
20. Validate the finished scene subtree.

Expensive generated results should be cached by the complete visual fingerprint. Generation may occur off the render thread, but creation or attachment of jME GPU resources must follow the engine's threading requirements.

## 12. Validation and reference tests

### 12.1 Determinism

- Identical request and versions produce identical quantized parameters.
- Generated mesh buffers have stable hashes on supported platforms.
- Changing one named substream does not perturb unrelated layers.
- Historical generator versions remain loadable or have baked fallbacks.

### 12.2 Geometry

- No NaN or infinite positions, normals, tangents, or bounds.
- Branches visibly connect to their parents.
- No foliage occupies the hollow or trunk interior.
- Root endpoints meet or enter the host-contact volume.
- LOD changes preserve the primary silhouette.
- Attachment and VFX sockets remain stable for the generated fingerprint.

### 12.3 Rendering

- Materials respond correctly to jME directional, point, ambient, and image-based lighting used by the client.
- The tree casts appropriate shadows in every foliage state.
- The crown remains stable under mipmapping and normal camera motion.
- Incorporeal treatments remain legible without turning the entire asset transparent.
- Emission remains an accent and does not erase PBR form.
- Off-color host blending is visible at contact regions but does not replace native identity.

### 12.4 Semantic golden cases

Maintain fixed seeds for at least these golden outputs:

1. Mundane balanced deciduous tree.
2. Life + Creation + Water.
3. Death + Preservation + Earth.
4. Death + Creation.
5. Chaos + Transformation + Air.
6. Order + Arcane + Radiance.
7. Incorporeal + Aether + Wandering.
8. Strongly Rooted + Embodied + Earth.
9. Native verdant tree with a Death-aligned host.
10. High-salience balanced Ethos using each center relationship mode.

For every golden case, save:

- Input semantic profile.
- Resolved tree parameters.
- Contribution trace.
- Mesh and material fingerprint.
- Reference screenshots under fixed lighting and camera positions.
