# Steady Arc Handoff

This repository currently carries two concurrent, non-overlapping handoff
records: the main arc record (Generic Asset Workflow Correction and Final POC
Validation, owned by the Great Tree reference generator) and the auxiliary
arc record (Expand the generator library). Each has its own handoff ID, scope,
and return condition, and neither authorizes work inside the other's scope.

## Record 1: Generic Asset Workflow Correction and Final POC Validation

### State

- **Status:** Active — implementation
- **Handoff ID:** PGA-2026-08-04-generic-asset-workflow-correction
- **From:** Omen
- **To:** Codex
- **Created:** 2026-08-04
- **Return owner:** Omen
- **Return condition:** The Generic Asset Workflow Correction and Final POC
  Validation arc is implemented and validated, or a material design decision
  requires Omen's direction.

### Engineering context

- **Current arc:** Generic Asset Workflow Correction and Final POC Validation
- **Active roadmap item:** 15. Integrate generation, semantics, and live visual preview
- **Authoritative baseline:** `main` at the start of this handoff, with 160 Maven
  tests passing through the Steady Arc relay.
- **Relevant durable notes:** `.steadyarc/engineering-notes.md`,
  `docs/architecture/generation-platform.md`, and `docs/guides/geometry-toolkit.md`.

### Delegation

- **Requested action:** Correct the tree-shaped authoring/runtime integration
  into a generic registered asset workflow, then perform roadmap items 11–17.
- **Immediate scope:** Establish generic provider, authoring-session,
  semantic-resolution, loaded-asset, environment, and optional runtime
  capability contracts before altering the workbench UI.
- **Constraints:**
  - The Great Tree is a reference provider, never a privileged subsystem.
  - Semantic wheels retain 2D direction/extremity and independent Salience.
  - Runtime environment inputs do not become semantic identity.
  - Reusable facilities remain independent of asset families and UI code.
  - Automated and human visual validation are recorded separately.
  - This record's scope is the Great Tree reference generator and the
    generic platform it exercises. It does not include the auxiliary
    generator-library arc in Record 2 below.

### Entry sequence

1. Read this handoff record and roadmap items 11–17.
2. Read the corresponding durable decisions and architecture documentation.
3. Inspect existing APIs, adapters, tools, previews, and tests before changing contracts.
4. Restate the defect and acceptance criteria in the implementation plan; do not
   rely on conversational task history as project memory.
5. Validate through the active Steady Arc relay before closing a bounded pass.

## Record 2: Expand the generator library

### State

- **Status:** Active — implementation
- **Handoff ID:** PGA-2026-08-05-expand-generator-library
- **From:** Omen
- **To:** Codex
- **Created:** 2026-08-05
- **Return owner:** Omen
- **Return condition:** The "Semantically aware crystals" generator (or the
  currently scoped generator item under this arc) is implemented and
  validated, or a material design decision requires Omen's direction.

### Engineering context

- **Current arc:** Parallel arc: Expand the generator library
- **Active roadmap item:** 1. Semantically aware crystals
- **Authoritative baseline:** same commit/branch baseline as Record 1 at the
  time this record was created; this arc is additive and must not diverge the
  platform contracts that Record 1 depends on without coordinating both
  records.
- **Relevant durable notes:** `.steadyarc/engineering-notes.md`,
  `docs/architecture/generation-platform.md`, and `docs/guides/geometry-toolkit.md`.

### Delegation

- **Requested action:** Implement one or more additional, individually
  tightly scoped asset generators, starting with "Semantically aware
  crystals," using the existing generic generator-provider platform.
- **Immediate scope:** Roadmap section "Parallel arc: Expand the generator
  library," item 1 only, unless and until further generator items are added
  to that same roadmap section.
- **Constraints:**
  - New generators are discovered only through the existing generic
    `AuthoringGeneratorProvider` registry; no generator-specific conditionals
    may be added to generic workbench or registry code.
  - New generators must not modify the Great Tree implementation or Record 1's
    active roadmap items; coordinate through this record instead of Record 1
    if a shared-platform change is required.
  - Reusable geometry, texture, material, and VFX facilities are extended
    only when a real generator need is demonstrated, not spun up speculatively.
  - Automated and human visual validation are recorded separately, matching
    the standard used for the Great Tree reference generator.
- **Non-overlapping scope note:** This record and Record 1 may both be
  active at once. They must not both claim ownership of the same file or
  roadmap item; if a change to shared platform code is required, it is
  proposed here and coordinated with Record 1's owner rather than made
  unilaterally.

### Entry sequence

1. Read this handoff record and the "Parallel arc: Expand the generator
   library" section of `.steadyarc/roadmap.md`, including its architectural
   boundary instructions.
2. Read the corresponding durable decisions and architecture documentation
   referenced above.
3. Inspect the existing generator-provider registry, descriptor contract, and
   the Great Tree provider as a worked reference before adding a new provider.
4. Restate the defect/objective and acceptance criteria for the active
   generator item in the implementation plan; do not rely on conversational
   task history as project memory.
5. Validate through the active Steady Arc relay before closing a bounded pass.
