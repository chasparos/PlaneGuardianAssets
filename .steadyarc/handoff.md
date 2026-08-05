# Steady Arc Handoff

## State

- **Status:** Active — implementation
- **Handoff ID:** PGA-2026-08-04-generic-asset-workflow-correction
- **From:** Omen
- **To:** Codex
- **Created:** 2026-08-04
- **Return owner:** Omen
- **Return condition:** The Generic Asset Workflow Correction and Final POC
  Validation arc is implemented and validated, or a material design decision
  requires Omen's direction.

## Engineering context

- **Current arc:** Generic Asset Workflow Correction and Final POC Validation
- **Active roadmap item:** 15. Integrate generation, semantics, and live visual preview
- **Authoritative baseline:** `main` at the start of this handoff, with 160 Maven
  tests passing through the Steady Arc relay.
- **Relevant durable notes:** `.steadyarc/engineering-notes.md`,
  `docs/architecture/generation-platform.md`, and `docs/guides/geometry-toolkit.md`.

## Delegation

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

## Entry sequence

1. Read this handoff and roadmap items 11–17.
2. Read the corresponding durable decisions and architecture documentation.
3. Inspect existing APIs, adapters, tools, previews, and tests before changing contracts.
4. Restate the defect and acceptance criteria in the implementation plan; do not
   rely on conversational task history as project memory.
5. Validate through the active Steady Arc relay before closing a bounded pass.
