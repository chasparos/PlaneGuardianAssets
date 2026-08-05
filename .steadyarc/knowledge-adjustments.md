# Knowledge Adjustments Pending Upstream

This file records project-local adjustments to Steady Arc knowledge files made
to enable a real project need, so they can be evaluated and, if accepted,
folded into the normative `SteadyArcWorkflow` release later. It is not itself
normative; it is a request queue for the workflow's maintainers.

## PGA-2026-08-05: Support a genuinely parallel auxiliary arc

### Problem

`knowledge/SteadyArc_Workflow.md` defines the roadmap as a single current
Engineering Arc with a single active item, and `.steadyarc/handoff.md` as a
single current-state record (allowing only a queue of amendments or a
successor with a new ID). This repository needed a second, genuinely
concurrent Engineering Arc — "Expand the generator library" — running
alongside the main "Generic Asset Workflow Correction and Final POC
Validation" arc, without either arc's completion or return implicitly closing
or reinterpreting the other.

The normative documents already anticipate "parallel independent delegations"
(`knowledge/SteadyArc_Workflow.md`, ownership-transition invariants: "parallel
independent delegations use separate handoff IDs and non-overlapping
scopes"), but they do not describe how to represent two concurrently active
arcs and two concurrently active handoff records inside the standard
single-current-record file layout.

### Local adjustment made in this repository

- `.steadyarc/roadmap.md` now contains the existing single main
  "Active arc" section unchanged, plus a new, clearly labeled
  "Parallel arc: Expand the generator library" section with its own ordered
  items and its own active-item marker. The main arc's "Current focus" line
  was left untouched, and the parallel arc's items are numbered independently
  starting at 1 within its own section, so the two arcs' item numbers never
  collide.
- `.steadyarc/handoff.md` now contains two clearly separated
  `## Record N: <arc name>` sections in one file, each with its own complete
  State / Engineering context / Delegation / Entry sequence, its own handoff
  ID, and an explicit note in each record's constraints stating that it does
  not authorize work in the other record's scope.
- `.steadyarc/engineering-notes.md` records the existence and rationale of the
  two-record handoff file as a durable repository fact.

### Suggested normative change for `SteadyArcWorkflow`

1. In `knowledge/SteadyArc_Workflow.md` §2 (Engineering Arcs), state that a
   repository may have more than one concurrently Active Engineering Arc when
   the human explicitly designates one as a bounded, clearly labeled parallel
   (auxiliary) arc with a non-overlapping objective, and that the roadmap must
   keep the arcs in clearly separate sections with independently scoped item
   numbering.
2. In `knowledge/SteadyArc_Workflow.md` §9 (or wherever the handoff-file
   layout is specified) and in `.steadyarc/handoff.md`'s own guidance, allow a
   single `handoff.md` to contain multiple current, non-`Closed` records when
   each covers a distinct, explicitly non-overlapping arc, each keeps its own
   handoff ID/state/delegation/entry-sequence, and each explicitly disclaims
   authority over the other record's scope. Keep the existing rule that a
   *single* arc's handoff stays one current record (amendment vs. new ID)
   unchanged; the adjustment only permits multiple records when they represent
   genuinely different concurrently active arcs.
3. Update `knowledge/SteadyArc_InformationArchitecture.md`'s description of
   `.steadyarc/handoff.md` ("the authoritative, append-preserving record of a
   bounded transfer of engineering responsibility") to acknowledge that the
   file may hold more than one such record at a time under the condition
   above, while still being "the single current record" collectively (i.e.,
   there is one file, not one-record-per-file, and `handoff-history/` still
   receives a record once *that specific* arc's handoff is returned/closed).

### Status

Proposed; not yet applied to the upstream `SteadyArcWorkflow` release. Treat
this repository's `.steadyarc/roadmap.md` and `.steadyarc/handoff.md` as the
concrete worked example if a maintainer wants to adopt the change.
