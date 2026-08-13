## Result of manual review 2026-08-13

I'm still noting a misunderstanding of the core concept here. Both in the documentation and the actual source code.

The point is that the loaded runtime asset should change its makeup given a semantic profile via an initialization or initialization-mutator step (e.g., `applySemantics(...)`).
We should NOT regenerate geometries at that point. Instead, elements (geometries, vfx, and at a later stage, child assets) of the asset should show/hide based on the profile. Colors and textures can change or blend, or the material can change entirely. An asset that includes an imported resource from Blender, or even certain generated geometries, can have morph targets (shape keys) driven by a 0-1 value, local point lights, or anything the asset generator defines. All of this must be controlled by a semantic JME AppComponent/Controller.

The goal is a dynamic representation of: "Player attaches a great oak to this 'Death and darkness' land." The oak asset should inherit/combine its own semantic wheel with that of the land, resulting in a completely different look.

This means that each Asset Generator should generate a "layered", conditionally-visible structure (e.g., place some skulls on the ground if the semantic context contains "death"). There is some of this present and working in the crystal and tree generators, though none of them do it well at the moment. The sample generator does it entirely wrong by trying to rebuild meshes.

For this to work and be the flexible, easily extendable system I envision, we need structural changes. I'll describe the target generator code structure in pseudo-Java:

```Java
// 1. Add a sub-asset structure and the predicate for toggling visibility
assetRoot.addSingleSemanticElement(semanticProfile -> semanticProfile.isDeath(), generateDeadTreeGeometryIncludingMaterial(profile));

// 2. Select a sub-asset from a list via a predicate and index mapper 
assetRoot.addSemanticElementList(semanticProfile -> !semanticProfile.isDeath(), semanticProfile -> isNature() ? 0 : 1, List.of(...,...))

// 3. Conditional scene graph sub-trees under a semantic toggle
radianceNode = assetRoot.newChildNode(semanticProfile -> semanticProfile.getRadiance() > 0.5);

// 4. Sub-object within a conditional branch that has no further variants 
radianceNode.addSingleElement(generateHalo());

// 5. VFX/Particles follow the exact same structural rules
radianceNode.addSingleSemanticElement(semanticProfile -> semanticProfile.getRadiance() > 0.9, generateRadianceBeams());
```

**Architectural Notes on the Pseudo-code:**
* **Decoupled Logic:** The predicates and index mappers should not be hardcoded inline inside the generator. Instead, they should live in the semantic JME controller, or eventually be expressed as JavaScript strings evaluated via GraalVM. For now, design the framework to accept functional interfaces (`Predicate`, `Function`) so we can swap out the evaluation backend later.
* **Morph Targets / Animations:** If an asset uses a Blender glTF file with morph targets, the controller should sample the `AnimComposer` action at a static time offset or set a specific morph weight once during this initialization step, rather than running a continuous runtime animation loop.

### Using Planeguardian nomenclature, this is exactly what I want to do:

1. A card is discovered. It is a land with 2 attachment slots.
2. The asset library contains 50 complete, curated floating island assets. (Semantic asset matching is TBD, but will likely use an `isValid(profile)` predicate or a short JS snippet attached to the asset). We use the card's unique hash to deterministically select one of these 50 templates.
3. We load the asset template, clone/instantiate it for our scene, initialize it once with the card's semantic wheel profile (which triggers the structural show/hide logic and sets morph weights), and attach it to the scene graph.
4. Later, the player attaches a different card (a land modifier) to one of the slots. We use the same method to select the modifier asset, load it, and attach it. We then update the combined state using a lerped semantic wheel profile.

The Lerp function will use saliency and power to interpolate values. For example, adding a weak life tree to a strong death land will cause the tree's profile evaluation to make it appear "more" dead.

Please rewrite our implementation strategy and refactor the base generator architecture to support this conditional, initialization-time scene graph manipulation.
