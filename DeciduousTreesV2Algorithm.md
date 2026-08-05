## Parametric Natural Tree Algorithm

### Scope

This algorithm is intended to generate natural-looking deciduous trees. It does not attempt to reproduce the full biological growth process. Instead, it models the visible structural consequences of that process.

A tree grows by seeking access to sunlight. It extends upward and outward, producing a branching network that distributes leaves throughout the available canopy volume. Because light, space, gravity, damage, and local growing conditions vary, trees can develop a very wide range of forms.

The purpose of the algorithm is to reproduce this variety through a controllable system of curves and branching rules.

### Simplified Tree Anatomy

For this algorithm, the tree is represented as a hierarchy:

**Trunk → Branch Levels → Twigs → Leaves**

The trunk and branches form the primary structural network. Twigs form the fine terminal structure that supports the leaves.

Each structural element is represented by a spline or curve with a variable cross-section.

### Branching and Splitting

A growing limb can divide in two distinct ways:

**Branching** creates a subordinate child limb. The parent continues as the dominant growth axis, while the child begins at a lower structural level.

**Splitting** divides the parent growth axis into two or more approximately equal continuations. The resulting limbs remain within the same branching level and share the available cross-sectional area.

In real trees, many new shoots begin growing, but only those that find sufficient light and open space become permanent branches. Shoots that remain shaded or obstructed weaken, die, and eventually fall away.

The algorithm does not need to simulate this entire process directly. However, branch placement should approximate its outcome by favouring directions with available growth space and access to the outer canopy.

### Canopy Space and Growth Targets

The large-scale form of the tree is controlled by a three-dimensional **canopy envelope**.

The canopy envelope may be spherical, ellipsoidal, asymmetric, tilted, compressed, stretched, or otherwise deformed. Its shape establishes the approximate volume the mature tree is allowed to occupy.

A set of ordered **growth targets** is distributed on or within this envelope. These targets represent desirable regions of future canopy occupation.

The targets should repel one another, or be generated using a spacing method such as Poisson-disc distribution, so that they spread across the available canopy rather than clustering together.

The shape, orientation, density, and distribution of these targets provide macro-level control over the silhouette of the tree.

### Trunk Growth

The trunk begins at the origin and is assigned an initial growth target.

Its starting direction is controlled by a **sprouting angle**, which defines the initial departure from the ground and the preferred upward direction.

The trunk then follows a primary growth curve toward its target. A curvature parameter controls how quickly it aligns itself with that target:

* A high early-turn value causes the trunk to bend toward the target close to its base.
* A low early-turn value causes it to preserve its initial direction and curve gradually over its full length.

The resulting curve should not be perfectly smooth. Natural growth is affected by changing conditions, uneven loading, damage, and local obstruction. To represent this, a secondary irregularity function called the **gnarl field** is superimposed onto the primary growth curve.

The gnarl field introduces controlled deviations in direction, curvature, and possibly torsion while preserving the overall movement toward the selected target.

### Recursive Growth

At selected positions along a limb, the algorithm evaluates whether to continue, branch, or split.

When a branch is created, it receives a new target and begins at the next branch level.

When a split is created, each continuation receives a new target, but both remain at the same branch level. Their cross-sectional areas are derived from that of the parent limb.

This process continues recursively until the maximum branch depth, minimum limb radius, minimum available growth space, or another termination condition is reached.

The main trunk-and-branch network therefore emerges from repeated target-seeking growth under spatial, structural, and hierarchical constraints.
