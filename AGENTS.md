# Codex Instructions

Steady Arc governs repository workflow in this project. This file is the Codex entry point, not a duplicate workflow specification.

## Enter the repository

Read in this order and stop once you have enough authority and context for the delegated task:

1. `.steadyarc/handoff.md` — current ownership, delegated authority, constraints, and return condition.
2. The current arc and active item in `.steadyarc/roadmap.md`.
3. Relevant sections of `.steadyarc/engineering-notes.md`; do not read it as a progress log.
4. `knowledge/SteadyArc_CodexWorkflow.md` only for Codex-specific operation, continuation-artifact handling, or knowledge-ZIP bootstrap/update work.
5. `knowledge/SteadyArc_InformationArchitecture.md` only when document authority or placement is unclear.

Inspect source, tests, scripts, and current validation evidence before changing behavior.

For geometry work, read `docs/guides/geometry-toolkit.md` before designing a
new generator. Prefer composition of the shared toolkit; asset-specific geometry
primitives require a demonstrated gap and should normally become reusable
engine-neutral operations first.

## Validation artifact workflow (GitHub Copilot task agent)

File uploads are not available in this interface. Instead, the human operator runs `PatchSequence.ps1` locally, then pushes the branch including the generated artifacts so this agent can read them directly.

**Reading artifacts after a push:**
- `latest snapshot manifest.json` — always read first; contains the committed source revision SHA, build outcome, and test summary.
- `latest test results.log` — always read for full Maven output when diagnosing failures.
- `latest snapshot.zip` — gitignored (large binary); only request it when inspecting compiled output or files not visible from the patch.

**Manifest SHA vs HEAD mismatch:** `PatchSequence.ps1` commits the source changes, generates the manifest and log, then the human makes a second push commit that includes those artifact files. The `repository.commit` in the manifest identifies the committed source revision; the artifacts themselves live in the subsequent commit. This is expected. Treat the manifest's `repository.commit` as the authoritative source baseline, not the current HEAD when the artifacts were pushed.

## Authority boundary

Treat `Active — review` as inspection-only. Treat `Active — implementation` as the named receiver's bounded delegated scope, not as general permission to modify the repository. A request to inspect continuation artifacts and continue authorizes inspection and planning unless an applicable handoff or explicit human instruction grants implementation authority.

Ask the human about material ambiguity before implementation when the handoff is review-only or its completion criteria cannot be satisfied safely.

## During delegated work

Follow the documentation placement rules in `knowledge/SteadyArc_InformationArchitecture.md`:

- preserve the original delegation and use `.steadyarc/handoff.md` only for material ownership, scope, constraint, blocking-question, or return changes;
- keep routine progress in commits, tests, code comments, or implementation documentation;
- record durable project decisions in `.steadyarc/engineering-notes.md`;
- record unrelated discoveries in `.steadyarc/deferred-issues.md`;
- complete the handoff return report when responsibility returns.

Before the last implementation step or return-document update, determine whether required validation needs the human-session support relay. If it does, check the channel and ask the human to start or restart it early; do not wait until validation is the only remaining action.

When given a Steady Arc knowledge ZIP for another repository, follow `knowledge/SteadyArc_CodexWorkflow.md`. Start with package inspection and updater dry-run. Never replace populated `.steadyarc/` project memory or locally modified managed tools without explicit human approval.
